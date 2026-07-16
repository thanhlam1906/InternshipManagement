import { useState, useEffect, useRef } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { studentApi, assignmentApi, resultApi, phaseApi, roundApi } from '@/services/api'
import {
  FileText, Briefcase, Clock, Target, Award, TrendingUp,
  ArrowRight, CheckCircle2, AlertCircle, Circle, Sparkles, GraduationCap
} from 'lucide-react'
import LoadingSpinner from '@/components/LoadingSpinner'

const statusConfig = {
  PENDING: { label: 'Chờ xử lý', color: 'bg-amber-100 text-amber-700 border-amber-200' },
  IN_PROGRESS: { label: 'Đang thực tập', color: 'bg-blue-100 text-blue-700 border-blue-200' },
  COMPLETED: { label: 'Hoàn thành', color: 'bg-emerald-100 text-emerald-700 border-emerald-200' },
  CANCELLED: { label: 'Đã hủy', color: 'bg-red-100 text-red-700 border-red-200' },
}

function scoreBadge(score) {
  if (score == null) return null
  const n = Number(score)
  if (n >= 8) return { color: 'text-emerald-600 bg-emerald-50', icon: CheckCircle2 }
  if (n >= 5) return { color: 'text-amber-600 bg-amber-50', icon: AlertCircle }
  return { color: 'text-red-600 bg-red-50', icon: Circle }
}

export default function StudentDashboardPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [profile, setProfile] = useState(null)
  const [assignments, setAssignments] = useState([])
  const [results, setResults] = useState([])
  const [phases, setPhases] = useState([])
  const [rounds, setRounds] = useState([])
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true
    return () => { mountedRef.current = false }
  }, [])

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true)
      try {
        const [profileRes, assignRes, resultRes, phaseRes, roundRes] = await Promise.allSettled([
          studentApi.getById(user.userId).then(r => r.data.data || null).catch(() => null),
          assignmentApi.getAll({ studentId: user.userId, page: 0, size: 10 }),
          resultApi.getAll({ studentId: user.userId, page: 0, size: 200 }),
          phaseApi.getAll({ page: 0, size: 20 }),
          roundApi.getAll({ page: 0, size: 100 }),
        ])

        if (mountedRef.current) {
          if (profileRes.status === 'fulfilled') setProfile(profileRes.value)
          if (assignRes.status === 'fulfilled') setAssignments(assignRes.value?.data?.data?.items || [])
          if (resultRes.status === 'fulfilled') setResults(resultRes.value?.data?.data?.items || [])
          if (phaseRes.status === 'fulfilled') setPhases(phaseRes.value?.data?.data?.items || [])
          if (roundRes.status === 'fulfilled') setRounds(roundRes.value?.data?.data?.items || [])
        }
      } catch {
        // Loi da duoc xu ly boi UI (Promise.allSettled + loading state)
      } finally {
        if (mountedRef.current) setLoading(false)
      }
    }
    fetchData()
  }, [user.userId])

  if (loading) return <LoadingSpinner text="Đang tải thông tin..." />

  const initials = user?.fullName
    ?.split(' ')
    .map(n => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase() || 'U'

  // Find current active phase
  const activeAssignment = assignments.find(a => a.status === 'IN_PROGRESS') || assignments[0]
  const activePhase = activeAssignment
    ? phases.find(p => p.id === activeAssignment.phaseId)
    : null

  // Phase timeline info — only show phases the student is assigned to
  const assignedPhaseIds = new Set(assignments.map(a => a.phaseId))
  const assignedPhases = phases.filter(p => assignedPhaseIds.has(p.id))
  const now = new Date()
  const sortedPhases = [...assignedPhases].sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
  const currentPhaseIdx = activePhase ? sortedPhases.findIndex(p => p.id === activePhase.id) : -1

  // Group rounds by phase
  const roundsByPhase = {}
  assignedPhaseIds.forEach(pid => {
    roundsByPhase[pid] = rounds.filter(r => r.phaseId === pid).sort((a, b) => new Date(a.startDate) - new Date(b.startDate))
  })

  // Lookup maps for cross-referencing
  const roundIdToPhaseId = {}
  rounds.forEach(r => { roundIdToPhaseId[r.id] = r.phaseId })
  const phaseIdToName = {}
  phases.forEach(p => { phaseIdToName[p.id] = p.phaseName })

  // Group results by phase → round
  const resultsByPhaseRound = {}
  results.forEach(r => {
    const pid = roundIdToPhaseId[r.roundId]
    if (!pid) return
    if (!resultsByPhaseRound[pid]) resultsByPhaseRound[pid] = {}
    if (!resultsByPhaseRound[pid][r.roundId]) resultsByPhaseRound[pid][r.roundId] = []
    resultsByPhaseRound[pid][r.roundId].push(r)
  })

  return (
    <div className="space-y-6">
      {/* ===== WELCOME BANNER ===== */}
      <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-primary via-primary to-accent p-6 md:p-8 text-white">
        {/* Decorative circles */}
        <div className="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/4" />
        <div className="absolute bottom-0 left-1/2 w-40 h-40 bg-white/5 rounded-full translate-y-1/3" />

        <div className="relative flex flex-col sm:flex-row items-start sm:items-center gap-4">
          <div className="w-16 h-16 rounded-full bg-white/20 backdrop-blur-sm flex items-center justify-center text-2xl font-bold ring-4 ring-white/10 shrink-0">
            {initials}
          </div>
          <div>
            <h1 className="text-xl md:text-2xl font-bold">
              Xin chào, {user?.fullName || 'Sinh viên'}!
            </h1>
            <p className="text-white/80 mt-1 text-sm md:text-base">
              {profile?.major ? `Ngành ${profile.major}` : 'Sinh viên thực tập'}
              {profile?.clazz ? ` • Lớp ${profile.clazz}` : ''}
            </p>
            {activeAssignment && (
              <div className="flex items-center gap-2 mt-3">
                <span className={`inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold border ${statusConfig[activeAssignment.status]?.color || statusConfig.PENDING.color}`}>
                  {statusConfig[activeAssignment.status]?.label || activeAssignment.status}
                </span>
                {activePhase && (
                  <span className="text-white/70 text-xs">{activePhase.phaseName}</span>
                )}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* ===== QUICK ACTIONS ===== */}
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <button
          onClick={() => navigate('/cv-review')}
          className="group relative overflow-hidden rounded-xl border-2 border-primary/20 bg-white p-5 hover:border-primary/40 hover:shadow-lg hover:shadow-primary/10 transition-all text-left"
        >
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-primary/10 flex items-center justify-center group-hover:bg-primary/20 transition-colors shrink-0">
              <FileText className="w-6 h-6 text-primary" />
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="font-bold text-base flex items-center gap-2">
                Review CV bằng AI
                <Sparkles className="w-4 h-4 text-amber-500" />
              </h3>
              <p className="text-sm text-muted-foreground mt-1">
                Tải lên CV và nhận đánh giá từ AI về format, nội dung, điểm mạnh và gợi ý cải thiện
              </p>
            </div>
            <ArrowRight className="w-5 h-5 text-muted-foreground group-hover:text-primary group-hover:translate-x-1 transition-all shrink-0 self-center" />
          </div>
        </button>

        <button
          onClick={() => navigate('/job-search')}
          className="group relative overflow-hidden rounded-xl border-2 border-accent/20 bg-white p-5 hover:border-accent/40 hover:shadow-lg hover:shadow-accent/10 transition-all text-left"
        >
          <div className="flex items-start gap-4">
            <div className="w-12 h-12 rounded-xl bg-accent/10 flex items-center justify-center group-hover:bg-accent/20 transition-colors shrink-0">
              <Briefcase className="w-6 h-6 text-accent" />
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="font-bold text-base">Tìm việc thực tập</h3>
              <p className="text-sm text-muted-foreground mt-1">
                Khám phá cơ hội thực tập phù hợp với ngành học của bạn từ các nền tảng tuyển dụng
              </p>
            </div>
            <ArrowRight className="w-5 h-5 text-muted-foreground group-hover:text-accent group-hover:translate-x-1 transition-all shrink-0 self-center" />
          </div>
        </button>
      </div>

      {/* ===== PROGRESS & RESULTS ===== */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Internship Timeline */}
        <div className="lg:col-span-3 bg-white rounded-xl border border-border p-6">
          <div className="flex items-center gap-2 mb-5">
            <Clock className="w-5 h-5 text-primary" />
            <h2 className="text-lg font-bold">Tiến độ thực tập</h2>
          </div>

          {sortedPhases.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground text-sm">
              <GraduationCap className="w-10 h-10 mx-auto text-muted-foreground/30 mb-2" />
              Chưa có đợt thực tập nào
            </div>
          ) : (
            <div className="relative">
              {/* Timeline line */}
              <div className="absolute left-[19px] top-2 bottom-2 w-0.5 bg-border" />

              <div className="space-y-4">
                {sortedPhases.map((phase, idx) => {
                  const isCurrent = currentPhaseIdx === idx
                  const isPast = idx < currentPhaseIdx
                  const isFuture = idx > currentPhaseIdx
                  const phaseRounds = roundsByPhase[phase.id] || []

                  return (
                    <div key={phase.id}>
                      <div className="relative flex gap-4 pl-1">
                        {/* Node */}
                        <div className={`relative z-10 w-9 h-9 rounded-full flex items-center justify-center shrink-0 border-2 transition-all ${
                          isCurrent
                            ? 'bg-primary border-primary text-white shadow-lg shadow-primary/30 animate-pulse'
                            : isPast
                              ? 'bg-emerald-100 border-emerald-400 text-emerald-600'
                              : 'bg-gray-100 border-gray-300 text-gray-400'
                        }`}>
                          {isPast ? <CheckCircle2 className="w-4 h-4" /> : <span className="text-xs font-bold">{idx + 1}</span>}
                        </div>

                        {/* Content */}
                        <div className={`flex-1 pb-1 ${isFuture ? 'opacity-50' : ''}`}>
                          <h3 className={`text-sm font-bold ${isCurrent ? 'text-primary' : 'text-foreground'}`}>
                            {phase.phaseName}
                            {isCurrent && (
                              <span className="ml-2 inline-block px-2 py-0.5 rounded-full bg-primary/10 text-primary text-[10px] font-bold uppercase tracking-wider">
                                Hiện tại
                              </span>
                            )}
                          </h3>
                          <p className="text-xs text-muted-foreground mt-0.5">
                            {new Date(phase.startDate).toLocaleDateString('vi-VN')} — {new Date(phase.endDate).toLocaleDateString('vi-VN')}
                          </p>
                          {phase.description && (
                            <p className="text-xs text-muted-foreground mt-1 line-clamp-2">{phase.description}</p>
                          )}
                        </div>
                      </div>

                      {/* Rounds under this phase */}
                      {phaseRounds.length > 0 && (
                        <div className="ml-[52px] mt-1 mb-2 space-y-2">
                          {phaseRounds.map((round, rIdx) => {
                            const roundStart = new Date(round.startDate)
                            const roundEnd = new Date(round.endDate)
                            const isRoundPast = roundEnd < now
                            const isRoundCurrent = now >= roundStart && now <= roundEnd
                            const isRoundFuture = roundStart > now
                            return (
                              <div key={round.id} className="flex items-start gap-3 p-2.5 rounded-lg bg-secondary/40 border border-border/50 hover:bg-secondary/60 transition-colors">
                                <div className={`w-7 h-7 rounded-full flex items-center justify-center shrink-0 ${
                                  isRoundPast ? 'bg-emerald-100 text-emerald-600' :
                                  isRoundCurrent ? 'bg-primary/10 text-primary' :
                                  'bg-gray-100 text-gray-400'
                                }`}>
                                  {isRoundPast ? <CheckCircle2 className="w-3.5 h-3.5" /> :
                                   isRoundCurrent ? <Clock className="w-3.5 h-3.5" /> :
                                   <Circle className="w-3.5 h-3.5" />}
                                </div>
                                <div className="flex-1 min-w-0">
                                  <p className="text-xs font-semibold">{round.roundName}</p>
                                  <p className="text-[10px] text-muted-foreground">
                                    {roundStart.toLocaleDateString('vi-VN')} — {roundEnd.toLocaleDateString('vi-VN')}
                                  </p>
                                </div>
                                {isRoundCurrent && (
                                  <span className="px-1.5 py-0.5 rounded-full bg-primary/10 text-primary text-[9px] font-bold uppercase shrink-0">
                                    Hiện tại
                                  </span>
                                )}
                              </div>
                            )
                          })}
                        </div>
                      )}
                    </div>
                  )
                })}
              </div>
            </div>
          )}
        </div>

        {/* Assessment Summary */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-border p-6">
          <div className="flex items-center gap-2 mb-5">
            <Award className="w-5 h-5 text-accent" />
            <h2 className="text-lg font-bold">Kết quả đánh giá</h2>
          </div>

          {results.length === 0 ? (
            <div className="text-center py-8 text-muted-foreground text-sm">
              <Target className="w-10 h-10 mx-auto text-muted-foreground/30 mb-2" />
              Chưa có kết quả đánh giá
            </div>
          ) : (
            <div className="space-y-4">
              {Object.keys(resultsByPhaseRound).map(pid => {
                const phaseName = phaseIdToName[parseInt(pid)] || 'Không xác định'
                const roundsData = resultsByPhaseRound[pid]
                return (
                  <div key={pid}>
                    <h3 className="text-sm font-bold text-foreground mb-2 flex items-center gap-2">
                      <GraduationCap className="w-4 h-4 text-primary" />
                      {phaseName}
                    </h3>
                    {Object.keys(roundsData).map(rid => {
                      const roundResults = roundsData[rid]
                      const roundName = roundResults[0]?.roundName || 'Vòng đánh giá'
                      const avgScore = roundResults.length > 0
                        ? (roundResults.reduce((s, r) => s + Number(r.score), 0) / roundResults.length).toFixed(1)
                        : null
                      const bestScore = roundResults.length > 0
                        ? Math.max(...roundResults.map(r => Number(r.score))).toFixed(1)
                        : null
                      return (
                        <div key={rid} className="mb-3 p-3 rounded-xl bg-secondary/30 border border-border/50">
                          <div className="flex items-center justify-between mb-2">
                            <p className="text-xs font-bold text-muted-foreground uppercase tracking-wider">
                              {roundName}
                            </p>
                            <div className="flex items-center gap-3 text-xs">
                              {avgScore && (
                                <span className="text-muted-foreground">
                                  TB: <span className="font-bold text-foreground">{avgScore}</span>
                                </span>
                              )}
                              {bestScore && (
                                <span className="text-muted-foreground">
                                  Cao nhất: <span className="font-bold text-accent">{bestScore}</span>
                                </span>
                              )}
                            </div>
                          </div>
                          <div className="space-y-1.5">
                            {roundResults.map(r => {
                              const badge = scoreBadge(r.score)
                              const BadgeIcon = badge?.icon
                              return (
                                <div key={r.id} className="flex items-center gap-2 py-1.5 px-2 rounded-lg hover:bg-secondary/60 transition-colors">
                                  <div className={`w-6 h-6 rounded flex items-center justify-center shrink-0 ${badge?.color || 'text-gray-500 bg-gray-100'}`}>
                                    {BadgeIcon ? <BadgeIcon className="w-3 h-3" /> : <Circle className="w-3 h-3" />}
                                  </div>
                                  <span className="text-xs flex-1 truncate">{r.criterionName}</span>
                                  {r.comments && (
                                    <span className="text-[10px] text-muted-foreground truncate hidden sm:inline max-w-[120px]">{r.comments}</span>
                                  )}
                                  <span className={`text-xs font-bold shrink-0 ${badge?.color?.split(' ')[0] || 'text-gray-500'}`}>
                                    {Number(r.score).toFixed(1)}
                                  </span>
                                </div>
                              )
                            })}
                          </div>
                        </div>
                      )
                    })}
                  </div>
                )
              })}
              <button
                onClick={() => navigate('/results')}
                className="w-full flex items-center justify-center gap-1 py-2 text-sm text-primary font-medium hover:bg-primary/5 rounded-lg transition-colors"
              >
                Xem tất cả kết quả
                <ArrowRight className="w-3.5 h-3.5" />
              </button>
            </div>
          )}
        </div>
      </div>

      {/* ===== CURRENT ASSIGNMENT DETAILS ===== */}
      {activeAssignment && (
        <div className="bg-white rounded-xl border border-border p-6">
          <div className="flex items-center gap-2 mb-4">
            <TrendingUp className="w-5 h-5 text-emerald-500" />
            <h2 className="text-lg font-bold">Phân công hiện tại</h2>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div className="p-4 rounded-xl bg-secondary/40">
              <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold mb-1">Đợt thực tập</p>
              <p className="text-sm font-bold">{activePhase?.phaseName || 'N/A'}</p>
              {activePhase && (
                <p className="text-xs text-muted-foreground mt-1">
                  {new Date(activePhase.startDate).toLocaleDateString('vi-VN')} — {new Date(activePhase.endDate).toLocaleDateString('vi-VN')}
                </p>
              )}
            </div>

            <div className="p-4 rounded-xl bg-secondary/40">
              <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold mb-1">Giảng viên hướng dẫn</p>
              <p className="text-sm font-bold">{activeAssignment.mentorName || 'Chưa phân công'}</p>
            </div>

            <div className="p-4 rounded-xl bg-secondary/40">
              <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold mb-1">Trạng thái</p>
              <span className={`inline-block px-2.5 py-1 rounded-full text-xs font-bold border ${statusConfig[activeAssignment.status]?.color || statusConfig.PENDING.color}`}>
                {statusConfig[activeAssignment.status]?.label || activeAssignment.status}
              </span>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

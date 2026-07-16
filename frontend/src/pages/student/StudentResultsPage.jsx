import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '@/context/AuthContext'
import { resultApi, roundApi, phaseApi } from '@/services/api'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Award, CheckCircle2, AlertCircle, Circle, TrendingUp, Target, GraduationCap } from 'lucide-react'

function scoreBadge(score) {
  const n = Number(score)
  if (n >= 8) return { label: 'Tốt', color: 'text-emerald-600 bg-emerald-50 border-emerald-200', icon: CheckCircle2 }
  if (n >= 5) return { label: 'Đạt', color: 'text-amber-600 bg-amber-50 border-amber-200', icon: AlertCircle }
  return { label: 'Kém', color: 'text-red-600 bg-red-50 border-red-200', icon: Circle }
}

export default function StudentResultsPage() {
  const { user } = useAuth()
  const [rounds, setRounds] = useState([])
  const [results, setResults] = useState([])
  const [phases, setPhases] = useState([])
  const [loading, setLoading] = useState(true)
  const abortRef = useRef(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const [roundRes, resultRes, phaseRes] = await Promise.allSettled([
        roundApi.getAll({ page: 0, size: 100, signal: abortRef.current?.signal }),
        resultApi.getAll({ studentId: user.userId, page: 0, size: 200, signal: abortRef.current?.signal }),
        phaseApi.getAll({ page: 0, size: 50, signal: abortRef.current?.signal }),
      ])

      let allRounds = []
      let allResults = []
      let allPhases = []

      if (roundRes.status === 'fulfilled') {
        allRounds = roundRes.value?.data?.data?.items || roundRes.value?.data?.data || []
      }
      if (resultRes.status === 'fulfilled') {
        allResults = resultRes.value?.data?.data?.items || resultRes.value?.data?.data || []
      }
      if (phaseRes.status === 'fulfilled') {
        allPhases = phaseRes.value?.data?.data?.items || phaseRes.value?.data?.data || []
      }

      setRounds(allRounds)
      setResults(allResults)
      setPhases(allPhases)
    } catch (err) {
      if (err.name !== 'AbortError') console.error('Failed to load results:', err)
    } finally {
      setLoading(false)
    }
  }, [user.userId])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchData()
    return () => { abortRef.current?.abort() }
  }, [fetchData])

  // Lookup maps
  const roundIdToPhaseId = {}
  rounds.forEach(r => { roundIdToPhaseId[r.id] = r.phaseId })
  const phaseIdToName = {}
  phases.forEach(p => { phaseIdToName[p.id] = p.phaseName })

  // Group results by phase -> round
  const byPhase = {}
  results.forEach(r => {
    const pid = roundIdToPhaseId[r.roundId]
    if (!pid) return
    if (!byPhase[pid]) byPhase[pid] = { phaseName: phaseIdToName[pid] || 'Không xác định', rounds: {} }
    if (!byPhase[pid].rounds[r.roundId]) {
      byPhase[pid].rounds[r.roundId] = {
        roundName: r.roundName || 'Vòng đánh giá',
        roundDescription: rounds.find(rd => rd.id === r.roundId)?.description || '',
        items: []
      }
    }
    byPhase[pid].rounds[r.roundId].items.push(r)
  })

  // Also catch orphan results (no phase mapping)
  const orphanResults = results.filter(r => !roundIdToPhaseId[r.roundId])
  if (orphanResults.length > 0) {
    const orphanKey = '__orphan__'
    byPhase[orphanKey] = { phaseName: 'Khác', rounds: { 0: { roundName: 'Chưa phân loại', roundDescription: '', items: orphanResults } } }
  }

  // Compute summaries
  const phaseSummaries = Object.keys(byPhase).map(pid => {
    const phaseData = byPhase[pid]
    const roundSummaries = Object.keys(phaseData.rounds).map(rid => {
      const roundData = phaseData.rounds[rid]
      const scores = roundData.items.map(i => Number(i.score)).filter(s => !isNaN(s))
      const avg = scores.length > 0 ? (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : null
      const total = scores.length > 0 ? scores.reduce((a, b) => a + b, 0).toFixed(1) : null
      const max = scores.length > 0 ? Math.max(...scores).toFixed(1) : null
      const min = scores.length > 0 ? Math.min(...scores).toFixed(1) : null
      return { ...roundData, avg, total, max, min }
    })
    const allScores = roundSummaries.flatMap(rs => rs.items.map(i => Number(i.score))).filter(s => !isNaN(s))
    const phaseAvg = allScores.length > 0 ? (allScores.reduce((a, b) => a + b, 0) / allScores.length).toFixed(1) : null
    return { pid, phaseName: phaseData.phaseName, roundSummaries, phaseAvg, totalItems: allScores.length }
  }).filter(p => p.roundSummaries.length > 0)

  if (loading) return <LoadingSpinner text="Đang tải kết quả đánh giá..." />

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Kết quả đánh giá</h1>
        <p className="text-muted-foreground text-sm">Theo dõi điểm số và nhận xét từ giảng viên hướng dẫn</p>
      </div>

      {phaseSummaries.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-12 text-center">
          <Target className="w-16 h-16 mx-auto text-muted-foreground/30 mb-4" />
          <p className="text-sm text-muted-foreground font-medium">Chưa có kết quả đánh giá nào</p>
          <p className="text-xs text-muted-foreground/70 mt-1">Kết quả sẽ xuất hiện khi giảng viên chấm điểm cho bạn</p>
        </div>
      ) : (
        <div className="space-y-6">
          {phaseSummaries.map(({ pid, phaseName, roundSummaries, phaseAvg, totalItems }) => (
            <div key={pid}>
              {/* Phase header */}
              <div className="flex items-center gap-2 mb-4">
                <GraduationCap className="w-5 h-5 text-primary" />
                <h2 className="text-base font-bold text-foreground">{phaseName}</h2>
                {phaseAvg && (
                  <span className="text-xs text-muted-foreground ml-2">
                    ({totalItems} điểm · TB đợt: <span className="font-bold text-primary">{phaseAvg}</span>)
                  </span>
                )}
              </div>

              <div className="space-y-4">
                {roundSummaries.map(({ roundName, roundDescription, items, avg, total, max, min }) => (
                  <div key={roundName} className="bg-white rounded-xl border border-border overflow-hidden">
                    {/* Round Header */}
                    <div className="bg-gradient-to-r from-primary/5 to-transparent p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                      <div>
                        <h3 className="text-base font-bold">{roundName}</h3>
                        {roundDescription && (
                          <p className="text-xs text-muted-foreground mt-0.5">{roundDescription}</p>
                        )}
                      </div>
                      <div className="flex items-center gap-4">
                        {avg && (
                          <div className="text-center">
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">Trung bình</p>
                            <p className={`text-lg font-bold ${Number(avg) >= 8 ? 'text-emerald-600' : Number(avg) >= 5 ? 'text-amber-600' : 'text-red-600'}`}>
                              {avg}
                            </p>
                          </div>
                        )}
                        {max && (
                          <div className="text-center">
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">Cao nhất</p>
                            <p className="text-lg font-bold text-emerald-600">{max}</p>
                          </div>
                        )}
                        {min && (
                          <div className="text-center">
                            <p className="text-[10px] text-muted-foreground uppercase tracking-wider font-semibold">Thấp nhất</p>
                            <p className="text-lg font-bold text-red-600">{min}</p>
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Criteria Scores */}
                    <div className="p-5 grid grid-cols-1 sm:grid-cols-2 gap-3">
                      {items.map((r) => {
                        const badge = scoreBadge(r.score)
                        const BadgeIcon = badge.icon
                        return (
                          <div key={r.id} className="flex items-start gap-3 p-3 rounded-xl bg-secondary/30 hover:bg-secondary/50 transition-colors">
                            <div className={`w-10 h-10 rounded-lg flex items-center justify-center shrink-0 border ${badge.color}`}>
                              <BadgeIcon className="w-4 h-4" />
                            </div>
                            <div className="flex-1 min-w-0">
                              <p className="text-sm font-semibold">{r.criterionName}</p>
                              {r.comments && (
                                <p className="text-xs text-muted-foreground mt-0.5 line-clamp-2">{r.comments}</p>
                              )}
                              {r.evaluatedByName && (
                                <p className="text-[10px] text-muted-foreground/70 mt-1">Người chấm: {r.evaluatedByName}</p>
                              )}
                            </div>
                            <div className="text-right shrink-0">
                              <span className={`text-lg font-bold ${badge.color.split(' ')[0]}`}>
                                {Number(r.score).toFixed(1)}
                              </span>
                              <span className={`block text-[10px] font-semibold px-1.5 py-0.5 rounded-full mt-0.5 border ${badge.color}`}>
                                {badge.label}
                              </span>
                            </div>
                          </div>
                        )
                      })}
                    </div>
                  </div>
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

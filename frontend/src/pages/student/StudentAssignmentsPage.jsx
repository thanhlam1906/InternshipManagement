import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '@/context/AuthContext'
import { assignmentApi, phaseApi, mentorApi } from '@/services/api'
import LoadingSpinner from '@/components/LoadingSpinner'
import { ClipboardList, Clock, Users, Calendar, MapPin, ChevronDown, ChevronUp, Filter } from 'lucide-react'

const statusConfig = {
  PENDING: { label: 'Chờ xử lý', color: 'bg-amber-50 text-amber-700 border-amber-200', dot: 'bg-amber-500' },
  IN_PROGRESS: { label: 'Đang thực tập', color: 'bg-blue-50 text-blue-700 border-blue-200', dot: 'bg-blue-500' },
  COMPLETED: { label: 'Hoàn thành', color: 'bg-emerald-50 text-emerald-700 border-emerald-200', dot: 'bg-emerald-500' },
  CANCELLED: { label: 'Đã hủy', color: 'bg-red-50 text-red-700 border-red-200', dot: 'bg-red-500' },
}

const FILTER_TABS = [
  { key: 'ALL', label: 'Tất cả' },
  { key: 'IN_PROGRESS', label: 'Đang thực tập' },
  { key: 'COMPLETED', label: 'Hoàn thành' },
  { key: 'PENDING', label: 'Chờ xử lý' },
]

export default function StudentAssignmentsPage() {
  const { user } = useAuth()
  const [assignments, setAssignments] = useState([])
  const [phases, setPhases] = useState([])
  const [mentors, setMentors] = useState([])
  const [loading, setLoading] = useState(true)
  const [filter, setFilter] = useState('ALL')
  const [expandedId, setExpandedId] = useState(null)
  const abortRef = useRef(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const [assignRes, phaseRes, mentorRes] = await Promise.allSettled([
        assignmentApi.getAll({ studentId: user.userId, page: 0, size: 50, signal: abortRef.current?.signal }),
        phaseApi.getAll({ page: 0, size: 50, signal: abortRef.current?.signal }),
        mentorApi.getAll({ signal: abortRef.current?.signal }),
      ])

      if (assignRes.status === 'fulfilled') {
        setAssignments(assignRes.value?.data?.data?.items || assignRes.value?.data?.data || [])
      }
      if (phaseRes.status === 'fulfilled') {
        setPhases(phaseRes.value?.data?.data?.items || phaseRes.value?.data?.data || [])
      }
      if (mentorRes.status === 'fulfilled') {
        setMentors(mentorRes.value?.data?.data?.items || mentorRes.value?.data?.data || mentorRes.value?.data || [])
      }
    } catch (err) {
      if (err.name !== 'AbortError') console.error('Failed to load assignments:', err)
    } finally {
      setLoading(false)
    }
  }, [user.userId])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchData()
    return () => { abortRef.current?.abort() }
  }, [fetchData])

  const phaseMap = new Map(phases.map(p => [p.id, p]))
  const mentorMap = new Map(mentors.map(m => [m.id, m]))

  const filtered = filter === 'ALL'
    ? assignments
    : assignments.filter(a => a.status === filter)

  if (loading) return <LoadingSpinner text="Đang tải danh sách phân công..." />

  const toggleExpand = (id) => setExpandedId(expandedId === id ? null : id)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Phân công thực tập</h1>
        <p className="text-muted-foreground text-sm">Danh sách các đợt thực tập bạn được phân công</p>
      </div>

      {/* Filter Tabs */}
      <div className="flex items-center gap-2 flex-wrap">
        {FILTER_TABS.map(tab => (
          <button
            key={tab.key}
            onClick={() => setFilter(tab.key)}
            className={`px-3 py-1.5 rounded-full text-xs font-semibold transition-all ${
              filter === tab.key
                ? 'bg-primary text-white shadow-sm'
                : 'bg-white text-muted-foreground hover:bg-secondary border border-border'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-12 text-center">
          <ClipboardList className="w-16 h-16 mx-auto text-muted-foreground/30 mb-4" />
          <p className="text-sm text-muted-foreground font-medium">Không có phân công nào</p>
          <p className="text-xs text-muted-foreground/70 mt-1">
            {filter !== 'ALL' ? 'Thử chọn bộ lọc khác' : 'Bạn chưa được phân công vào đợt thực tập nào'}
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filtered.map((a) => {
            const phase = phaseMap.get(a.phaseId)
            const mentor = mentorMap.get(a.mentorId)
            const status = statusConfig[a.status] || statusConfig.PENDING
            const isExpanded = expandedId === a.id

            return (
              <div
                key={a.id}
                className="bg-white rounded-xl border border-border hover:shadow-md transition-all overflow-hidden"
              >
                <button
                  onClick={() => toggleExpand(a.id)}
                  className="w-full p-5 text-left"
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2">
                        <span className={`w-2.5 h-2.5 rounded-full ${status.dot}`} />
                        <h3 className="font-bold text-base truncate">{phase?.phaseName || `Đợt #${a.phaseId}`}</h3>
                      </div>
                      <p className="text-xs text-muted-foreground mt-1">
                        Giảng viên: <span className="font-medium text-foreground">{a.mentorName || mentor?.fullName || mentor?.user?.fullName || 'Chưa rõ'}</span>
                      </p>
                      <div className="flex items-center gap-3 mt-3">
                        <span className={`inline-block px-2.5 py-1 rounded-full text-[10px] font-bold border ${status.color}`}>
                          {status.label}
                        </span>
                        <span className="text-xs text-muted-foreground">
                          {new Date(a.assignedDate).toLocaleDateString('vi-VN')}
                        </span>
                      </div>
                    </div>
                    <div className="shrink-0 text-muted-foreground">
                      {isExpanded ? <ChevronUp className="w-5 h-5" /> : <ChevronDown className="w-5 h-5" />}
                    </div>
                  </div>
                </button>

                {/* Expanded Details */}
                {isExpanded && (
                  <div className="border-t border-border px-5 py-4 bg-secondary/20 space-y-3 animate-in fade-in slide-in-from-top-2 duration-200">
                    {phase && (
                      <>
                        <div className="flex items-start gap-2.5">
                          <Calendar className="w-4 h-4 text-muted-foreground shrink-0 mt-0.5" />
                          <div>
                            <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Thời gian</p>
                            <p className="text-sm">
                              {new Date(phase.startDate).toLocaleDateString('vi-VN')} — {new Date(phase.endDate).toLocaleDateString('vi-VN')}
                            </p>
                          </div>
                        </div>
                        {phase.description && (
                          <div className="flex items-start gap-2.5">
                            <MapPin className="w-4 h-4 text-muted-foreground shrink-0 mt-0.5" />
                            <div>
                              <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Mô tả</p>
                              <p className="text-sm text-muted-foreground">{phase.description}</p>
                            </div>
                          </div>
                        )}
                      </>
                    )}
                    {mentor && (
                      <div className="flex items-start gap-2.5">
                        <Users className="w-4 h-4 text-muted-foreground shrink-0 mt-0.5" />
                        <div>
                          <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Giảng viên hướng dẫn</p>
                          <p className="text-sm font-medium">
                            {mentor.fullName || mentor.user?.fullName || '--'}
                            {mentor.department && <span className="text-muted-foreground"> • {mentor.department}</span>}
                          </p>
                          {mentor.academicRank && (
                            <p className="text-xs text-muted-foreground">{mentor.academicRank}</p>
                          )}
                        </div>
                      </div>
                    )}
                    <div className="flex items-start gap-2.5">
                      <Clock className="w-4 h-4 text-muted-foreground shrink-0 mt-0.5" />
                      <div>
                        <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Ngày phân công</p>
                        <p className="text-sm">{new Date(a.assignedDate).toLocaleDateString('vi-VN')}</p>
                      </div>
                    </div>
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

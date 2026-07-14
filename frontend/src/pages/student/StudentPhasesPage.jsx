import { useState, useEffect, useCallback, useRef } from 'react'
import { phaseApi, assignmentApi } from '@/services/api'
import { useAuth } from '@/context/AuthContext'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Calendar, CheckCircle2, Clock, Lock, Circle } from 'lucide-react'

export default function StudentPhasesPage() {
  const { user } = useAuth()
  const [phases, setPhases] = useState([])
  const [assignments, setAssignments] = useState([])
  const [loading, setLoading] = useState(true)
  const abortRef = useRef(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const [phaseRes, assignRes] = await Promise.allSettled([
        phaseApi.getAll({ page: 0, size: 50, signal: abortRef.current?.signal }),
        assignmentApi.getAll({ studentId: user.userId, page: 0, size: 50, signal: abortRef.current?.signal }),
      ])

      let allPhases = []
      let allAssignments = []

      if (phaseRes.status === 'fulfilled') {
        allPhases = phaseRes.value?.data?.data?.items || phaseRes.value?.data?.data || []
      }
      if (assignRes.status === 'fulfilled') {
        allAssignments = assignRes.value?.data?.data?.items || assignRes.value?.data?.data || []
      }

      setPhases(allPhases.sort((a, b) => new Date(a.startDate) - new Date(b.startDate)))
      setAssignments(allAssignments)
    } catch (err) {
      if (err.name !== 'AbortError') console.error('Failed to load phases:', err)
    } finally {
      setLoading(false)
    }
  }, [user.userId])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchData()
    return () => { abortRef.current?.abort() }
  }, [fetchData])

  if (loading) return <LoadingSpinner text="Đang tải danh sách đợt thực tập..." />

  // Determine which phases the student is assigned to
  const assignedPhaseIds = new Set(assignments.map(a => a.phaseId))

  // Find current active phase
  const now = new Date()
  let currentPhaseId = null

  // First check: any assignment IN_PROGRESS
  const activeAssignment = assignments.find(a => a.status === 'IN_PROGRESS')
  if (activeAssignment) {
    currentPhaseId = activeAssignment.phaseId
  } else {
    // Fallback: find the phase whose date range contains today
    const activePhase = phases.find(p => {
      const start = new Date(p.startDate)
      const end = new Date(p.endDate)
      return now >= start && now <= end
    })
    if (activePhase) currentPhaseId = activePhase.id
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Các đợt thực tập</h1>
        <p className="text-muted-foreground text-sm">Lịch trình các đợt thực tập trong chương trình</p>
      </div>

      {phases.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-12 text-center">
          <Calendar className="w-16 h-16 mx-auto text-muted-foreground/30 mb-4" />
          <p className="text-sm text-muted-foreground font-medium">Chưa có đợt thực tập nào</p>
        </div>
      ) : (
        <div className="max-w-2xl">
          <div className="relative">
            {/* Timeline vertical line */}
            <div className="absolute left-[23px] top-4 bottom-4 w-0.5 bg-border" />

            <div className="space-y-6">
              {phases.map((phase, idx) => {
                const isAssigned = assignedPhaseIds.has(phase.id)
                const isCurrent = phase.id === currentPhaseId
                const phaseStart = new Date(phase.startDate)
                const phaseEnd = new Date(phase.endDate)
                const isPast = phaseEnd < now
                const isFuture = phaseStart > now

                return (
                  <div key={phase.id} className="relative flex gap-5 pl-1">
                    {/* Timeline Node */}
                    <div className={`relative z-10 w-11 h-11 rounded-full flex items-center justify-center shrink-0 border-2 transition-all ${
                      isCurrent
                        ? 'bg-primary border-primary text-white shadow-lg shadow-primary/30'
                        : isPast && isAssigned
                          ? 'bg-emerald-100 border-emerald-400 text-emerald-600'
                          : isFuture || !isAssigned
                            ? 'bg-gray-100 border-gray-300 text-gray-400'
                            : 'bg-blue-100 border-blue-300 text-blue-600'
                    }`}>
                      {isPast && isAssigned ? (
                        <CheckCircle2 className="w-5 h-5" />
                      ) : isFuture || !isAssigned ? (
                        <Lock className="w-4 h-4" />
                      ) : isCurrent ? (
                        <div className="relative">
                          <Circle className="w-5 h-5 animate-pulse" />
                        </div>
                      ) : (
                        <Clock className="w-4 h-4" />
                      )}
                    </div>

                    {/* Content */}
                    <div className={`flex-1 bg-white rounded-xl border p-5 transition-all ${
                      isCurrent
                        ? 'border-primary/30 shadow-md shadow-primary/5'
                        : isFuture || !isAssigned
                          ? 'border-border opacity-60'
                          : 'border-border'
                    }`}>
                      <div className="flex items-start justify-between gap-3">
                        <div>
                          <h3 className={`text-base font-bold ${isCurrent ? 'text-primary' : 'text-foreground'}`}>
                            {phase.phaseName}
                          </h3>
                          <div className="flex items-center gap-1.5 mt-1 text-xs text-muted-foreground">
                            <Calendar className="w-3.5 h-3.5" />
                            <span>
                              {phaseStart.toLocaleDateString('vi-VN')} — {phaseEnd.toLocaleDateString('vi-VN')}
                            </span>
                          </div>
                        </div>

                        {/* Status Badges */}
                        <div className="flex flex-col items-end gap-1.5 shrink-0">
                          {isCurrent && (
                            <span className="px-2.5 py-1 rounded-full bg-primary/10 text-primary text-[10px] font-bold uppercase tracking-wider animate-pulse">
                              Đang diễn ra
                            </span>
                          )}
                          {isPast && isAssigned && (
                            <span className="px-2.5 py-1 rounded-full bg-emerald-50 text-emerald-600 text-[10px] font-bold uppercase tracking-wider border border-emerald-200">
                              Đã hoàn thành
                            </span>
                          )}
                          {isFuture && (
                            <span className="px-2.5 py-1 rounded-full bg-gray-100 text-gray-500 text-[10px] font-bold uppercase tracking-wider border border-gray-200">
                              Sắp diễn ra
                            </span>
                          )}
                          {!isAssigned && (
                            <span className="px-2.5 py-1 rounded-full bg-gray-100 text-gray-400 text-[10px] font-bold uppercase tracking-wider border border-gray-200">
                              Chưa phân công
                            </span>
                          )}
                        </div>
                      </div>

                      {phase.description && (
                        <p className="text-sm text-muted-foreground mt-3 leading-relaxed">{phase.description}</p>
                      )}

                      {/* Duration indicator */}
                      <div className="mt-4 flex items-center gap-2">
                        <div className="flex-1 h-1.5 rounded-full bg-secondary overflow-hidden">
                          <div
                            className={`h-full rounded-full transition-all ${
                              isPast && isAssigned ? 'bg-emerald-400' : isCurrent ? 'bg-primary' : 'bg-gray-200'
                            }`}
                            style={{
                              width: isPast && isAssigned ? '100%'
                                : isCurrent ? `${Math.max(5, Math.min(95, ((now - phaseStart) / (phaseEnd - phaseStart)) * 100))}%`
                                : isFuture ? '0%' : '30%'
                            }}
                          />
                        </div>
                        <span className="text-[10px] text-muted-foreground font-medium shrink-0">
                          {isPast && isAssigned ? '100%' : isCurrent ? 'Đang tiến hành' : isFuture ? '0%' : '--'}
                        </span>
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

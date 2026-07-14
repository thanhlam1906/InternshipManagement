import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '@/context/AuthContext'
import { resultApi, roundApi } from '@/services/api'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Award, CheckCircle2, AlertCircle, Circle, TrendingUp, Target } from 'lucide-react'

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
  const [loading, setLoading] = useState(true)
  const abortRef = useRef(null)

  const fetchData = useCallback(async () => {
    setLoading(true)
    try {
      const [roundRes, resultRes] = await Promise.allSettled([
        roundApi.getAll({ page: 0, size: 50, signal: abortRef.current?.signal }),
        resultApi.getAll({ studentId: user.userId, page: 0, size: 200, signal: abortRef.current?.signal }),
      ])

      let allRounds = []
      let allResults = []

      if (roundRes.status === 'fulfilled') {
        allRounds = roundRes.value?.data?.data?.items || roundRes.value?.data?.data || []
      }
      if (resultRes.status === 'fulfilled') {
        allResults = resultRes.value?.data?.data?.items || resultRes.value?.data?.data || []
      }

      setRounds(allRounds)
      setResults(allResults)
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

  // Group results by round
  const resultsByRound = rounds.map(round => ({
    round,
    items: results.filter(r => r.roundId === round.id || r.roundName === round.roundName),
  })).filter(g => g.items.length > 0)

  // Also show orphan results (no matching round)
  const orphanResults = results.filter(r => !rounds.some(rd => rd.id === r.roundId))
  if (orphanResults.length > 0) {
    resultsByRound.push({ round: { id: 0, roundName: 'Khác', description: '' }, items: orphanResults })
  }

  // Compute round summaries
  const summaries = resultsByRound.map(g => {
    const scores = g.items.map(i => Number(i.score)).filter(s => !isNaN(s))
    const avg = scores.length > 0 ? (scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : null
    const total = scores.length > 0 ? scores.reduce((a, b) => a + b, 0).toFixed(1) : null
    return { ...g, avg, total }
  })

  if (loading) return <LoadingSpinner text="Đang tải kết quả đánh giá..." />

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Kết quả đánh giá</h1>
        <p className="text-muted-foreground text-sm">Theo dõi điểm số và nhận xét từ giảng viên hướng dẫn</p>
      </div>

      {summaries.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-12 text-center">
          <Target className="w-16 h-16 mx-auto text-muted-foreground/30 mb-4" />
          <p className="text-sm text-muted-foreground font-medium">Chưa có kết quả đánh giá nào</p>
          <p className="text-xs text-muted-foreground/70 mt-1">Kết quả sẽ xuất hiện khi giảng viên chấm điểm cho bạn</p>
        </div>
      ) : (
        <div className="space-y-6">
          {summaries.map(({ round, items, avg, total }) => (
            <div key={round.id} className="bg-white rounded-xl border border-border overflow-hidden">
              {/* Round Header */}
              <div className="bg-gradient-to-r from-primary/5 to-transparent p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
                <div>
                  <h2 className="text-lg font-bold">{round.roundName}</h2>
                  {round.description && (
                    <p className="text-xs text-muted-foreground mt-0.5">{round.description}</p>
                  )}
                </div>
                <div className="flex items-center gap-4">
                  {avg && (
                    <div className="text-center">
                      <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Trung bình</p>
                      <p className={`text-xl font-bold ${Number(avg) >= 8 ? 'text-emerald-600' : Number(avg) >= 5 ? 'text-amber-600' : 'text-red-600'}`}>
                        {avg}
                      </p>
                    </div>
                  )}
                  {total && (
                    <div className="text-center">
                      <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Tổng</p>
                      <p className="text-xl font-bold text-primary">{total}</p>
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
      )}
    </div>
  )
}

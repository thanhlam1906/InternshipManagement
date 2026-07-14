import { useState, useEffect, useCallback, useRef } from 'react'
import { mentorApi } from '@/services/api'
import LoadingSpinner from '@/components/LoadingSpinner'
import { UserCheck, GraduationCap, Building, Mail, Phone } from 'lucide-react'

export default function StudentMentorsPage() {
  const [mentors, setMentors] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedMentor, setSelectedMentor] = useState(null)
  const abortRef = useRef(null)

  const fetchMentors = useCallback(async () => {
    setLoading(true)
    try {
      const res = await mentorApi.getAll({ signal: abortRef.current?.signal })
      const data = res.data?.data?.items || res.data?.data || res.data || []
      setMentors(Array.isArray(data) ? data : [])
    } catch (err) {
      if (err.name !== 'AbortError') console.error('Failed to load mentors:', err)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchMentors()
    return () => { abortRef.current?.abort() }
  }, [fetchMentors])

  if (loading) return <LoadingSpinner text="Đang tải danh sách giảng viên..." />

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Giảng viên hướng dẫn</h1>
        <p className="text-muted-foreground text-sm">Danh sách giảng viên tham gia hướng dẫn thực tập</p>
      </div>

      {mentors.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-12 text-center">
          <UserCheck className="w-16 h-16 mx-auto text-muted-foreground/30 mb-4" />
          <p className="text-sm text-muted-foreground font-medium">Chưa có giảng viên nào</p>
        </div>
      ) : (
        <>
          {/* Grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {mentors.map((m) => {
              const name = m.fullName || m.user?.fullName || 'Chưa rõ'
              const email = m.email || m.user?.email || ''
              const phone = m.phoneNumber || m.user?.phoneNumber || ''
              const initials = name
                .split(' ')
                .map(n => n[0])
                .slice(0, 2)
                .join('')
                .toUpperCase()

              return (
                <button
                  key={m.id}
                  onClick={() => setSelectedMentor(selectedMentor?.id === m.id ? null : m)}
                  className={`bg-white rounded-xl border p-5 text-left hover:shadow-md transition-all ${
                    selectedMentor?.id === m.id ? 'border-primary shadow-md ring-1 ring-primary/20' : 'border-border'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-gradient-to-br from-accent to-primary flex items-center justify-center text-white font-bold text-sm shadow-md shrink-0">
                      {initials}
                    </div>
                    <div className="min-w-0">
                      <h3 className="font-bold text-sm truncate">{name}</h3>
                      <p className="text-xs text-muted-foreground">{m.academicRank || 'Giảng viên'}</p>
                    </div>
                  </div>

                  {m.department && (
                    <div className="flex items-center gap-1.5 mt-3 text-xs text-muted-foreground">
                      <Building className="w-3.5 h-3.5" />
                      <span className="truncate">{m.department}</span>
                    </div>
                  )}

                  {/* Expanded info */}
                  {selectedMentor?.id === m.id && (
                    <div className="mt-4 pt-4 border-t border-border space-y-2 animate-in fade-in slide-in-from-top-2 duration-200">
                      {email && (
                        <div className="flex items-center gap-2 text-xs">
                          <Mail className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
                          <span className="text-muted-foreground truncate">{email}</span>
                        </div>
                      )}
                      {phone && (
                        <div className="flex items-center gap-2 text-xs">
                          <Phone className="w-3.5 h-3.5 text-muted-foreground shrink-0" />
                          <span className="text-muted-foreground">{phone}</span>
                        </div>
                      )}
                      {m.createdAt && (
                        <p className="text-[10px] text-muted-foreground/70 mt-2">
                          Tham gia từ: {new Date(m.createdAt).toLocaleDateString('vi-VN')}
                        </p>
                      )}
                    </div>
                  )}
                </button>
              )
            })}
          </div>
        </>
      )}
    </div>
  )
}

import { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '@/context/AuthContext'
import { studentApi, userApi } from '@/services/api'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { User, GraduationCap, Mail, Phone, MapPin, Calendar, Edit, Hash, BookOpen } from 'lucide-react'
import toast from 'react-hot-toast'

export default function StudentProfilePage() {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [userDetails, setUserDetails] = useState(null)
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [formLoading, setFormLoading] = useState(false)
  const [form, setForm] = useState({ studentCode: '', major: '', clazz: '', dateOfBirth: '', address: '' })
  const abortRef = useRef(null)

  const fetchProfile = useCallback(async () => {
    setLoading(true)
    try {
      const [studentRes, userRes] = await Promise.allSettled([
        studentApi.getAll({ signal: abortRef.current?.signal }).then(r => {
          const all = r.data.data.items || []
          return all.find(s => s.userId === user.userId) || null
        }),
        userApi.getById(user.userId, { signal: abortRef.current?.signal }),
      ])
      if (studentRes.status === 'fulfilled') setProfile(studentRes.value)
      if (userRes.status === 'fulfilled') setUserDetails(userRes.value?.data?.data || userRes.value?.data || null)
    } catch (err) {
      if (err.name !== 'AbortError') toast.error('Không thể tải thông tin hồ sơ')
    } finally {
      setLoading(false)
    }
  }, [user.userId])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchProfile()
    return () => { abortRef.current?.abort() }
  }, [fetchProfile])

  const openEdit = () => {
    setForm({
      studentCode: profile?.studentCode || '',
      major: profile?.major || '',
      clazz: profile?.clazz || '',
      dateOfBirth: profile?.dateOfBirth || '',
      address: profile?.address || '',
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      const studentId = profile?.id || user.userId
      await studentApi.update(studentId, {
        studentCode: form.studentCode,
        major: form.major,
        clazz: form.clazz,
        dateOfBirth: form.dateOfBirth || null,
        address: form.address,
      })
      toast.success('Cập nhật hồ sơ thành công!')
      setFormOpen(false)
      fetchProfile()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  if (loading) return <LoadingSpinner text="Đang tải hồ sơ..." />

  const initials = user?.fullName
    ?.split(' ')
    .map(n => n[0])
    .slice(0, 2)
    .join('')
    .toUpperCase() || 'U'

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Hồ sơ cá nhân</h1>
          <p className="text-muted-foreground text-sm">Quản lý thông tin cá nhân và học tập của bạn</p>
        </div>
        <button
          onClick={openEdit}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20"
        >
          <Edit className="w-4 h-4" />
          Chỉnh sửa
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Left Column — Personal Info */}
        <div className="lg:col-span-3 bg-white rounded-xl border border-border overflow-hidden">
          {/* Header */}
          <div className="bg-gradient-to-r from-primary/5 to-accent/5 p-6 flex items-center gap-4">
            <div className="w-16 h-16 rounded-full bg-gradient-to-br from-primary to-accent flex items-center justify-center text-white text-xl font-bold shadow-lg shrink-0">
              {initials}
            </div>
            <div>
              <h2 className="text-lg font-bold">{user?.fullName}</h2>
              <p className="text-sm text-muted-foreground">@{user?.username}</p>
              <span className="inline-block mt-1 px-2 py-0.5 rounded-full bg-primary/10 text-primary text-[10px] font-bold uppercase tracking-wider">
                Sinh viên
              </span>
            </div>
          </div>

          {/* Info Grid */}
          <div className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div className="flex items-start gap-3 p-3 rounded-xl bg-secondary/30">
              <Mail className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Email</p>
                <p className="text-sm font-medium mt-0.5">{userDetails?.email || '--'}</p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-3 rounded-xl bg-secondary/30">
              <Phone className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Số điện thoại</p>
                <p className="text-sm font-medium mt-0.5">{userDetails?.phoneNumber || '--'}</p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-3 rounded-xl bg-secondary/30">
              <Calendar className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Ngày sinh</p>
                <p className="text-sm font-medium mt-0.5">
                  {profile?.dateOfBirth
                    ? new Date(profile.dateOfBirth).toLocaleDateString('vi-VN')
                    : '--'}
                </p>
              </div>
            </div>

            <div className="flex items-start gap-3 p-3 rounded-xl bg-secondary/30">
              <MapPin className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
              <div>
                <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Địa chỉ</p>
                <p className="text-sm font-medium mt-0.5">{profile?.address || '--'}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Right Column — Academic Info */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-xl border border-border p-6">
            <div className="flex items-center gap-2 mb-4">
              <GraduationCap className="w-5 h-5 text-primary" />
              <h3 className="font-bold">Thông tin học tập</h3>
            </div>

            <div className="space-y-4">
              <div className="flex items-start gap-3">
                <Hash className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Mã sinh viên</p>
                  <p className="text-sm font-bold">{profile?.studentCode || '--'}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <BookOpen className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Ngành học</p>
                  <p className="text-sm font-bold">{profile?.major || '--'}</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <User className="w-5 h-5 text-muted-foreground shrink-0 mt-0.5" />
                <div>
                  <p className="text-xs text-muted-foreground uppercase tracking-wider font-semibold">Lớp</p>
                  <p className="text-sm font-bold">{profile?.clazz || '--'}</p>
                </div>
              </div>
            </div>
          </div>

          {/* Account Status Card */}
          <div className="bg-white rounded-xl border border-border p-6">
            <h3 className="font-bold mb-3">Trạng thái tài khoản</h3>
            <div className="flex items-center gap-3">
              <div className={`w-3 h-3 rounded-full ${userDetails?.isActive !== false ? 'bg-emerald-500' : 'bg-red-500'}`} />
              <span className="text-sm font-medium">
                {userDetails?.isActive !== false ? 'Đang hoạt động' : 'Đã vô hiệu hóa'}
              </span>
            </div>
            <p className="text-xs text-muted-foreground mt-3">
              Tham gia từ: {userDetails?.createdAt
                ? new Date(userDetails.createdAt).toLocaleDateString('vi-VN')
                : '--'}
            </p>
          </div>
        </div>
      </div>

      {/* Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title="Chỉnh sửa hồ sơ"
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        <div>
          <label className="block text-sm font-medium mb-1">Mã sinh viên</label>
          <input
            type="text"
            value={form.studentCode}
            onChange={e => setForm({ ...form, studentCode: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
            maxLength={50}
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Ngành học</label>
          <input
            type="text"
            value={form.major}
            onChange={e => setForm({ ...form, major: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Lớp</label>
          <input
            type="text"
            value={form.clazz}
            onChange={e => setForm({ ...form, clazz: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Ngày sinh</label>
          <input
            type="date"
            value={form.dateOfBirth}
            onChange={e => setForm({ ...form, dateOfBirth: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Địa chỉ</label>
          <textarea
            value={form.address}
            onChange={e => setForm({ ...form, address: e.target.value })}
            maxLength={500}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 min-h-[80px]"
          />
        </div>
      </FormDialog>
    </div>
  )
}

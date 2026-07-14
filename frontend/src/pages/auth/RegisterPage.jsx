import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { GraduationCap, Eye, EyeOff, UserPlus } from 'lucide-react'
import toast from 'react-hot-toast'

export default function RegisterPage() {
  const [form, setForm] = useState({
    username: '',
    password: '',
    confirmPassword: '',
    fullName: '',
    email: '',
    phoneNumber: '',
  })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)
  const { register } = useAuth()
  const navigate = useNavigate()

  const handleChange = (e) => {
    setForm((prev) => ({ ...prev, [e.target.name]: e.target.value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()

    const trimmedUsername = form.username.trim()
    const trimmedEmail = form.email.trim()
    const trimmedFullName = form.fullName.trim()

    if (trimmedUsername.length < 3) {
      toast.error('Tên đăng nhập phải có ít nhất 3 ký tự')
      return
    }
    if (form.password.length < 6) {
      toast.error('Mật khẩu phải có ít nhất 6 ký tự')
      return
    }
    if (form.password !== form.confirmPassword) {
      toast.error('Mật khẩu xác nhận không khớp')
      return
    }
    if (trimmedFullName.length === 0) {
      toast.error('Vui lòng nhập họ và tên')
      return
    }
    if (!trimmedEmail.includes('@')) {
      toast.error('Email không hợp lệ')
      return
    }

    setLoading(true)
    try {
      await register({
        username: trimmedUsername,
        password: form.password,
        fullName: trimmedFullName,
        email: trimmedEmail,
        phoneNumber: form.phoneNumber.trim() || undefined,
      })
      toast.success('Đăng ký thành công!')
      navigate('/')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Đăng ký thất bại, vui lòng thử lại')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500 p-4">
      {/* Background decoration */}
      <div className="absolute inset-0 overflow-hidden">
        <div className="absolute -top-40 -right-40 w-80 h-80 bg-white/10 rounded-full blur-3xl" />
        <div className="absolute -bottom-40 -left-40 w-80 h-80 bg-white/10 rounded-full blur-3xl" />
      </div>

      <div className="relative w-full max-w-md">
        <div className="bg-white/95 backdrop-blur-xl rounded-2xl shadow-2xl p-8">
          {/* Logo */}
          <div className="flex flex-col items-center mb-6">
            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-primary to-accent
              flex items-center justify-center shadow-lg shadow-primary/30 mb-4">
              <GraduationCap className="w-9 h-9 text-white" />
            </div>
            <h1 className="text-2xl font-bold text-foreground">Đăng ký tài khoản</h1>
            <p className="text-muted-foreground text-sm mt-1">Dành cho sinh viên thực tập</p>
          </div>

          {/* Form */}
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-foreground mb-1">Tên đăng nhập</label>
              <input
                type="text"
                name="username"
                value={form.username}
                onChange={handleChange}
                placeholder="Nhập username..."
                required
                minLength={3}
                autoComplete="username"
                className="w-full px-4 py-2.5 rounded-lg border border-input bg-white text-sm
                  focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-foreground mb-1">Họ và tên</label>
              <input
                type="text"
                name="fullName"
                value={form.fullName}
                onChange={handleChange}
                placeholder="Nhập họ và tên..."
                required
                className="w-full px-4 py-2.5 rounded-lg border border-input bg-white text-sm
                  focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-foreground mb-1">Email</label>
              <input
                type="email"
                name="email"
                value={form.email}
                onChange={handleChange}
                placeholder="Nhập email..."
                required
                autoComplete="email"
                className="w-full px-4 py-2.5 rounded-lg border border-input bg-white text-sm
                  focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-foreground mb-1">Số điện thoại</label>
              <input
                type="tel"
                name="phoneNumber"
                value={form.phoneNumber}
                onChange={handleChange}
                placeholder="Nhập số điện thoại (tùy chọn)..."
                className="w-full px-4 py-2.5 rounded-lg border border-input bg-white text-sm
                  focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary transition-all"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-foreground mb-1">Mật khẩu</label>
              <div className="relative">
                <input
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  value={form.password}
                  onChange={handleChange}
                  placeholder="Nhập mật khẩu (ít nhất 6 ký tự)..."
                  required
                  minLength={6}
                  autoComplete="new-password"
                  className="w-full px-4 py-2.5 rounded-lg border border-input bg-white text-sm
                    focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary
                    transition-all pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={showPassword ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground
                    hover:text-foreground transition-colors"
                >
                  {showPassword ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-foreground mb-1">Xác nhận mật khẩu</label>
              <div className="relative">
                <input
                  type={showConfirm ? 'text' : 'password'}
                  name="confirmPassword"
                  value={form.confirmPassword}
                  onChange={handleChange}
                  placeholder="Nhập lại mật khẩu..."
                  required
                  minLength={6}
                  autoComplete="new-password"
                  className="w-full px-4 py-2.5 rounded-lg border border-input bg-white text-sm
                    focus:outline-none focus:ring-2 focus:ring-primary/20 focus:border-primary
                    transition-all pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowConfirm(!showConfirm)}
                  aria-label={showConfirm ? 'Ẩn mật khẩu' : 'Hiện mật khẩu'}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground
                    hover:text-foreground transition-colors"
                >
                  {showConfirm ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                </button>
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 rounded-lg bg-gradient-to-r from-primary to-accent
                text-white font-semibold text-sm shadow-lg shadow-primary/30
                hover:shadow-xl hover:shadow-primary/40 transition-all
                disabled:opacity-50 flex items-center justify-center gap-2"
            >
              <UserPlus className="w-4 h-4" />
              {loading ? 'Đang đăng ký...' : 'Đăng ký'}
            </button>
          </form>

          {/* Login link */}
          <p className="text-center text-sm text-muted-foreground mt-6">
            Đã có tài khoản?{' '}
            <Link to="/login" className="text-primary font-medium hover:underline">
              Đăng nhập
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}

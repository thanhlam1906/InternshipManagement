import { useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import { Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

export default function OAuth2Callback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { login } = useAuth()

  useEffect(() => {
    const token = searchParams.get('token')
    const userId = searchParams.get('userId')
    const username = searchParams.get('username')
    const fullName = searchParams.get('fullName')
    const role = searchParams.get('role')

    if (!token) {
      toast.error('Đăng nhập Google thất bại — không nhận được token')
      navigate('/login', { replace: true })
      return
    }

    // Store the token so the AuthContext picks it up on next mount
    sessionStorage.setItem('token', token)

    toast.success('Đăng nhập Google thành công!')
    // Full page reload to let AuthProvider re-mount and read the token
    window.location.replace('/')
  }, [searchParams, navigate, login])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-500 via-purple-500 to-pink-500">
      <div className="bg-white/95 backdrop-blur-xl rounded-2xl shadow-2xl p-8 flex flex-col items-center gap-4">
        <Loader2 className="w-10 h-10 text-primary animate-spin" />
        <p className="text-foreground font-medium">Đang xử lý đăng nhập Google...</p>
        <p className="text-muted-foreground text-sm">Vui lòng đợi trong giây lát</p>
      </div>
    </div>
  )
}

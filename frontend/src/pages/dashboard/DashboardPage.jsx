import { useAuth } from '@/context/AuthContext'
import { Users, GraduationCap, UserCheck, ClipboardList, TrendingUp, Activity } from 'lucide-react'

const statCards = [
  { label: 'Tổng Users', icon: Users, color: 'from-blue-500 to-blue-600', roles: ['ADMIN'] },
  { label: 'Sinh viên', icon: GraduationCap, color: 'from-emerald-500 to-emerald-600', roles: ['ADMIN', 'MENTOR'] },
  { label: 'Giảng viên', icon: UserCheck, color: 'from-purple-500 to-purple-600', roles: ['ADMIN'] },
  { label: 'Phân công', icon: ClipboardList, color: 'from-orange-500 to-orange-600', roles: ['ADMIN', 'MENTOR', 'STUDENT'] },
]

export default function DashboardPage() {
  const { user } = useAuth()

  const visibleCards = statCards.filter(c => c.roles.includes(user?.role))

  return (
    <div className="space-y-6">
      {/* Welcome */}
      <div className="bg-gradient-to-r from-primary to-accent rounded-2xl p-6 text-white">
        <h1 className="text-2xl font-bold">Xin chào, {user?.fullName}! 👋</h1>
        <p className="text-white/80 mt-1">
          {user?.role === 'ADMIN' && 'Quản lý hệ thống thực tập'}
          {user?.role === 'MENTOR' && 'Quản lý sinh viên thực tập'}
          {user?.role === 'STUDENT' && 'Theo dõi quá trình thực tập của bạn'}
        </p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        {visibleCards.map(({ label, icon: Icon, color }) => (
          <div key={label} className="bg-white rounded-xl border border-border p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">{label}</p>
                <p className="text-2xl font-bold mt-1">--</p>
              </div>
              <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${color} flex items-center justify-center shadow-lg`}>
                <Icon className="w-6 h-6 text-white" />
              </div>
            </div>
            <div className="flex items-center gap-1 mt-3 text-xs text-muted-foreground">
              <TrendingUp className="w-3 h-3 text-success" />
              <span className="text-success font-medium">Dữ liệu từ API</span>
            </div>
          </div>
        ))}
      </div>

      {/* Quick Actions */}
      <div className="bg-white rounded-xl border border-border p-6">
        <div className="flex items-center gap-2 mb-4">
          <Activity className="w-5 h-5 text-primary" />
          <h2 className="text-lg font-semibold">Truy cập nhanh</h2>
        </div>
        <p className="text-sm text-muted-foreground">
          Sử dụng menu bên trái để truy cập các chức năng quản lý.
        </p>
      </div>
    </div>
  )
}

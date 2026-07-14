import { NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import {
  LayoutDashboard, Users, GraduationCap, UserCheck, Calendar,
  ClipboardList, Award, ListChecks, LogOut, ChevronLeft, ChevronRight, Target, Link
} from 'lucide-react'
import { useState } from 'react'
import toast from 'react-hot-toast'

const adminNav = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/users', icon: Users, label: 'Quản lý Users' },
  { to: '/students', icon: GraduationCap, label: 'Sinh viên' },
  { to: '/mentors', icon: UserCheck, label: 'Giảng viên' },
  { to: '/phases', icon: Calendar, label: 'Đợt thực tập & Đánh giá' },
  { to: '/assignments', icon: ClipboardList, label: 'Phân công' },
  { to: '/assessment-rounds', icon: Award, label: 'Đợt đánh giá' },
  { to: '/evaluation-criteria', icon: ListChecks, label: 'Tiêu chí' },
  { to: '/round-criteria', icon: Link, label: 'Tiêu chí đợt' },
  { to: '/assessment-results', icon: Target, label: 'Kết quả' },
]

const mentorNav = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/students', icon: GraduationCap, label: 'Sinh viên' },
  { to: '/assignments', icon: ClipboardList, label: 'Phân công' },
  { to: '/assessment-rounds', icon: Award, label: 'Đợt đánh giá' },
  { to: '/assessment-results', icon: Target, label: 'Kết quả' },
]

// STUDENT uses StudentLayout, not this sidebar — kept for fallback only
const studentNav = [
  { to: '/', icon: LayoutDashboard, label: 'Dashboard' },
]

function getNavItems(role) {
  switch (role) {
    case 'ADMIN': return adminNav
    case 'MENTOR': return mentorNav
    case 'STUDENT': return studentNav
    default: return []
  }
}

export default function Sidebar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(false)
  const navItems = getNavItems(user?.role)

  const handleLogout = async () => {
    try {
      await logout()
      navigate('/login')
    } catch (err) {
      toast.error('Đăng xuất thất bại. Vui lòng thử lại.')
    }
  }

  return (
    <aside className={`
      fixed left-0 top-0 h-screen bg-sidebar text-sidebar-foreground
      flex flex-col transition-all duration-300 z-50
      ${collapsed ? 'w-16' : 'w-64'}
    `}>
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 h-16 border-b border-sidebar-accent">
        <div className="w-8 h-8 rounded-lg bg-primary flex items-center justify-center shrink-0">
          <GraduationCap className="w-5 h-5 text-white" />
        </div>
        {!collapsed && (
          <span className="font-bold text-sm whitespace-nowrap">Internship Manager</span>
        )}
      </div>

      {/* Nav */}
      <nav aria-label="Điều hướng chính" className="flex-1 overflow-y-auto py-4 px-2 space-y-1">
        {navItems.map(({ to, icon: Icon, label }) => (
          <NavLink
            key={to}
            to={to}
            end={to === '/'}
            className={({ isActive }) => `
              flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium
              transition-all duration-200
              ${isActive
                ? 'bg-primary text-white shadow-md'
                : 'text-sidebar-foreground/70 hover:bg-sidebar-accent hover:text-sidebar-foreground'
              }
            `}
          >
            <Icon className="w-5 h-5 shrink-0" />
            {!collapsed && <span>{label}</span>}
          </NavLink>
        ))}
      </nav>

      {/* Collapse toggle */}
      <button
        onClick={() => setCollapsed(!collapsed)}
        aria-label={collapsed ? "Mở rộng menu" : "Thu gọn menu"}
        className="mx-2 mb-2 p-2 rounded-lg hover:bg-sidebar-accent transition-colors"
      >
        {collapsed ? <ChevronRight className="w-5 h-5" /> : <ChevronLeft className="w-5 h-5" />}
      </button>

      {/* User + Logout */}
      <div className="border-t border-sidebar-accent px-3 py-3">
        {!collapsed && (
          <div className="mb-2">
            <p className="text-sm font-medium truncate">{user?.fullName}</p>
            <p className="text-xs text-sidebar-foreground/50">{user?.role}</p>
          </div>
        )}
        <button
          onClick={handleLogout}
          aria-label="Đăng xuất"
          className="flex items-center gap-3 w-full px-3 py-2 rounded-lg text-sm
            text-red-400 hover:bg-red-500/10 transition-colors"
        >
          <LogOut className="w-5 h-5 shrink-0" />
          {!collapsed && <span>Đăng xuất</span>}
        </button>
      </div>
    </aside>
  )
}

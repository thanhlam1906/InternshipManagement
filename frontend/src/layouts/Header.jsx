import { useAuth } from '@/context/AuthContext'
import { Bell } from 'lucide-react'

export default function Header() {
  const { user } = useAuth()

  return (
    <header className="h-16 bg-white border-b border-border flex items-center justify-between px-6 sticky top-0 z-40">
      {/* Search - coming soon */}
      <div className="flex items-center gap-4" />

      <div className="flex items-center gap-4">
        {/* Notifications - coming soon */}
        <button className="relative p-2 rounded-lg hover:bg-secondary transition-colors" aria-label="Thông báo">
          <Bell className="w-5 h-5 text-muted-foreground" />
        </button>

        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-full bg-gradient-to-br from-primary to-accent
            flex items-center justify-center text-white font-semibold text-sm">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div className="hidden md:block">
            <p className="text-sm font-medium">{user?.fullName}</p>
            <p className="text-xs text-muted-foreground capitalize">{user?.role?.toLowerCase()}</p>
          </div>
        </div>
      </div>
    </header>
  )
}

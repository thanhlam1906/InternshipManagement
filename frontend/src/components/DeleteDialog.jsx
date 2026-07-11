import { useState, useEffect, useRef, useCallback } from 'react'
import { Trash2, X, AlertTriangle } from 'lucide-react'

export default function DeleteDialog({ open, onClose, onConfirm, title = 'Xác nhận xóa', message }) {
  const [loading, setLoading] = useState(false)
  const mountedRef = useRef(true)

  useEffect(() => {
    mountedRef.current = true
    return () => { mountedRef.current = false }
  }, [])

  const safeSetLoading = useCallback((val) => {
    if (mountedRef.current) setLoading(val)
  }, [])

  // Escape key handler
  useEffect(() => {
    if (!open) return
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && onClose) onClose()
    }
    document.addEventListener('keydown', handleKeyDown)
    return () => document.removeEventListener('keydown', handleKeyDown)
  }, [open, onClose])

  if (!open) return null

  const handleConfirm = async () => {
    if (!onConfirm) return
    safeSetLoading(true)
    try {
      await onConfirm()
    } finally {
      safeSetLoading(false)
    }
  }

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center"
      role="dialog"
      aria-modal="true"
      aria-labelledby="delete-dialog-title"
    >
      <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={() => onClose?.()} />
      <div className="relative bg-white rounded-xl shadow-2xl p-6 w-full max-w-md mx-4 animate-in fade-in zoom-in-95">
        <button
          onClick={() => onClose?.()}
          aria-label="Đóng"
          className="absolute right-4 top-4 p-1 rounded-lg hover:bg-secondary"
        >
          <X className="w-4 h-4" />
        </button>

        <div className="flex items-center gap-3 mb-4">
          <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center">
            <AlertTriangle className="w-5 h-5 text-destructive" />
          </div>
          <h3 id="delete-dialog-title" className="text-lg font-semibold">{title}</h3>
        </div>

        <p className="text-muted-foreground text-sm mb-6">
          {message || 'Bạn có chắc chắn muốn xóa? Hành động này không thể hoàn tác.'}
        </p>

        <div className="flex gap-3 justify-end">
          <button
            onClick={() => onClose?.()}
            className="px-4 py-2 rounded-lg border border-border text-sm font-medium
              hover:bg-secondary transition-colors"
          >
            Hủy
          </button>
          <button
            onClick={handleConfirm}
            disabled={loading}
            className="px-4 py-2 rounded-lg bg-destructive text-white text-sm font-medium
              hover:bg-destructive/90 transition-colors disabled:opacity-50 flex items-center gap-2"
          >
            <Trash2 className="w-4 h-4" />
            {loading ? 'Đang xóa...' : 'Xóa'}
          </button>
        </div>
      </div>
    </div>
  )
}

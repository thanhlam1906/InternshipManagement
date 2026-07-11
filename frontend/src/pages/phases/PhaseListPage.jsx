import { useState, useEffect, useCallback } from 'react'
import { phaseApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function PhaseListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'

  const [phases, setPhases] = useState([])
  const [pagination, setPagination] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [editingPhase, setEditingPhase] = useState(null)
  const [deletingPhase, setDeletingPhase] = useState(null)
  const [formLoading, setFormLoading] = useState(false)

  // Form state
  const [form, setForm] = useState({
    phaseName: '',
    startDate: '',
    endDate: '',
    description: ''
  })

  const fetchPhases = useCallback(async () => {
    setLoading(true)
    try {
      const res = await phaseApi.getAll({ page, size: 10 })
      setPhases(res.data.data.items || [])
      setPagination(res.data.data.pagination)
    } catch (err) {
      toast.error('Không thể tải danh sách đợt thực tập')
    } finally {
      setLoading(false)
    }
  }, [page])

  useEffect(() => {
    fetchPhases()
  }, [fetchPhases])

  const openCreate = () => {
    setEditingPhase(null)
    setForm({
      phaseName: '',
      startDate: '',
      endDate: '',
      description: ''
    })
    setFormOpen(true)
  }

  const openEdit = (phase) => {
    setEditingPhase(phase)
    setForm({
      phaseName: phase.phaseName || '',
      startDate: phase.startDate || '',
      endDate: phase.endDate || '',
      description: phase.description || ''
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      if (editingPhase) {
        await phaseApi.update(editingPhase.id, form)
        toast.success('Cập nhật đợt thực tập thành công!')
      } else {
        await phaseApi.create(form)
        toast.success('Tạo đợt thực tập thành công!')
      }
      setFormOpen(false)
      fetchPhases()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      await phaseApi.delete(deletingPhase.id)
      toast.success('Xóa đợt thực tập thành công!')
      setDeleteOpen(false)
      fetchPhases()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    }
  }

  const columns = [
    { key: 'id', title: 'ID', width: '60px' },
    { key: 'phaseName', title: 'Tên đợt thực tập' },
    { key: 'startDate', title: 'Ngày bắt đầu' },
    { key: 'endDate', title: 'Ngày kết thúc' },
    { key: 'description', title: 'Mô tả', render: (val) => val || '--' },
    {
      key: 'actions', title: '',
      render: (_, row) => {
        if (!isAdmin) return null
        return (
          <div className="flex gap-1">
            <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
              className="p-1.5 rounded-lg hover:bg-secondary transition-colors">
              <Edit className="w-4 h-4 text-muted-foreground" />
            </button>
            <button onClick={(e) => { e.stopPropagation(); setDeletingPhase(row); setDeleteOpen(true) }}
              className="p-1.5 rounded-lg hover:bg-red-50 transition-colors">
              <Trash2 className="w-4 h-4 text-destructive" />
            </button>
          </div>
        )
      }
    },
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Quản lý Đợt thực tập</h1>
          <p className="text-muted-foreground text-sm">Các mốc thời gian diễn ra đợt thực tập của trường</p>
        </div>
        {isAdmin && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm đợt mới
          </button>
        )}
      </div>

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={phases}
          pagination={pagination}
          onPageChange={setPage}
          emptyMessage="Chưa có đợt thực tập nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingPhase ? 'Chỉnh sửa Đợt thực tập' : 'Tạo Đợt thực tập mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        <div>
          <label className="block text-sm font-medium mb-1">Tên đợt thực tập</label>
          <input
            type="text"
            value={form.phaseName}
            onChange={e => setForm({ ...form, phaseName: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Ngày bắt đầu</label>
          <input
            type="date"
            value={form.startDate}
            onChange={e => setForm({ ...form, startDate: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Ngày kết thúc</label>
          <input
            type="date"
            value={form.endDate}
            onChange={e => setForm({ ...form, endDate: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Mô tả</label>
          <textarea
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 min-h-[80px]"
          />
        </div>
      </FormDialog>

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        message={`Bạn có chắc muốn xóa đợt thực tập "${deletingPhase?.phaseName}"?`}
      />
    </div>
  )
}

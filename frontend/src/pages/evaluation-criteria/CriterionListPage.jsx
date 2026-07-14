import { useState, useEffect, useCallback, useRef } from 'react'
import { criterionApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function CriterionListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'

  const [criteria, setCriteria] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [editingCriterion, setEditingCriterion] = useState(null)
  const [deletingCriterion, setDeletingCriterion] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const abortRef = useRef(null)

  // Form state
  const [form, setForm] = useState({
    criterionName: '',
    description: '',
    maxScore: ''
  })

  const fetchCriteria = useCallback(async () => {
    setLoading(true)
    try {
      const res = await criterionApi.getAll({ signal: abortRef.current?.signal })
      setCriteria(res.data.data.items || [])
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách tiêu chí đánh giá')
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchCriteria()
    return () => { abortRef.current?.abort() }
  }, [fetchCriteria])

  const openCreate = () => {
    setEditingCriterion(null)
    setForm({
      criterionName: '',
      description: '',
      maxScore: ''
    })
    setFormOpen(true)
  }

  const openEdit = (criterion) => {
    setEditingCriterion(criterion)
    setForm({
      criterionName: criterion.criterionName || '',
      description: criterion.description || '',
      maxScore: criterion.maxScore || ''
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      const maxScore = parseFloat(form.maxScore)
      if (isNaN(maxScore)) {
        toast.error('Điểm tối đa không hợp lệ')
        setFormLoading(false)
        return
      }
      const payload = {
        ...form,
        maxScore
      }
      if (editingCriterion) {
        await criterionApi.update(editingCriterion.id, payload)
        toast.success('Cập nhật tiêu chí thành công!')
      } else {
        await criterionApi.create(payload)
        toast.success('Tạo tiêu chí thành công!')
      }
      setFormOpen(false)
      fetchCriteria()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await criterionApi.delete(deletingCriterion.id)
      toast.success('Xóa tiêu chí thành công!')
      setDeleteOpen(false)
      fetchCriteria()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setDeleteLoading(false)
    }
  }

  const columns = [
    { key: 'id', title: 'ID', width: '60px' },
    { key: 'criterionName', title: 'Tên tiêu chí' },
    { key: 'maxScore', title: 'Điểm tối đa', width: '120px' },
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
            <button onClick={(e) => { e.stopPropagation(); setDeletingCriterion(row); setDeleteOpen(true) }}
              disabled={loading}
              className="p-1.5 rounded-lg hover:bg-red-50 transition-colors disabled:opacity-50">
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
          <h1 className="text-2xl font-bold">Tiêu chí Đánh giá</h1>
          <p className="text-muted-foreground text-sm">Các tiêu chí dùng để chấm điểm thực tập của sinh viên</p>
        </div>
        {isAdmin && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm tiêu chí
          </button>
        )}
      </div>

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={criteria}
          emptyMessage="Chưa có tiêu chí đánh giá nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingCriterion ? 'Chỉnh sửa Tiêu chí' : 'Tạo Tiêu chí mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        <div>
          <label className="block text-sm font-medium mb-1">Tên tiêu chí</label>
          <input
            type="text"
            value={form.criterionName}
            onChange={e => setForm({ ...form, criterionName: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Điểm tối đa</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            value={form.maxScore}
            onChange={e => setForm({ ...form, maxScore: e.target.value })}
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
        loading={deleteLoading}
        message={`Bạn có chắc muốn xóa tiêu chí "${deletingCriterion?.criterionName}"?`}
      />
    </div>
  )
}

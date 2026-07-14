import { useState, useEffect, useCallback, useRef } from 'react'
import { roundCriterionApi, roundApi, criterionApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function RoundCriterionListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'

  const [roundCriteria, setRoundCriteria] = useState([])
  const [rounds, setRounds] = useState([])
  const [criteria, setCriteria] = useState([])
  const [loading, setLoading] = useState(true)

  // Selected Round Filter
  const [selectedRoundId, setSelectedRoundId] = useState('')

  // Dialogs
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [editingMapping, setEditingMapping] = useState(null)
  const [deletingMapping, setDeletingMapping] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const abortRef = useRef(null)

  // Form State
  const [form, setForm] = useState({
    criterionId: '',
    weight: '1.0'
  })

  const fetchRoundsAndCriteria = useCallback(async () => {
    try {
      const [roundsRes, criteriaRes] = await Promise.all([
        roundApi.getAll({ page: 0, size: 100 }),
        criterionApi.getAll()
      ])
      const fetchedRounds = roundsRes.data.data.items || []
      setRounds(fetchedRounds)
      setCriteria(criteriaRes.data.data.items || [])
      
      // Auto select first round if available
      if (fetchedRounds.length > 0) {
        setSelectedRoundId(fetchedRounds[0].id.toString())
      }
    } catch (err) {
      console.error('Failed to load filter options')
    }
  }, [])

  const fetchRoundCriteria = useCallback(async () => {
    if (!selectedRoundId) {
      setRoundCriteria([])
      setLoading(false)
      return
    }
    setLoading(true)
    try {
      const res = await roundCriterionApi.getAll({ roundId: parseInt(selectedRoundId) }, { signal: abortRef.current?.signal })
      setRoundCriteria(res.data.data || [])
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách tiêu chí của đợt')
      }
    } finally {
      setLoading(false)
    }
  }, [selectedRoundId])

  useEffect(() => {
    fetchRoundsAndCriteria()
  }, [fetchRoundsAndCriteria])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchRoundCriteria()
    return () => { abortRef.current?.abort() }
  }, [fetchRoundCriteria])

  const openCreate = () => {
    setEditingMapping(null)
    setForm({ criterionId: '', weight: '1.0' })
    setFormOpen(true)
  }

  const openEdit = (mapping) => {
    setEditingMapping(mapping)
    setForm({
      criterionId: mapping.criterionId,
      weight: mapping.weight?.toString() || '1.0'
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      const weight = parseFloat(form.weight)
      if (isNaN(weight) || weight <= 0) {
        toast.error('Trọng số không hợp lệ')
        setFormLoading(false)
        return
      }
      if (editingMapping) {
        await roundCriterionApi.update(editingMapping.id, {
          weight
        })
        toast.success('Cập nhật trọng số thành công!')
      } else {
        const criterionId = parseInt(form.criterionId)
        if (isNaN(criterionId)) {
          toast.error('Vui lòng chọn tiêu chí')
          setFormLoading(false)
          return
        }
        await roundCriterionApi.create({
          roundId: parseInt(selectedRoundId),
          criterionId,
          weight
        })
        toast.success('Gán tiêu chí vào đợt đánh giá thành công!')
      }
      setFormOpen(false)
      fetchRoundCriteria()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await roundCriterionApi.delete(deletingMapping.id)
      toast.success('Xóa tiêu chí khỏi đợt thành công!')
      setDeleteOpen(false)
      fetchRoundCriteria()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setDeleteLoading(false)
    }
  }

  const columns = [
    { key: 'id', title: 'Mã mapping', width: '120px' },
    { key: 'criterionName', title: 'Tên tiêu chí' },
    { key: 'weight', title: 'Trọng số', width: '120px' },
    {
      key: 'actions', title: '',
      render: (_, row) => {
        if (!isAdmin) return null
        return (
          <div className="flex gap-1">
            <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
              className="p-1.5 rounded-lg hover:bg-secondary transition-colors" title="Sửa trọng số">
              <Edit className="w-4 h-4 text-muted-foreground" />
            </button>
            <button onClick={(e) => { e.stopPropagation(); setDeletingMapping(row); setDeleteOpen(true) }}
              disabled={loading}
              className="p-1.5 rounded-lg hover:bg-red-50 transition-colors disabled:opacity-50" title="Xóa khỏi đợt">
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
          <h1 className="text-2xl font-bold">Quản lý trọng số tiêu chí đợt</h1>
          <p className="text-muted-foreground text-sm">Gán tiêu chí đánh giá chung vào các đợt đánh giá cụ thể với trọng số tương ứng</p>
        </div>
        {isAdmin && selectedRoundId && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm tiêu chí vào đợt
          </button>
        )}
      </div>

      {/* Select Round Filter */}
      <div className="bg-white p-4 rounded-xl border border-border flex gap-4 items-center">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-muted-foreground">Đợt đánh giá:</span>
          <select value={selectedRoundId} onChange={e => setSelectedRoundId(e.target.value)}
            className="px-3 py-1.5 rounded-lg border border-input text-sm bg-white min-w-[250px]">
            <option value="">-- Chọn đợt đánh giá --</option>
            {rounds.map(r => <option key={r.id} value={r.id}>{r.roundName}</option>)}
          </select>
        </div>
      </div>

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={roundCriteria}
          emptyMessage="Chọn đợt đánh giá hoặc đợt chưa được gán tiêu chí nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingMapping ? 'Chỉnh sửa Trọng số' : 'Gán Tiêu chí vào Đợt'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        {!editingMapping && (
          <div>
            <label className="block text-sm font-medium mb-1">Tiêu chí</label>
            <select value={form.criterionId} onChange={e => setForm({ ...form, criterionId: e.target.value })}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
              <option value="">-- Chọn tiêu chí --</option>
              {criteria
                .filter(c => !roundCriteria.some(rc => rc.criterionId === c.id))
                .map(c => <option key={c.id} value={c.id}>{c.criterionName} (Tối đa: {c.maxScore})</option>)}
            </select>
          </div>
        )}

        <div>
          <label className="block text-sm font-medium mb-1">Trọng số</label>
          <input
            type="number"
            step="0.01"
            min="0.01"
            value={form.weight}
            onChange={e => setForm({ ...form, weight: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>
      </FormDialog>

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        loading={deleteLoading}
        message={`Bạn có chắc muốn gỡ tiêu chí này khỏi đợt đánh giá?`}
      />
    </div>
  )
}

import { useState, useEffect, useCallback, useRef } from 'react'
import { roundApi, phaseApi, criterionApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2, Eye, ToggleLeft, ToggleRight } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function RoundListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'

  const [rounds, setRounds] = useState([])
  const [phases, setPhases] = useState([])
  const [criteria, setCriteria] = useState([])
  const [pagination, setPagination] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)

  // Filters
  const [phaseFilter, setPhaseFilter] = useState('')

  // Dialogs
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [detailOpen, setDetailOpen] = useState(false)

  const [editingRound, setEditingRound] = useState(null)
  const [deletingRound, setDeletingRound] = useState(null)
  const [selectedRound, setSelectedRound] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [toggleLoadingId, setToggleLoadingId] = useState(null)
  const abortRef = useRef(null)

  // Form states
  const [form, setForm] = useState({
    phaseId: '',
    roundName: '',
    startDate: '',
    endDate: '',
    description: '',
    isActive: true,
    criteriaList: [] // list of { criterionId: '', weight: '' }
  })

  const fetchFilters = useCallback(async () => {
    try {
      const [phaseRes, criterionRes] = await Promise.all([
        phaseApi.getAll({ page: 0, size: 100 }),
        criterionApi.getAll()
      ])
      setPhases(phaseRes.data.data.items || [])
      setCriteria(criterionRes.data.data.items || [])
    } catch (err) {
      console.error('Failed to load filter options')
    }
  }, [])

  const fetchRounds = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size: 10 }
      if (phaseFilter) params.phaseId = phaseFilter
      const res = await roundApi.getAll(params, { signal: abortRef.current?.signal })
      setRounds(res.data.data.items || [])
      setPagination(res.data.data.pagination)
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách đợt đánh giá')
      }
    } finally {
      setLoading(false)
    }
  }, [page, phaseFilter])

  useEffect(() => {
    fetchFilters()
  }, [fetchFilters])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchRounds()
    return () => { abortRef.current?.abort() }
  }, [fetchRounds])

  const openCreate = () => {
    setEditingRound(null)
    setForm({
      phaseId: '',
      roundName: '',
      startDate: '',
      endDate: '',
      description: '',
      isActive: true,
      criteriaList: [{ criterionId: '', weight: '1.0' }]
    })
    setFormOpen(true)
  }

  const openEdit = (round) => {
    setEditingRound(round)
    setForm({
      phaseId: round.phaseId,
      roundName: round.roundName || '',
      startDate: round.startDate || '',
      endDate: round.endDate || '',
      description: round.description || '',
      isActive: round.isActive ?? true,
      criteriaList: round.criteria?.map(c => ({
        criterionId: c.criterionId,
        weight: c.weight?.toString() || '1.0'
      })) || []
    })
    setFormOpen(true)
  }

  const handleAddCriterionField = () => {
    setForm({
      ...form,
      criteriaList: [...form.criteriaList, { criterionId: '', weight: '1.0' }]
    })
  }

  const handleRemoveCriterionField = (index) => {
    const list = [...form.criteriaList]
    list.splice(index, 1)
    setForm({ ...form, criteriaList: list })
  }

  const handleCriterionChange = (index, field, value) => {
    const list = [...form.criteriaList]
    list[index][field] = value
    setForm({ ...form, criteriaList: list })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (form.startDate && form.endDate && form.endDate < form.startDate) {
      toast.error('Ngày kết thúc phải sau hoặc bằng ngày bắt đầu')
      return
    }
    setFormLoading(true)
    try {
      if (editingRound) {
        // Edit round only updates round fields (not direct criteria weight map via create request)
        await roundApi.update(editingRound.id, {
          phaseId: parseInt(form.phaseId),
          roundName: form.roundName,
          startDate: form.startDate,
          endDate: form.endDate,
          description: form.description,
          isActive: form.isActive
        })
        toast.success('Cập nhật đợt đánh giá thành công!')
      } else {
        // Create request includes criteria mapping
        const criteriaListPayload = form.criteriaList
          .filter(c => c.criterionId !== '')
          .map(c => ({
            criterionId: parseInt(c.criterionId),
            weight: parseFloat(c.weight)
          }))
        
        await roundApi.create({
          phaseId: parseInt(form.phaseId),
          roundName: form.roundName,
          startDate: form.startDate,
          endDate: form.endDate,
          description: form.description,
          criteria: criteriaListPayload
        })
        toast.success('Tạo đợt đánh giá thành công!')
      }
      setFormOpen(false)
      fetchRounds()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await roundApi.delete(deletingRound.id)
      toast.success('Xóa đợt đánh giá thành công!')
      setDeleteOpen(false)
      fetchRounds()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setDeleteLoading(false)
    }
  }

  const handleToggleStatus = async (round) => {
    setToggleLoadingId(round.id)
    try {
      await roundApi.update(round.id, { isActive: !round.isActive })
      toast.success('Cập nhật trạng thái thành công!')
      fetchRounds()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setToggleLoadingId(null)
    }
  }

  const columns = [
    { key: 'id', title: 'ID', width: '60px' },
    { key: 'roundName', title: 'Tên đợt đánh giá' },
    { key: 'startDate', title: 'Ngày bắt đầu' },
    { key: 'endDate', title: 'Ngày kết thúc' },
    {
      key: 'isActive', title: 'Trạng thái',
      render: (val, row) => (
        <button
          disabled={!isAdmin || toggleLoadingId === row.id}
          onClick={(e) => { e.stopPropagation(); handleToggleStatus(row) }}
          className="flex items-center gap-1.5 text-xs font-medium disabled:opacity-50"
        >
          {val ? (
            <><ToggleRight className="w-5 h-5 text-success" /> <span className="text-success">Hoạt động</span></>
          ) : (
            <><ToggleLeft className="w-5 h-5 text-muted-foreground" /> <span className="text-muted-foreground">Khóa</span></>
          )}
        </button>
      )
    },
    {
      key: 'actions', title: '',
      render: (_, row) => (
        <div className="flex gap-1">
          <button onClick={(e) => { e.stopPropagation(); setSelectedRound(row); setDetailOpen(true) }}
            className="p-1.5 rounded-lg hover:bg-secondary transition-colors" title="Chi tiết tiêu chí">
            <Eye className="w-4 h-4 text-primary" />
          </button>
          {isAdmin && (
            <>
              <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
                className="p-1.5 rounded-lg hover:bg-secondary transition-colors" title="Sửa">
                <Edit className="w-4 h-4 text-muted-foreground" />
              </button>
              <button onClick={(e) => { e.stopPropagation(); setDeletingRound(row); setDeleteOpen(true) }}
                disabled={loading}
                className="p-1.5 rounded-lg hover:bg-red-50 transition-colors disabled:opacity-50" title="Xóa">
                <Trash2 className="w-4 h-4 text-destructive" />
              </button>
            </>
          )}
        </div>
      )
    },
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Đợt đánh giá thực tập</h1>
          <p className="text-muted-foreground text-sm">Các đợt đánh giá chấm điểm thực tập tương ứng với đợt thực tập</p>
        </div>
        {isAdmin && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm đợt đánh giá
          </button>
        )}
      </div>

      {/* Filter */}
      <div className="bg-white p-4 rounded-xl border border-border flex gap-4 items-center">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-muted-foreground">Lọc đợt thực tập:</span>
          <select value={phaseFilter} onChange={e => { setPhaseFilter(e.target.value); setPage(0) }}
            className="px-3 py-1.5 rounded-lg border border-input text-sm bg-white min-w-[200px]">
            <option value="">Tất cả</option>
            {phases.map(p => <option key={p.id} value={p.id}>{p.phaseName}</option>)}
          </select>
        </div>
      </div>

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={rounds}
          pagination={pagination}
          onPageChange={setPage}
          emptyMessage="Chưa có đợt đánh giá nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingRound ? 'Chỉnh sửa Đợt đánh giá' : 'Tạo Đợt đánh giá mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        <div>
          <label className="block text-sm font-medium mb-1">Đợt thực tập liên kết</label>
          <select value={form.phaseId} onChange={e => setForm({ ...form, phaseId: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
            <option value="">-- Chọn Đợt --</option>
            {phases.map(p => <option key={p.id} value={p.id}>{p.phaseName}</option>)}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Tên đợt đánh giá</label>
          <input
            type="text"
            value={form.roundName}
            onChange={e => setForm({ ...form, roundName: e.target.value })}
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

        {/* Dynamic Criteria List - only on Create */}
        {!editingRound && (
          <div className="space-y-2 border-t border-border pt-4">
            <div className="flex justify-between items-center">
              <label className="text-sm font-medium">Tiêu chí và Trọng số</label>
              <button type="button" onClick={handleAddCriterionField}
                className="text-xs text-primary font-semibold hover:underline">
                + Thêm tiêu chí
              </button>
            </div>
            {form.criteriaList.map((item, idx) => (
              <div key={item.criterionId ? `${item.criterionId}-${idx}` : `new-${idx}`} className="flex gap-2 items-center">
                <select
                  value={item.criterionId}
                  onChange={e => handleCriterionChange(idx, 'criterionId', e.target.value)}
                  className="flex-1 px-3 py-2 rounded-lg border border-input text-sm bg-white"
                  required
                >
                  <option value="">-- Chọn tiêu chí --</option>
                  {criteria.map(c => <option key={c.id} value={c.id}>{c.criterionName} (Tối đa: {c.maxScore})</option>)}
                </select>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  placeholder="Trọng số"
                  value={item.weight}
                  onChange={e => handleCriterionChange(idx, 'weight', e.target.value)}
                  className="w-24 px-3 py-2 rounded-lg border border-input text-sm focus:outline-none"
                  required
                />
                {form.criteriaList.length > 1 && (
                  <button type="button" onClick={() => handleRemoveCriterionField(idx)}
                    className="text-destructive hover:text-destructive/80 text-xs">
                    Xóa
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </FormDialog>

      {/* Detail Dialog */}
      {detailOpen && selectedRound && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={() => setDetailOpen(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-lg mx-4 p-6 animate-in fade-in zoom-in-95">
            <h3 className="text-lg font-bold mb-4">Danh sách tiêu chí đợt: {selectedRound.roundName}</h3>
            <div className="divide-y divide-border max-h-[300px] overflow-y-auto pr-2">
              {selectedRound.criteria && selectedRound.criteria.length > 0 ? (
                selectedRound.criteria.map((c) => (
                  <div key={c.id} className="py-2.5 flex justify-between items-center text-sm">
                    <div>
                      <p className="font-semibold">{c.criterionName}</p>
                      <p className="text-xs text-muted-foreground">Mã tiêu chí: {c.criterionId}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-semibold text-primary">Trọng số: {c.weight}</p>
                    </div>
                  </div>
                ))
              ) : (
                <p className="py-6 text-center text-muted-foreground text-sm">Không có tiêu chí nào được gán</p>
              )}
            </div>
            <div className="flex justify-end mt-6">
              <button onClick={() => setDetailOpen(false)}
                className="px-4 py-2 rounded-lg bg-secondary hover:bg-secondary/80 text-sm font-medium transition-colors">
                Đóng
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        loading={deleteLoading}
        message={`Bạn có chắc muốn xóa đợt đánh giá "${deletingRound?.roundName}"?`}
      />
    </div>
  )
}

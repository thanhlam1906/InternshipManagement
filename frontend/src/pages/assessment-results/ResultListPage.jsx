import { useState, useEffect, useCallback } from 'react'
import { resultApi, assignmentApi, roundApi, roundCriterionApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function ResultListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'
  const isMentor = currentUser?.role === 'MENTOR'
  const isStudent = currentUser?.role === 'STUDENT'

  const [results, setResults] = useState([])
  const [pagination, setPagination] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)

  // Options
  const [assignments, setAssignments] = useState([])
  const [rounds, setRounds] = useState([])
  const [roundCriteria, setRoundCriteria] = useState([])

  // Dialogs
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  
  const [editingResult, setEditingResult] = useState(null)
  const [deletingResult, setDeletingResult] = useState(null)
  const [formLoading, setFormLoading] = useState(false)

  // Filters (Admin/Mentor)
  const [selectedStudentId, setSelectedStudentId] = useState('')
  const [selectedRoundId, setSelectedRoundId] = useState('')

  // Form states
  const [form, setForm] = useState({
    assignmentId: '',
    roundId: '',
    criterionId: '',
    score: '',
    comments: ''
  })

  const fetchFilters = useCallback(async () => {
    try {
      // Fetch assignments based on role
      const assignParams = { page: 0, size: 100 }
      if (isMentor) assignParams.mentorId = currentUser.userId
      if (isStudent) assignParams.studentId = currentUser.userId

      const [assignRes, roundRes] = await Promise.all([
        assignmentApi.getAll(assignParams),
        roundApi.getAll({ page: 0, size: 100 })
      ])
      setAssignments(assignRes.data.data.items || [])
      setRounds(roundRes.data.data.items || [])
    } catch (err) {
      console.error('Failed to load filter options')
    }
  }, [isMentor, isStudent, currentUser.userId])

  const fetchResults = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size: 10 }
      
      if (isStudent) {
        params.studentId = currentUser.userId
      } else {
        if (isMentor) params.mentorId = currentUser.userId
        if (selectedStudentId) params.studentId = selectedStudentId
        if (selectedRoundId) params.roundId = selectedRoundId
      }

      const res = await resultApi.getAll(params)
      setResults(res.data.data.items || [])
      setPagination(res.data.data.pagination)
    } catch (err) {
      toast.error('Không thể tải danh sách kết quả đánh giá')
    } finally {
      setLoading(false)
    }
  }, [page, isStudent, isMentor, currentUser.userId, selectedStudentId, selectedRoundId])

  useEffect(() => {
    fetchFilters()
  }, [fetchFilters])

  useEffect(() => {
    fetchResults()
  }, [fetchResults])

  // Fetch round criteria when round changes in the form
  useEffect(() => {
    if (!form.roundId || editingResult) return
    
    const loadCriteria = async () => {
      try {
        const res = await roundCriterionApi.getAll({ roundId: parseInt(form.roundId) })
        setRoundCriteria(res.data.data || [])
      } catch (err) {
        console.error('Failed to load criteria for round')
      }
    }
    loadCriteria()
  }, [form.roundId, editingResult])

  const openCreate = () => {
    setEditingResult(null)
    setForm({
      assignmentId: '',
      roundId: '',
      criterionId: '',
      score: '',
      comments: ''
    })
    setRoundCriteria([])
    setFormOpen(true)
  }

  const openEdit = (result) => {
    setEditingResult(result)
    setForm({
      assignmentId: result.assignmentId,
      roundId: result.roundId,
      criterionId: result.criterionId,
      score: result.score || '',
      comments: result.comments || ''
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      if (editingResult) {
        await resultApi.update(editingResult.id, {
          score: parseFloat(form.score),
          comments: form.comments
        })
        toast.success('Cập nhật điểm đánh giá thành công!')
      } else {
        await resultApi.create({
          assignmentId: parseInt(form.assignmentId),
          roundId: parseInt(form.roundId),
          criterionId: parseInt(form.criterionId),
          score: parseFloat(form.score),
          comments: form.comments
        })
        toast.success('Đánh giá sinh viên thành công!')
      }
      setFormOpen(false)
      fetchResults()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      await resultApi.delete(deletingResult.id)
      toast.success('Xóa điểm đánh giá thành công!')
      setDeleteOpen(false)
      fetchResults()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    }
  }

  const columns = [
    { key: 'id', title: 'ID', width: '60px' },
    { key: 'roundName', title: 'Đợt đánh giá' },
    { key: 'criterionName', title: 'Tiêu chí' },
    { key: 'score', title: 'Điểm', render: (val) => <span className="font-bold text-primary">{val}</span> },
    { key: 'comments', title: 'Nhận xét', render: (val) => val || '--' },
    { key: 'evaluatedByName', title: 'Người đánh giá' },
    {
      key: 'actions', title: '',
      render: (_, row) => {
        // Mentors or Admins can edit/delete
        const canManage = isAdmin || (isMentor && row.evaluatedById === currentUser.userId)
        if (!canManage) return null

        return (
          <div className="flex gap-1">
            <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
              className="p-1.5 rounded-lg hover:bg-secondary transition-colors" title="Sửa điểm">
              <Edit className="w-4 h-4 text-muted-foreground" />
            </button>
            <button onClick={(e) => { e.stopPropagation(); setDeletingResult(row); setDeleteOpen(true) }}
              className="p-1.5 rounded-lg hover:bg-red-50 transition-colors" title="Xóa điểm">
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
          <h1 className="text-2xl font-bold">Kết quả Đánh giá</h1>
          <p className="text-muted-foreground text-sm">Chấm điểm các tiêu chí của sinh viên thực tập</p>
        </div>
        {isMentor && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Đánh giá sinh viên
          </button>
        )}
      </div>

      {/* Filters */}
      {!isStudent && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-white p-4 rounded-xl border border-border">
          <div>
            <label className="block text-xs font-semibold text-muted-foreground uppercase mb-1">Sinh viên</label>
            <select value={selectedStudentId} onChange={e => { setSelectedStudentId(e.target.value); setPage(0) }}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white">
              <option value="">Tất cả sinh viên đang hướng dẫn</option>
              {assignments.map(a => <option key={a.studentId} value={a.studentId}>{a.studentName} ({a.studentCode})</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-muted-foreground uppercase mb-1">Đợt đánh giá</label>
            <select value={selectedRoundId} onChange={e => { setSelectedRoundId(e.target.value); setPage(0) }}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white">
              <option value="">Tất cả đợt đánh giá</option>
              {rounds.map(r => <option key={r.id} value={r.id}>{r.roundName}</option>)}
            </select>
          </div>
        </div>
      )}

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={results}
          pagination={pagination}
          onPageChange={setPage}
          emptyMessage="Chưa có kết quả đánh giá nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingResult ? 'Chỉnh sửa kết quả Đánh giá' : 'Đánh giá Sinh viên'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        {!editingResult && (
          <>
            <div>
              <label className="block text-sm font-medium mb-1">Sinh viên thực tập</label>
              <select value={form.assignmentId} onChange={e => setForm({ ...form, assignmentId: e.target.value })}
                className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
                <option value="">-- Chọn sinh viên --</option>
                {assignments.map(a => <option key={a.id} value={a.id}>{a.studentName} ({a.studentCode})</option>)}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Đợt đánh giá</label>
              <select value={form.roundId} onChange={e => setForm({ ...form, roundId: e.target.value })}
                className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
                <option value="">-- Chọn đợt đánh giá --</option>
                {rounds.map(r => <option key={r.id} value={r.id}>{r.roundName}</option>)}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">Tiêu chí đánh giá</label>
              <select value={form.criterionId} onChange={e => setForm({ ...form, criterionId: e.target.value })}
                className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
                <option value="">-- Chọn tiêu chí --</option>
                {roundCriteria.map(rc => (
                  <option key={rc.criterionId} value={rc.criterionId}>
                    {rc.criterionName} (Trọng số: {rc.weight})
                  </option>
                ))}
              </select>
            </div>
          </>
        )}

        <div>
          <label className="block text-sm font-medium mb-1">Điểm số</label>
          <input
            type="number"
            step="0.1"
            value={form.score}
            onChange={e => setForm({ ...form, score: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Nhận xét / Góp ý</label>
          <textarea
            value={form.comments}
            onChange={e => setForm({ ...form, comments: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 min-h-[80px]"
          />
        </div>
      </FormDialog>

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        message={`Bạn có chắc muốn xóa điểm đánh giá này?`}
      />
    </div>
  )
}

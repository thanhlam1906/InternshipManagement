import { useState, useEffect, useCallback, useRef } from 'react'
import { assignmentApi, studentApi, mentorApi, phaseApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2, CheckCircle2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function AssignmentListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'
  const isMentor = currentUser?.role === 'MENTOR'
  const isStudent = currentUser?.role === 'STUDENT'

  const [assignments, setAssignments] = useState([])
  const [pagination, setPagination] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)

  // Filters
  const [phases, setPhases] = useState([])
  const [students, setStudents] = useState([])
  const [mentors, setMentors] = useState([])

  const [selectedPhase, setSelectedPhase] = useState('')
  const [selectedStudent, setSelectedStudent] = useState('')
  const [selectedMentor, setSelectedMentor] = useState('')

  // Dialogs
  const [formOpen, setFormOpen] = useState(false)
  const [statusOpen, setStatusOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  
  const [editingAssignment, setEditingAssignment] = useState(null)
  const [deletingAssignment, setDeletingAssignment] = useState(null)
  const [statusAssignment, setStatusAssignment] = useState(null)

  const [formLoading, setFormLoading] = useState(false)
  const [statusLoading, setStatusLoading] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const abortRef = useRef(null)

  // Form states
  const [form, setForm] = useState({
    studentId: '',
    mentorId: '',
    phaseId: ''
  })
  const [newStatus, setNewStatus] = useState('PENDING')

  const fetchFilters = useCallback(async () => {
    try {
      const [phaseRes, studentRes, mentorRes] = await Promise.all([
        phaseApi.getAll({ page: 0, size: 100 }),
        studentApi.getAll(),
        mentorApi.getAll()
      ])
      setPhases(phaseRes.data.data.items || [])
      setStudents(studentRes.data.data.items || [])
      setMentors(mentorRes.data.data.items || [])
    } catch (err) {
      console.error('Failed to load filter options')
    }
  }, [])

  const fetchAssignments = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size: 10 }

      // Role based automatic filters
      if (isStudent) {
        params.studentId = currentUser?.userId
      } else if (isMentor) {
        params.mentorId = currentUser?.userId
      } else {
        // Admin manually selected filters
        if (selectedPhase) params.phaseId = selectedPhase
        if (selectedStudent) params.studentId = selectedStudent
        if (selectedMentor) params.mentorId = selectedMentor
      }

      const res = await assignmentApi.getAll(params, { signal: abortRef.current?.signal })
      setAssignments(res.data.data.items || [])
      setPagination(res.data.data.pagination)
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách phân công')
      }
    } finally {
      setLoading(false)
    }
  }, [page, isStudent, isMentor, currentUser?.userId, selectedPhase, selectedStudent, selectedMentor])

  useEffect(() => {
    fetchFilters()
  }, [fetchFilters])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchAssignments()
    return () => { abortRef.current?.abort() }
  }, [fetchAssignments])

  const openCreate = () => {
    setEditingAssignment(null)
    setForm({ studentId: '', mentorId: '', phaseId: '' })
    setFormOpen(true)
  }

  const openEdit = (assignment) => {
    setEditingAssignment(assignment)
    setForm({
      studentId: assignment.studentId,
      mentorId: assignment.mentorId,
      phaseId: assignment.phaseId
    })
    setFormOpen(true)
  }

  const openStatus = (assignment) => {
    setStatusAssignment(assignment)
    setNewStatus(assignment.status)
    setStatusOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      const studentId = parseInt(form.studentId)
      const mentorId = parseInt(form.mentorId)
      const phaseId = parseInt(form.phaseId)
      if (isNaN(studentId) || isNaN(mentorId) || isNaN(phaseId)) {
        toast.error('Vui lòng chọn đầy đủ thông tin')
        setFormLoading(false)
        return
      }
      const payload = {
        studentId,
        mentorId,
        phaseId
      }
      if (editingAssignment) {
        await assignmentApi.update(editingAssignment.id, payload)
        toast.success('Cập nhật phân công thành công!')
      } else {
        await assignmentApi.create(payload)
        toast.success('Tạo phân công thành công!')
      }
      setFormOpen(false)
      fetchAssignments()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleStatusSubmit = async (e) => {
    e.preventDefault()
    setStatusLoading(true)
    try {
      await assignmentApi.updateStatus(statusAssignment.id, { status: newStatus })
      toast.success('Cập nhật trạng thái thành công!')
      setStatusOpen(false)
      fetchAssignments()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setStatusLoading(false)
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await assignmentApi.delete(deletingAssignment.id)
      toast.success('Xóa phân công thành công!')
      setDeleteOpen(false)
      fetchAssignments()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setDeleteLoading(false)
    }
  }

  const columns = [
    { key: 'id', title: 'ID', width: '60px' },
    { key: 'studentName', title: 'Sinh viên', render: (val, row) => `${val} (${row.studentCode})` },
    { key: 'mentorName', title: 'Người hướng dẫn' },
    { key: 'phaseName', title: 'Đợt thực tập' },
    {
      key: 'status', title: 'Trạng thái',
      render: (val) => (
        <span className={`px-2.5 py-1 rounded-full text-xs font-semibold ${
          val === 'PENDING' ? 'bg-orange-100 text-orange-700' :
          val === 'IN_PROGRESS' ? 'bg-blue-100 text-blue-700' :
          val === 'COMPLETED' ? 'bg-emerald-100 text-emerald-700' :
          'bg-red-100 text-red-700'
        }`}>{val}</span>
      )
    },
    {
      key: 'actions', title: '',
      render: (_, row) => (
        <div className="flex gap-1">
          {isAdmin && (
            <>
              <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
                disabled={loading}
                className="p-1.5 rounded-lg hover:bg-secondary transition-colors disabled:opacity-50" title="Sửa thông tin">
                <Edit className="w-4 h-4 text-muted-foreground" />
              </button>
              <button onClick={(e) => { e.stopPropagation(); openStatus(row) }}
                disabled={loading}
                className="p-1.5 rounded-lg hover:bg-secondary transition-colors disabled:opacity-50" title="Đổi trạng thái">
                <CheckCircle2 className="w-4 h-4 text-primary" />
              </button>
              <button onClick={(e) => { e.stopPropagation(); setDeletingAssignment(row); setDeleteOpen(true) }}
                disabled={loading}
                className="p-1.5 rounded-lg hover:bg-red-50 transition-colors disabled:opacity-50" title="Xóa phân công">
                <Trash2 className="w-4 h-4 text-destructive" />
              </button>
            </>
          )}
          {isMentor && (
            <button onClick={(e) => { e.stopPropagation(); openStatus(row) }}
              disabled={loading}
              className="p-1.5 rounded-lg hover:bg-secondary transition-colors disabled:opacity-50" title="Đổi trạng thái">
              <CheckCircle2 className="w-4 h-4 text-primary" />
            </button>
          )}
        </div>
      )
    },
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Phân công Thực tập</h1>
          <p className="text-muted-foreground text-sm">Quản lý việc phân công sinh viên cho giảng viên hướng dẫn</p>
        </div>
        {isAdmin && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm phân công
          </button>
        )}
      </div>

      {/* Admin Filters */}
      {isAdmin && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 bg-white p-4 rounded-xl border border-border">
          <div>
            <label className="block text-xs font-semibold text-muted-foreground uppercase mb-1">Đợt thực tập</label>
            <select value={selectedPhase} onChange={e => { setSelectedPhase(e.target.value); setPage(0) }}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white">
              <option value="">Tất cả</option>
              {phases.map(p => <option key={p.id} value={p.id}>{p.phaseName}</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-muted-foreground uppercase mb-1">Sinh viên</label>
            <select value={selectedStudent} onChange={e => { setSelectedStudent(e.target.value); setPage(0) }}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white">
              <option value="">Tất cả</option>
              {students.map(s => <option key={s.id} value={s.id}>{s.fullName} ({s.studentCode})</option>)}
            </select>
          </div>
          <div>
            <label className="block text-xs font-semibold text-muted-foreground uppercase mb-1">Giảng viên hướng dẫn</label>
            <select value={selectedMentor} onChange={e => { setSelectedMentor(e.target.value); setPage(0) }}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white">
              <option value="">Tất cả</option>
              {mentors.map(m => <option key={m.id} value={m.id}>{m.fullName}</option>)}
            </select>
          </div>
        </div>
      )}

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={assignments}
          pagination={pagination}
          onPageChange={setPage}
          emptyMessage="Chưa có phân công thực tập nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingAssignment ? 'Chỉnh sửa Phân công' : 'Thêm Phân công mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        <div>
          <label className="block text-sm font-medium mb-1">Đợt thực tập</label>
          <select value={form.phaseId} onChange={e => setForm({ ...form, phaseId: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
            <option value="">-- Chọn Đợt --</option>
            {phases.map(p => <option key={p.id} value={p.id}>{p.phaseName}</option>)}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Sinh viên</label>
          <select value={form.studentId} onChange={e => setForm({ ...form, studentId: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
            <option value="">-- Chọn Sinh viên --</option>
            {students.map(s => <option key={s.id} value={s.id}>{s.fullName} ({s.studentCode})</option>)}
          </select>
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Giảng viên hướng dẫn</label>
          <select value={form.mentorId} onChange={e => setForm({ ...form, mentorId: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
            <option value="">-- Chọn Giảng viên --</option>
            {mentors.map(m => <option key={m.id} value={m.id}>{m.fullName}</option>)}
          </select>
        </div>
      </FormDialog>

      {/* Status Update Dialog */}
      <FormDialog
        open={statusOpen}
        onClose={() => setStatusOpen(false)}
        title="Cập nhật Trạng thái Phân công"
        onSubmit={handleStatusSubmit}
        loading={statusLoading}
        submitText="Cập nhật"
      >
        <div>
          <label className="block text-sm font-medium mb-1">Trạng thái mới</label>
          <select value={newStatus} onChange={e => setNewStatus(e.target.value)}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white" required>
            <option value="PENDING">PENDING</option>
            <option value="IN_PROGRESS">IN_PROGRESS</option>
            <option value="COMPLETED">COMPLETED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </div>
      </FormDialog>

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        loading={deleteLoading}
        message={`Bạn có chắc muốn xóa phân công cho sinh viên "${deletingAssignment?.studentName}"?`}
      />
    </div>
  )
}

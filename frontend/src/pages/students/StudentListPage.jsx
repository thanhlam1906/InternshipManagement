import { useState, useEffect, useCallback } from 'react'
import { studentApi, userApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function StudentListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'
  const isMentor = currentUser?.role === 'MENTOR'
  const isStudent = currentUser?.role === 'STUDENT'

  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [editingStudent, setEditingStudent] = useState(null)
  const [deletingStudent, setDeletingStudent] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  
  // Available users to link
  const [studentUsers, setStudentUsers] = useState([])

  // Form state
  const [form, setForm] = useState({
    userId: '',
    studentCode: '',
    major: '',
    clazz: '',
    dateOfBirth: '',
    address: ''
  })

  const fetchStudents = useCallback(async () => {
    setLoading(true)
    try {
      if (isStudent) {
        // A student might only get their own profile. Let's see what backend returns.
        // Actually /students endpoint returns list. If it fails for students, we'll try to fetch by id.
        try {
          const res = await studentApi.getById(currentUser.userId)
          setStudents([res.data.data])
        } catch {
          setStudents([])
        }
      } else {
        const res = await studentApi.getAll()
        setStudents(res.data.data || [])
      }
    } catch (err) {
      toast.error('Không thể tải danh sách sinh viên')
    } finally {
      setLoading(false)
    }
  }, [isStudent, currentUser])

  const fetchStudentUsers = async () => {
    try {
      const res = await userApi.getAll({ role: 'STUDENT', size: 100 })
      // Filter out users who already have student profiles
      const usersList = res.data.data.items || []
      const existingUserIds = students.map(s => s.id) // backend StudentResponse maps userId as 'id' or student has it.
      // Wait, let's filter out based on existing student records
      const filtered = usersList.filter(u => !students.some(s => s.username === u.username))
      setStudentUsers(filtered)
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => {
    fetchStudents()
  }, [fetchStudents])

  const openCreate = () => {
    setEditingStudent(null)
    setForm({
      userId: '',
      studentCode: '',
      major: '',
      clazz: '',
      dateOfBirth: '',
      address: ''
    })
    fetchStudentUsers()
    setFormOpen(true)
  }

  const openEdit = (student) => {
    setEditingStudent(student)
    setForm({
      userId: student.id,
      studentCode: student.studentCode || '',
      major: student.major || '',
      clazz: student.clazz || '',
      dateOfBirth: student.dateOfBirth || '',
      address: student.address || ''
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      if (editingStudent) {
        // Update uses studentId
        await studentApi.update(editingStudent.id, {
          studentCode: form.studentCode,
          major: form.major,
          clazz: form.clazz,
          dateOfBirth: form.dateOfBirth ? form.dateOfBirth : null,
          address: form.address
        })
        toast.success('Cập nhật thông tin sinh viên thành công!')
      } else {
        if (!form.userId) {
          toast.error('Vui lòng chọn tài khoản liên kết')
          setFormLoading(false)
          return
        }
        await studentApi.create({
          userId: parseInt(form.userId),
          studentCode: form.studentCode,
          major: form.major,
          clazz: form.clazz,
          dateOfBirth: form.dateOfBirth ? form.dateOfBirth : null,
          address: form.address
        })
        toast.success('Tạo thông tin sinh viên thành công!')
      }
      setFormOpen(false)
      fetchStudents()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      await studentApi.delete(deletingStudent.id)
      toast.success('Xóa thông tin sinh viên thành công!')
      setDeleteOpen(false)
      fetchStudents()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    }
  }

  const columns = [
    { key: 'studentCode', title: 'Mã SV' },
    { key: 'fullName', title: 'Họ tên' },
    { key: 'clazz', title: 'Lớp' },
    { key: 'major', title: 'Ngành' },
    { key: 'email', title: 'Email' },
    { key: 'phoneNumber', title: 'SĐT' },
    {
      key: 'actions', title: '',
      render: (_, row) => {
        // Admin has full CRUD, Student can edit own profile
        const canEdit = isAdmin || (isStudent && row.id === currentUser.userId)
        const canDelete = isAdmin
        
        if (!canEdit && !canDelete) return null

        return (
          <div className="flex gap-1">
            {canEdit && (
              <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
                className="p-1.5 rounded-lg hover:bg-secondary transition-colors">
                <Edit className="w-4 h-4 text-muted-foreground" />
              </button>
            )}
            {canDelete && (
              <button onClick={(e) => { e.stopPropagation(); setDeletingStudent(row); setDeleteOpen(true) }}
                className="p-1.5 rounded-lg hover:bg-red-50 transition-colors">
                <Trash2 className="w-4 h-4 text-destructive" />
              </button>
            )}
          </div>
        )
      }
    },
  ]

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Quản lý Sinh viên</h1>
          <p className="text-muted-foreground text-sm">Hồ sơ thông tin chi tiết của sinh viên thực tập</p>
        </div>
        {isAdmin && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm hồ sơ SV
          </button>
        )}
      </div>

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={students}
          emptyMessage="Chưa có hồ sơ sinh viên nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingStudent ? 'Chỉnh sửa hồ sơ Sinh viên' : 'Tạo hồ sơ Sinh viên mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        {!editingStudent && (
          <div>
            <label className="block text-sm font-medium mb-1">Tài khoản liên kết</label>
            <select
              value={form.userId}
              onChange={e => setForm({ ...form, userId: e.target.value })}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 bg-white"
              required
            >
              <option value="">-- Chọn User --</option>
              {studentUsers.map(u => (
                <option key={u.userId} value={u.userId}>{u.fullName} ({u.username})</option>
              ))}
            </select>
          </div>
        )}
        
        <div>
          <label className="block text-sm font-medium mb-1">Mã sinh viên</label>
          <input
            type="text"
            value={form.studentCode}
            onChange={e => setForm({ ...form, studentCode: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Ngành học</label>
          <input
            type="text"
            value={form.major}
            onChange={e => setForm({ ...form, major: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Lớp</label>
          <input
            type="text"
            value={form.clazz}
            onChange={e => setForm({ ...form, clazz: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Ngày sinh</label>
          <input
            type="date"
            value={form.dateOfBirth}
            onChange={e => setForm({ ...form, dateOfBirth: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Địa chỉ</label>
          <textarea
            value={form.address}
            onChange={e => setForm({ ...form, address: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 min-h-[80px]"
          />
        </div>
      </FormDialog>

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        message={`Bạn có chắc muốn xóa hồ sơ sinh viên "${deletingStudent?.fullName}"?`}
      />
    </div>
  )
}

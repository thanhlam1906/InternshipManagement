import { useState, useEffect, useCallback, useRef } from 'react'
import { mentorApi, userApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2 } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function MentorListPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'

  const [mentors, setMentors] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [editingMentor, setEditingMentor] = useState(null)
  const [deletingMentor, setDeletingMentor] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const abortRef = useRef(null)

  // Available users to link
  const [mentorUsers, setMentorUsers] = useState([])

  // Form state
  const [form, setForm] = useState({
    userId: '',
    department: '',
    academicRank: ''
  })

  const fetchMentors = useCallback(async () => {
    setLoading(true)
    try {
      const res = await mentorApi.getAll({ signal: abortRef.current?.signal })
      setMentors(res.data.data.items || [])
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách giảng viên')
      }
    } finally {
      setLoading(false)
    }
  }, [])

  const fetchMentorUsers = async () => {
    try {
      const res = await userApi.getAll({ role: 'MENTOR', size: 100 }, { signal: abortRef.current?.signal })
      const usersList = res.data.data.items || []
      const filtered = usersList.filter(u => !mentors.some(m => m.username === u.username))
      setMentorUsers(filtered)
    } catch (err) {
      console.error(err)
    }
  }

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchMentors()
    return () => { abortRef.current?.abort() }
  }, [fetchMentors])

  const openCreate = () => {
    setEditingMentor(null)
    setForm({
      userId: '',
      department: '',
      academicRank: ''
    })
    fetchMentorUsers()
    setFormOpen(true)
  }

  const openEdit = (mentor) => {
    setEditingMentor(mentor)
    setForm({
      userId: '',
      department: mentor.department || '',
      academicRank: mentor.academicRank || ''
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      if (editingMentor) {
        await mentorApi.update(editingMentor.id, {
          department: form.department,
          academicRank: form.academicRank
        })
        toast.success('Cập nhật thông tin giảng viên thành công!')
      } else {
        if (!form.userId) {
          toast.error('Vui lòng chọn tài khoản liên kết')
          setFormLoading(false)
          return
        }
        const parsedUserId = parseInt(form.userId)
        if (isNaN(parsedUserId)) {
          toast.error('ID tài khoản không hợp lệ')
          setFormLoading(false)
          return
        }
        await mentorApi.create({
          userId: parsedUserId,
          department: form.department,
          academicRank: form.academicRank
        })
        toast.success('Tạo thông tin giảng viên thành công!')
      }
      setFormOpen(false)
      fetchMentors()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await mentorApi.delete(deletingMentor.id)
      toast.success('Xóa thông tin giảng viên thành công!')
      setDeleteOpen(false)
      fetchMentors()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setDeleteLoading(false)
    }
  }

  const columns = [
    { key: 'fullName', title: 'Họ tên' },
    { key: 'department', title: 'Khoa/Bộ môn' },
    { key: 'academicRank', title: 'Học hàm/Học vị' },
    { key: 'email', title: 'Email' },
    { key: 'phoneNumber', title: 'SĐT', render: (val) => val || '--' },
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
            <button onClick={(e) => { e.stopPropagation(); setDeletingMentor(row); setDeleteOpen(true) }}
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
          <h1 className="text-2xl font-bold">Quản lý Giảng viên</h1>
          <p className="text-muted-foreground text-sm">Hồ sơ thông tin chi tiết của giảng viên hướng dẫn</p>
        </div>
        {isAdmin && (
          <button onClick={openCreate}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Thêm giảng viên
          </button>
        )}
      </div>

      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={mentors}
          emptyMessage="Chưa có hồ sơ giảng viên nào"
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingMentor ? 'Chỉnh sửa hồ sơ Giảng viên' : 'Tạo hồ sơ Giảng viên mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        {!editingMentor && (
          <div>
            <label className="block text-sm font-medium mb-1">Tài khoản liên kết</label>
            <select
              value={form.userId}
              onChange={e => setForm({ ...form, userId: e.target.value })}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 bg-white"
              required
            >
              <option value="">-- Chọn User --</option>
              {mentorUsers.map(u => (
                <option key={u.userId} value={u.userId}>{u.fullName} ({u.username})</option>
              ))}
            </select>
          </div>
        )}

        <div>
          <label className="block text-sm font-medium mb-1">Khoa / Bộ môn</label>
          <input
            type="text"
            value={form.department}
            onChange={e => setForm({ ...form, department: e.target.value })}
            maxLength={100}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20"
            required
          />
        </div>

        <div>
          <label className="block text-sm font-medium mb-1">Học hàm / Học vị</label>
          <input
            type="text"
            value={form.academicRank}
            onChange={e => setForm({ ...form, academicRank: e.target.value })}
            maxLength={100}
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
        message={`Bạn có chắc muốn xóa hồ sơ giảng viên "${deletingMentor?.fullName}"?`}
      />
    </div>
  )
}

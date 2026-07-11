import { useState, useEffect, useCallback, useRef } from 'react'
import { userApi } from '@/services/api'
import DataTable from '@/components/DataTable'
import DeleteDialog from '@/components/DeleteDialog'
import FormDialog from '@/components/FormDialog'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2, Shield, ToggleLeft, ToggleRight } from 'lucide-react'
import toast from 'react-hot-toast'

export default function UserListPage() {
  const [users, setUsers] = useState([])
  const [pagination, setPagination] = useState(null)
  const [loading, setLoading] = useState(true)
  const [page, setPage] = useState(0)
  const [roleFilter, setRoleFilter] = useState('')
  const [formOpen, setFormOpen] = useState(false)
  const [deleteOpen, setDeleteOpen] = useState(false)
  const [editingUser, setEditingUser] = useState(null)
  const [deletingUser, setDeletingUser] = useState(null)
  const [formLoading, setFormLoading] = useState(false)
  const [deleteLoading, setDeleteLoading] = useState(false)
  const [toggleLoadingId, setToggleLoadingId] = useState(null)
  const abortRef = useRef(null)

  // Form state
  const [form, setForm] = useState({
    username: '', password: '', fullName: '', email: '', phoneNumber: '', role: 'STUDENT'
  })

  const fetchUsers = useCallback(async () => {
    setLoading(true)
    try {
      const params = { page, size: 10 }
      if (roleFilter) params.role = roleFilter
      const res = await userApi.getAll(params, { signal: abortRef.current?.signal })
      setUsers(res.data.data.items)
      setPagination(res.data.data.pagination)
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách users')
      }
    } finally {
      setLoading(false)
    }
  }, [page, roleFilter])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchUsers()
    return () => { abortRef.current?.abort() }
  }, [fetchUsers])

  const openCreate = () => {
    setEditingUser(null)
    setForm({ username: '', password: '', fullName: '', email: '', phoneNumber: '', role: 'STUDENT' })
    setFormOpen(true)
  }

  const openEdit = (user) => {
    setEditingUser(user)
    setForm({
      username: user.username, password: '', fullName: user.fullName,
      email: user.email || '', phoneNumber: user.phoneNumber || '', role: user.role
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setFormLoading(true)
    try {
      if (editingUser) {
        await userApi.update(editingUser.userId, {
          fullName: form.fullName, email: form.email, phoneNumber: form.phoneNumber
        })
        toast.success('Cập nhật thành công!')
      } else {
        await userApi.create(form)
        toast.success('Tạo user thành công!')
      }
      setFormOpen(false)
      fetchUsers()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const handleDelete = async () => {
    setDeleteLoading(true)
    try {
      await userApi.delete(deletingUser.userId)
      toast.success('Xóa thành công!')
      setDeleteOpen(false)
      fetchUsers()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setDeleteLoading(false)
    }
  }

  const handleToggleStatus = async (user) => {
    setToggleLoadingId(user.userId)
    try {
      await userApi.updateStatus(user.userId, { isActive: !user.isActive })
      toast.success('Cập nhật trạng thái thành công!')
      fetchUsers()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setToggleLoadingId(null)
    }
  }

  const columns = [
    { key: 'userId', title: 'ID', width: '60px' },
    { key: 'username', title: 'Username' },
    { key: 'fullName', title: 'Họ tên' },
    { key: 'email', title: 'Email' },
    {
      key: 'role', title: 'Vai trò',
      render: (val) => (
        <span className={`px-2 py-1 rounded-full text-xs font-medium ${
          val === 'ADMIN' ? 'bg-red-100 text-red-700' :
          val === 'MENTOR' ? 'bg-purple-100 text-purple-700' :
          'bg-blue-100 text-blue-700'
        }`}>{val}</span>
      )
    },
    {
      key: 'isActive', title: 'Trạng thái',
      render: (val, row) => (
        <button
          onClick={(e) => { e.stopPropagation(); handleToggleStatus(row) }}
          disabled={toggleLoadingId === row.userId}
          className="flex items-center gap-1.5 text-xs font-medium disabled:opacity-50">
          {val ? (
            <><ToggleRight className="w-5 h-5 text-success" /> <span className="text-success">Active</span></>
          ) : (
            <><ToggleLeft className="w-5 h-5 text-muted-foreground" /> <span className="text-muted-foreground">Inactive</span></>
          )}
        </button>
      )
    },
    {
      key: 'actions', title: '',
      render: (_, row) => (
        <div className="flex gap-1">
          <button onClick={(e) => { e.stopPropagation(); openEdit(row) }}
            className="p-1.5 rounded-lg hover:bg-secondary transition-colors">
            <Edit className="w-4 h-4 text-muted-foreground" />
          </button>
          <button onClick={(e) => { e.stopPropagation(); setDeletingUser(row); setDeleteOpen(true) }}
            className="p-1.5 rounded-lg hover:bg-red-50 transition-colors">
            <Trash2 className="w-4 h-4 text-destructive" />
          </button>
        </div>
      )
    },
  ]

  return (
    <div className="space-y-4">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Quản lý Users</h1>
          <p className="text-muted-foreground text-sm">Quản lý tài khoản người dùng hệ thống</p>
        </div>
        <button onClick={openCreate}
          className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
            hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
          <Plus className="w-4 h-4" /> Thêm User
        </button>
      </div>

      {/* Filter */}
      <div className="flex gap-2">
        {['', 'ADMIN', 'MENTOR', 'STUDENT'].map((r) => (
          <button key={r} onClick={() => { setRoleFilter(r); setPage(0) }}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              roleFilter === r ? 'bg-primary text-white' : 'bg-secondary text-muted-foreground hover:bg-secondary/80'
            }`}>
            {r || 'Tất cả'}
          </button>
        ))}
      </div>

      {/* Table */}
      {loading ? <LoadingSpinner /> : (
        <DataTable
          columns={columns}
          data={users}
          pagination={pagination}
          onPageChange={setPage}
        />
      )}

      {/* Create/Edit Dialog */}
      <FormDialog
        open={formOpen}
        onClose={() => setFormOpen(false)}
        title={editingUser ? 'Chỉnh sửa User' : 'Thêm User mới'}
        onSubmit={handleSubmit}
        loading={formLoading}
      >
        <div>
          <label className="block text-sm font-medium mb-1">Username</label>
          <input type="text" value={form.username} disabled={!!editingUser}
            onChange={e => setForm({ ...form, username: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20 disabled:bg-muted" required />
        </div>
        {!editingUser && (
          <div>
            <label className="block text-sm font-medium mb-1">Mật khẩu</label>
            <input type="password" value={form.password}
              onChange={e => setForm({ ...form, password: e.target.value })}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20" required />
          </div>
        )}
        <div>
          <label className="block text-sm font-medium mb-1">Họ tên</label>
          <input type="text" value={form.fullName}
            onChange={e => setForm({ ...form, fullName: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20" required />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Email</label>
          <input type="email" value={form.email} autoComplete="email"
            onChange={e => setForm({ ...form, email: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20" />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Số điện thoại</label>
          <input type="tel" value={form.phoneNumber} autoComplete="tel"
            onChange={e => setForm({ ...form, phoneNumber: e.target.value })}
            className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20" />
        </div>
        {!editingUser && (
          <div>
            <label className="block text-sm font-medium mb-1">Vai trò</label>
            <select value={form.role}
              onChange={e => setForm({ ...form, role: e.target.value })}
              className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none focus:ring-2 focus:ring-primary/20">
              <option value="STUDENT">Sinh viên</option>
              <option value="MENTOR">Giảng viên</option>
              <option value="ADMIN">Admin</option>
            </select>
          </div>
        )}
      </FormDialog>

      {/* Delete Dialog */}
      <DeleteDialog
        open={deleteOpen}
        onClose={() => setDeleteOpen(false)}
        onConfirm={handleDelete}
        loading={deleteLoading}
        message={`Bạn có chắc muốn xóa user "${deletingUser?.username}"?`}
      />
    </div>
  )
}

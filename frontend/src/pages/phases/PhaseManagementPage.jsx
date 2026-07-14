import { useState, useEffect, useCallback, useRef } from 'react'
import { phaseApi, roundApi, criterionApi, roundCriterionApi } from '@/services/api'
import LoadingSpinner from '@/components/LoadingSpinner'
import { Plus, Edit, Trash2, ChevronDown, ChevronRight, Calendar, Award, X, GripVertical } from 'lucide-react'
import toast from 'react-hot-toast'
import { useAuth } from '@/context/AuthContext'

export default function PhaseManagementPage() {
  const { user: currentUser } = useAuth()
  const isAdmin = currentUser?.role === 'ADMIN'

  // ── Phase state ──
  const [phases, setPhases] = useState([])
  const [loading, setLoading] = useState(true)
  const [expandedPhaseId, setExpandedPhaseId] = useState(null)
  const abortRef = useRef(null)

  // ── Rounds cache: phaseId → { rounds, loading } ──
  const [roundsByPhase, setRoundsByPhase] = useState({})
  const [expandedRoundId, setExpandedRoundId] = useState(null)

  // ── Criteria cache: roundId → { criteria, loading } ──
  const [criteriaByRound, setCriteriaByRound] = useState({})

  // ── All available criteria (for dropdowns) ──
  const [allCriteria, setAllCriteria] = useState([])

  // ── Inline forms ──
  const [editingPhase, setEditingPhase] = useState(null)       // null | phase object (null=create)
  const [showPhaseForm, setShowPhaseForm] = useState(false)
  const [phaseForm, setPhaseForm] = useState({ phaseName: '', startDate: '', endDate: '', description: '' })

  const [editingRound, setEditingRound] = useState(null)       // null | round object
  const [showRoundForm, setShowRoundForm] = useState({})       // { [phaseId]: true/false }
  const [roundForm, setRoundForm] = useState({ roundName: '', startDate: '', endDate: '', description: '' })
  // Criteria to include when creating a round (round creation requires ≥1 criterion)
  const [roundCriteriaForm, setRoundCriteriaForm] = useState([{ criterionId: '', weight: '1.0' }])

  // ── Add single criterion to existing round ──
  const [addingCriterionTo, setAddingCriterionTo] = useState(null) // roundId or null
  const [addCriterionForm, setAddCriterionForm] = useState({ criterionId: '', weight: '1.0' })

  // Editing an existing round-criterion weight
  const [editingCriterionWeight, setEditingCriterionWeight] = useState(null) // { id, weight }
  const [formLoading, setFormLoading] = useState(false)

  // ===================== FETCH PHASES =====================
  const fetchPhases = useCallback(async () => {
    setLoading(true)
    try {
      const res = await phaseApi.getAll({ page: 0, size: 50 }, { signal: abortRef.current?.signal })
      setPhases(res.data.data.items || [])
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách đợt thực tập')
      }
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    abortRef.current = new AbortController()
    fetchPhases()
    return () => { abortRef.current?.abort() }
  }, [fetchPhases])

  // ===================== FETCH ROUNDS FOR A PHASE =====================
  const fetchRounds = useCallback(async (phaseId) => {
    setRoundsByPhase(prev => ({ ...prev, [phaseId]: { ...prev[phaseId], loading: true } }))
    try {
      const res = await roundApi.getAll({ phaseId, page: 0, size: 50 }, { signal: abortRef.current?.signal })
      const rounds = res.data.data.items || []
      setRoundsByPhase(prev => ({ ...prev, [phaseId]: { rounds, loading: false } }))
      return rounds
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải danh sách đợt đánh giá')
      }
      setRoundsByPhase(prev => ({ ...prev, [phaseId]: { ...prev[phaseId], loading: false } }))
      return []
    }
  }, [])

  // ===================== FETCH CRITERIA FOR A ROUND =====================
  const fetchRoundCriteria = useCallback(async (roundId) => {
    setCriteriaByRound(prev => ({ ...prev, [roundId]: { ...prev[roundId], loading: true } }))
    try {
      const res = await roundCriterionApi.getAll({ roundId }, { signal: abortRef.current?.signal })
      const criteria = res.data.data || []  // RoundCriterionController returns List, not PaginatedResponse
      setCriteriaByRound(prev => ({ ...prev, [roundId]: { criteria, loading: false } }))
      return criteria
    } catch (err) {
      if (err.name !== 'AbortError' && err.code !== 'ERR_CANCELED') {
        toast.error('Không thể tải tiêu chí của đợt đánh giá')
      }
      setCriteriaByRound(prev => ({ ...prev, [roundId]: { ...prev[roundId], loading: false } }))
      return []
    }
  }, [])

  // ===================== FETCH ALL CRITERIA (for dropdowns) =====================
  const fetchAllCriteria = useCallback(async () => {
    if (allCriteria.length > 0) return allCriteria
    try {
      const res = await criterionApi.getAll({ page: 0, size: 200 })
      const list = res.data.data.items || []
      setAllCriteria(list)
      return list
    } catch {
      return []
    }
  }, [allCriteria.length])

  // ===================== TOGGLE PHASE EXPAND =====================
  const togglePhase = useCallback((phaseId) => {
    setExpandedPhaseId(prev => {
      const next = prev === phaseId ? null : phaseId
      if (next && !roundsByPhase[next]?.rounds) {
        fetchRounds(next)
      }
      return next
    })
    setExpandedRoundId(null) // collapse any open round
  }, [roundsByPhase, fetchRounds])

  // ===================== TOGGLE ROUND EXPAND =====================
  const toggleRound = useCallback((roundId) => {
    setExpandedRoundId(prev => {
      const next = prev === roundId ? null : roundId
      if (next && !criteriaByRound[next]?.criteria) {
        fetchRoundCriteria(next)
      }
      return next
    })
  }, [criteriaByRound, fetchRoundCriteria])

  // ===================== PHASE CRUD =====================
  const openCreatePhase = () => {
    setEditingPhase(null)
    setPhaseForm({ phaseName: '', startDate: '', endDate: '', description: '' })
    setShowPhaseForm(true)
  }

  const openEditPhase = (phase) => {
    setEditingPhase(phase)
    setPhaseForm({
      phaseName: phase.phaseName || '',
      startDate: phase.startDate || '',
      endDate: phase.endDate || '',
      description: phase.description || ''
    })
    setShowPhaseForm(true)
  }

  const handlePhaseSubmit = async (e) => {
    e.preventDefault()
    if (phaseForm.startDate && phaseForm.endDate && phaseForm.endDate <= phaseForm.startDate) {
      toast.error('Ngày kết thúc phải sau ngày bắt đầu')
      return
    }
    setFormLoading(true)
    try {
      if (editingPhase) {
        await phaseApi.update(editingPhase.id, phaseForm)
        toast.success('Cập nhật đợt thực tập thành công!')
      } else {
        await phaseApi.create(phaseForm)
        toast.success('Tạo đợt thực tập thành công!')
      }
      setShowPhaseForm(false)
      fetchPhases()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const deletePhase = async (phase) => {
    try {
      await phaseApi.delete(phase.id)
      toast.success('Xóa đợt thực tập thành công!')
      setExpandedPhaseId(null)
      fetchPhases()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    }
  }

  // ===================== ROUND CRUD =====================
  const openCreateRound = async (phaseId) => {
    setEditingRound(null)
    setRoundForm({ roundName: '', startDate: '', endDate: '', description: '' })
    setRoundCriteriaForm([{ criterionId: '', weight: '1.0' }])
    await fetchAllCriteria()
    setShowRoundForm(prev => ({ ...prev, [phaseId]: true }))
  }

  const openEditRound = async (round) => {
    setEditingRound(round)
    setRoundForm({
      roundName: round.roundName || '',
      startDate: round.startDate || '',
      endDate: round.endDate || '',
      description: round.description || ''
    })
    // Pre-fill criteria form with existing criteria if available
    const existing = criteriaByRound[round.id]?.criteria || round.criteria || []
    if (existing.length > 0) {
      setRoundCriteriaForm(existing.map(c => ({ criterionId: String(c.criterionId), weight: String(c.weight) })))
    } else {
      setRoundCriteriaForm([{ criterionId: '', weight: '1.0' }])
    }
    await fetchAllCriteria()
    setShowRoundForm(prev => ({ ...prev, [round.phaseId || expandedPhaseId]: true }))
  }

  const addCriteriaRow = () => {
    setRoundCriteriaForm(prev => [...prev, { criterionId: '', weight: '1.0' }])
  }

  const removeCriteriaRow = (idx) => {
    setRoundCriteriaForm(prev => prev.filter((_, i) => i !== idx))
  }

  const updateCriteriaRow = (idx, field, value) => {
    setRoundCriteriaForm(prev => prev.map((item, i) => i === idx ? { ...item, [field]: value } : item))
  }

  const handleRoundSubmit = async (e, phaseId) => {
    e.preventDefault()
    if (roundForm.startDate && roundForm.endDate && roundForm.endDate <= roundForm.startDate) {
      toast.error('Ngày kết thúc phải sau ngày bắt đầu')
      return
    }
    // Validate criteria
    const validCriteria = roundCriteriaForm.filter(c => c.criterionId)
    if (validCriteria.length === 0) {
      toast.error('Vui lòng chọn ít nhất 1 tiêu chí cho đợt đánh giá')
      return
    }
    setFormLoading(true)
    try {
      const payload = {
        ...roundForm,
        phaseId,
        criteria: validCriteria.map(c => ({
          criterionId: parseInt(c.criterionId),
          weight: parseFloat(c.weight) || 1.0
        }))
      }
      if (editingRound) {
        // Update round (without criteria via this endpoint; criteria managed separately)
        const { criteria, ...updatePayload } = payload
        await roundApi.update(editingRound.id, updatePayload)
        toast.success('Cập nhật đợt đánh giá thành công!')
      } else {
        await roundApi.create(payload)
        toast.success('Tạo đợt đánh giá thành công!')
      }
      setShowRoundForm(prev => ({ ...prev, [phaseId]: false }))
      fetchRounds(phaseId)
      // Clear criteria cache for edited round
      if (editingRound) setCriteriaByRound(prev => { const n = { ...prev }; delete n[editingRound.id]; return n })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const deleteRound = async (round) => {
    try {
      await roundApi.delete(round.id)
      toast.success('Xóa đợt đánh giá thành công!')
      const phaseId = round.phaseId || expandedPhaseId
      fetchRounds(phaseId)
      setCriteriaByRound(prev => { const n = { ...prev }; delete n[round.id]; return n })
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    }
  }

  // ===================== CRITERIA MANAGEMENT (on existing round) =====================
  const openAddCriterion = async (roundId) => {
    setAddCriterionForm({ criterionId: '', weight: '1.0' })
    await fetchAllCriteria()
    setAddingCriterionTo(roundId)
  }

  const handleAddCriterion = async (roundId) => {
    if (!addCriterionForm.criterionId) { toast.error('Vui lòng chọn tiêu chí'); return }
    setFormLoading(true)
    try {
      await roundCriterionApi.create({
        roundId,
        criterionId: parseInt(addCriterionForm.criterionId),
        weight: parseFloat(addCriterionForm.weight) || 1.0
      })
      toast.success('Thêm tiêu chí thành công!')
      setAddingCriterionTo(null)
      fetchRoundCriteria(roundId)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const updateCriterionWeight = async (rcId, roundId) => {
    if (!editingCriterionWeight) return
    const w = parseFloat(editingCriterionWeight.weight)
    if (isNaN(w) || w <= 0) { toast.error('Trọng số không hợp lệ'); return }
    setFormLoading(true)
    try {
      await roundCriterionApi.update(rcId, { weight: w })
      toast.success('Cập nhật trọng số thành công!')
      setEditingCriterionWeight(null)
      fetchRoundCriteria(roundId)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    } finally {
      setFormLoading(false)
    }
  }

  const removeCriterion = async (rcId, roundId) => {
    try {
      await roundCriterionApi.delete(rcId)
      toast.success('Xóa tiêu chí khỏi đợt thành công!')
      fetchRoundCriteria(roundId)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Có lỗi xảy ra')
    }
  }

  // ===================== RENDER HELPERS =====================
  const getCriteriaForRound = (roundId) => criteriaByRound[roundId]?.criteria || []
  const getRoundsForPhase = (phaseId) => roundsByPhase[phaseId]?.rounds || []
  const isRoundsLoading = (phaseId) => roundsByPhase[phaseId]?.loading || false

  const unusedCriteria = (roundId) => {
    const used = new Set(getCriteriaForRound(roundId).map(c => c.criterionId))
    return allCriteria.filter(c => !used.has(c.id))
  }

  return (
    <div className="space-y-4">
      {/* ===== HEADER ===== */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold">Quản lý Đợt Thực tập</h1>
          <p className="text-muted-foreground text-sm">Quản lý đợt thực tập, đợt đánh giá và tiêu chí — tất cả trong một màn hình</p>
        </div>
        {isAdmin && (
          <button onClick={openCreatePhase}
            className="flex items-center gap-2 px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium
              hover:bg-primary/90 transition-colors shadow-lg shadow-primary/20">
            <Plus className="w-4 h-4" /> Tạo Đợt mới
          </button>
        )}
      </div>

      {/* ===== PHASE FORM DIALOG ===== */}
      {showPhaseForm && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center">
          <div className="absolute inset-0 bg-black/50 backdrop-blur-sm" onClick={() => setShowPhaseForm(false)} />
          <div className="relative bg-white rounded-xl shadow-2xl w-full max-w-lg mx-4 p-0 animate-in fade-in zoom-in-95">
            <div className="flex items-center justify-between px-6 py-4 border-b border-border">
              <h3 className="text-lg font-semibold">{editingPhase ? 'Chỉnh sửa Đợt thực tập' : 'Tạo Đợt thực tập mới'}</h3>
              <button onClick={() => setShowPhaseForm(false)}><X className="w-4 h-4" /></button>
            </div>
            <form onSubmit={handlePhaseSubmit} className="px-6 py-4 space-y-4">
              <div>
                <label className="block text-sm font-medium mb-1">Tên đợt thực tập</label>
                <input type="text" value={phaseForm.phaseName} onChange={e => setPhaseForm({ ...phaseForm, phaseName: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg border border-input text-sm" required maxLength={100} />
              </div>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Ngày bắt đầu</label>
                  <input type="date" value={phaseForm.startDate} onChange={e => setPhaseForm({ ...phaseForm, startDate: e.target.value })}
                    className="w-full px-3 py-2 rounded-lg border border-input text-sm" required />
                </div>
                <div>
                  <label className="block text-sm font-medium mb-1">Ngày kết thúc</label>
                  <input type="date" value={phaseForm.endDate} onChange={e => setPhaseForm({ ...phaseForm, endDate: e.target.value })}
                    min={phaseForm.startDate || undefined} className="w-full px-3 py-2 rounded-lg border border-input text-sm" required />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Mô tả</label>
                <textarea value={phaseForm.description} onChange={e => setPhaseForm({ ...phaseForm, description: e.target.value })}
                  className="w-full px-3 py-2 rounded-lg border border-input text-sm min-h-[80px]" maxLength={1000} />
              </div>
              <div className="flex gap-3 justify-end pt-2 border-t border-border">
                <button type="button" onClick={() => setShowPhaseForm(false)}
                  className="px-4 py-2 rounded-lg border border-input text-sm">Hủy</button>
                <button type="submit" disabled={formLoading}
                  className="px-4 py-2 rounded-lg bg-primary text-white text-sm font-medium disabled:opacity-50">
                  {formLoading ? 'Đang lưu...' : 'Lưu'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ===== PHASE LIST ===== */}
      {loading ? <LoadingSpinner text="Đang tải đợt thực tập..." /> : phases.length === 0 ? (
        <div className="bg-white rounded-xl border border-border p-12 text-center text-muted-foreground">
          <Calendar className="w-12 h-12 mx-auto text-muted-foreground/30 mb-3" />
          <p className="text-sm">Chưa có đợt thực tập nào</p>
          {isAdmin && <p className="text-xs text-muted-foreground/70 mt-1">Nhấn "Tạo Đợt mới" để bắt đầu</p>}
        </div>
      ) : (
        <div className="space-y-3">
          {phases.map(phase => {
            const isExpanded = expandedPhaseId === phase.id
            const rounds = getRoundsForPhase(phase.id)
            const roundsLoading = isRoundsLoading(phase.id)

            return (
              <div key={phase.id} className="bg-white rounded-xl border border-border shadow-sm overflow-hidden">
                {/* ===== PHASE HEADER ===== */}
                <button onClick={() => togglePhase(phase.id)} className="w-full p-4 text-left hover:bg-secondary/30 transition-colors">
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex items-center gap-3 min-w-0">
                      <div className="shrink-0 text-muted-foreground mt-0.5">
                        {isExpanded ? <ChevronDown className="w-5 h-5" /> : <ChevronRight className="w-5 h-5" />}
                      </div>
                      <div className="min-w-0">
                        <h3 className="font-semibold text-foreground truncate">{phase.phaseName}</h3>
                        <p className="text-xs text-muted-foreground mt-0.5">
                          <Calendar className="w-3 h-3 inline mr-1" />
                          {phase.startDate} — {phase.endDate}
                          {phase.description && <span className="ml-3 italic">"{phase.description}"</span>}
                        </p>
                      </div>
                    </div>
                    {isAdmin && (
                      <div className="flex gap-1 shrink-0" onClick={e => e.stopPropagation()}>
                        <button onClick={() => openEditPhase(phase)}
                          className="p-1.5 rounded-lg hover:bg-secondary transition-colors" title="Sửa đợt">
                          <Edit className="w-4 h-4 text-muted-foreground" />
                        </button>
                        <button onClick={() => deletePhase(phase)}
                          className="p-1.5 rounded-lg hover:bg-red-50 transition-colors" title="Xóa đợt">
                          <Trash2 className="w-4 h-4 text-destructive" />
                        </button>
                      </div>
                    )}
                  </div>
                </button>

                {/* ===== EXPANDED: ROUNDS ===== */}
                {isExpanded && (
                  <div className="border-t border-border bg-secondary/10 px-4 py-3 space-y-3 animate-in fade-in slide-in-from-top-2 duration-200">
                    {/* Round form */}
                    {showRoundForm[phase.id] && (
                      <div className="bg-white rounded-lg border border-border p-4">
                        <h4 className="font-semibold text-sm mb-3">{editingRound ? 'Chỉnh sửa Đợt đánh giá' : 'Tạo Đợt đánh giá mới'}</h4>
                        <form onSubmit={e => handleRoundSubmit(e, phase.id)} className="space-y-3">
                          <div>
                            <label className="block text-xs font-medium mb-0.5">Tên đợt</label>
                            <input type="text" value={roundForm.roundName} onChange={e => setRoundForm({ ...roundForm, roundName: e.target.value })}
                              className="w-full px-3 py-1.5 rounded-lg border border-input text-sm" required maxLength={100} />
                          </div>
                          <div className="grid grid-cols-2 gap-3">
                            <div>
                              <label className="block text-xs font-medium mb-0.5">Ngày bắt đầu</label>
                              <input type="date" value={roundForm.startDate} onChange={e => setRoundForm({ ...roundForm, startDate: e.target.value })}
                                className="w-full px-3 py-1.5 rounded-lg border border-input text-sm" required />
                            </div>
                            <div>
                              <label className="block text-xs font-medium mb-0.5">Ngày kết thúc</label>
                              <input type="date" value={roundForm.endDate} onChange={e => setRoundForm({ ...roundForm, endDate: e.target.value })}
                                min={roundForm.startDate || undefined} className="w-full px-3 py-1.5 rounded-lg border border-input text-sm" required />
                            </div>
                          </div>
                          <div>
                            <label className="block text-xs font-medium mb-0.5">Mô tả</label>
                            <input type="text" value={roundForm.description} onChange={e => setRoundForm({ ...roundForm, description: e.target.value })}
                              className="w-full px-3 py-1.5 rounded-lg border border-input text-sm" maxLength={500} />
                          </div>

                          {/* Criteria selection (required for round creation) */}
                          <div className="border-t border-border pt-3">
                            <div className="flex items-center justify-between mb-2">
                              <label className="text-xs font-semibold">Tiêu chí ({roundCriteriaForm.filter(c => c.criterionId).length} đã chọn)</label>
                              <button type="button" onClick={addCriteriaRow}
                                className="text-xs text-primary font-medium hover:underline">+ Thêm tiêu chí</button>
                            </div>
                            {roundCriteriaForm.map((rc, idx) => (
                              <div key={idx} className="flex gap-2 items-center mb-2">
                                <select value={rc.criterionId} onChange={e => updateCriteriaRow(idx, 'criterionId', e.target.value)}
                                  className="flex-1 px-2 py-1.5 rounded-lg border border-input text-xs bg-white">
                                  <option value="">-- Chọn tiêu chí --</option>
                                  {allCriteria.map(c => (
                                    <option key={c.id} value={c.id}>{c.criterionName} (max: {c.maxScore})</option>
                                  ))}
                                </select>
                                <input type="number" step="0.01" min="0.01" value={rc.weight}
                                  onChange={e => updateCriteriaRow(idx, 'weight', e.target.value)}
                                  placeholder="Trọng số" className="w-20 px-2 py-1.5 rounded-lg border border-input text-xs" />
                                {roundCriteriaForm.length > 1 && (
                                  <button type="button" onClick={() => removeCriteriaRow(idx)}
                                    className="p-1 text-red-500 hover:bg-red-50 rounded"><X className="w-3.5 h-3.5" /></button>
                                )}
                              </div>
                            ))}
                            {roundCriteriaForm.filter(c => c.criterionId).length === 0 && (
                              <p className="text-xs text-muted-foreground">Cần ít nhất 1 tiêu chí để tạo đợt đánh giá</p>
                            )}
                          </div>

                          <div className="flex gap-2 justify-end pt-2 border-t border-border">
                            <button type="button" onClick={() => setShowRoundForm(prev => ({ ...prev, [phase.id]: false }))}
                              className="px-3 py-1.5 rounded-lg border border-input text-xs">Hủy</button>
                            <button type="submit" disabled={formLoading}
                              className="px-3 py-1.5 rounded-lg bg-primary text-white text-xs font-medium disabled:opacity-50">
                              {formLoading ? 'Đang lưu...' : 'Lưu'}
                            </button>
                          </div>
                        </form>
                      </div>
                    )}

                    {/* Rounds list */}
                    {roundsLoading ? <LoadingSpinner text="Đang tải đợt đánh giá..." /> : rounds.length === 0 && !showRoundForm[phase.id] ? (
                      <div className="text-center py-4 text-muted-foreground text-sm">
                        <Award className="w-6 h-6 mx-auto text-muted-foreground/30 mb-1" />
                        <p>Chưa có đợt đánh giá nào</p>
                      </div>
                    ) : (
                      rounds.map(round => {
                        const roundCriteria = getCriteriaForRound(round.id)
                        const isRoundExpanded = expandedRoundId === round.id
                        const criteriaLoading = criteriaByRound[round.id]?.loading

                        return (
                          <div key={round.id} className="bg-white rounded-lg border border-border overflow-hidden">
                            {/* Round header */}
                            <button onClick={() => toggleRound(round.id)} className="w-full p-3 text-left hover:bg-secondary/20 transition-colors">
                              <div className="flex items-center justify-between gap-2">
                                <div className="flex items-center gap-2 min-w-0">
                                  <span className="text-muted-foreground shrink-0">
                                    {isRoundExpanded ? <ChevronDown className="w-4 h-4" /> : <ChevronRight className="w-4 h-4" />}
                                  </span>
                                  <div className="min-w-0">
                                    <p className="text-sm font-medium truncate">{round.roundName}</p>
                                    <p className="text-xs text-muted-foreground">{round.startDate} — {round.endDate}</p>
                                  </div>
                                </div>
                                {isAdmin && (
                                  <div className="flex gap-1 shrink-0" onClick={e => e.stopPropagation()}>
                                    <button onClick={() => openEditRound(round)}
                                      className="p-1 rounded hover:bg-secondary transition-colors" title="Sửa đợt đánh giá">
                                      <Edit className="w-3.5 h-3.5 text-muted-foreground" />
                                    </button>
                                    <button onClick={() => deleteRound(round)}
                                      className="p-1 rounded hover:bg-red-50 transition-colors" title="Xóa đợt đánh giá">
                                      <Trash2 className="w-3.5 h-3.5 text-destructive" />
                                    </button>
                                  </div>
                                )}
                              </div>
                            </button>

                            {/* Expanded round: criteria */}
                            {isRoundExpanded && (
                              <div className="border-t border-border bg-secondary/5 px-4 py-3 space-y-2 animate-in fade-in slide-in-from-top-1 duration-150">
                                {criteriaLoading ? <LoadingSpinner text="Đang tải tiêu chí..." /> : (
                                  <>
                                    {roundCriteria.map(rc => (
                                      <div key={rc.id} className="flex items-center justify-between py-1.5 px-3 bg-white rounded-lg border border-border">
                                        <div className="flex-1 min-w-0">
                                          <span className="text-sm">{rc.criterionName}</span>
                                          <span className="text-xs text-muted-foreground ml-2">(tối đa: {rc.maxScore})</span>
                                        </div>
                                        <div className="flex items-center gap-2 shrink-0">
                                          {editingCriterionWeight?.id === rc.id ? (
                                            <div className="flex items-center gap-1">
                                              <input type="number" step="0.01" min="0.01"
                                                value={editingCriterionWeight.weight}
                                                onChange={e => setEditingCriterionWeight({ ...editingCriterionWeight, weight: e.target.value })}
                                                className="w-16 px-1.5 py-0.5 rounded border border-input text-xs" />
                                              <button onClick={() => updateCriterionWeight(rc.id, round.id)}
                                                className="text-xs text-primary font-medium">Lưu</button>
                                              <button onClick={() => setEditingCriterionWeight(null)}
                                                className="text-xs text-muted-foreground">Hủy</button>
                                            </div>
                                          ) : (
                                            <span className="text-xs font-medium text-primary bg-primary/5 px-2 py-0.5 rounded-full">
                                              Trọng số: {rc.weight}
                                            </span>
                                          )}
                                          {isAdmin && (
                                            <>
                                              <button onClick={() => setEditingCriterionWeight({ id: rc.id, weight: String(rc.weight) })}
                                                className="p-1 rounded hover:bg-secondary transition-colors" title="Sửa trọng số">
                                                <Edit className="w-3 h-3 text-muted-foreground" />
                                              </button>
                                              <button onClick={() => removeCriterion(rc.id, round.id)}
                                                className="p-1 rounded hover:bg-red-50 transition-colors" title="Xóa tiêu chí">
                                                <X className="w-3 h-3 text-destructive" />
                                              </button>
                                            </>
                                          )}
                                        </div>
                                      </div>
                                    ))}

                                    {/* Add new criterion to existing round */}
                                    {isAdmin && (
                                      addingCriterionTo === round.id ? (
                                        <div className="flex gap-2 items-center py-1.5 px-3 bg-white rounded-lg border border-dashed border-primary/30">
                                          <select value={addCriterionForm.criterionId}
                                            onChange={e => setAddCriterionForm({ ...addCriterionForm, criterionId: e.target.value })}
                                            className="flex-1 px-2 py-1 rounded border border-input text-xs bg-white">
                                            <option value="">-- Chọn tiêu chí --</option>
                                            {unusedCriteria(round.id).map(c => (
                                              <option key={c.id} value={c.id}>{c.criterionName} (max: {c.maxScore})</option>
                                            ))}
                                          </select>
                                          <input type="number" step="0.01" min="0.01" value={addCriterionForm.weight}
                                            onChange={e => setAddCriterionForm({ ...addCriterionForm, weight: e.target.value })}
                                            placeholder="Trọng số" className="w-16 px-1.5 py-1 rounded border border-input text-xs" />
                                          <button onClick={() => handleAddCriterion(round.id)}
                                            className="text-xs text-primary font-medium">Thêm</button>
                                          <button onClick={() => setAddingCriterionTo(null)}
                                            className="text-xs text-muted-foreground">Hủy</button>
                                        </div>
                                      ) : (
                                        <button onClick={() => openAddCriterion(round.id)}
                                          className="flex items-center gap-1 text-xs text-primary font-medium hover:underline py-1">
                                          <Plus className="w-3 h-3" /> Thêm tiêu chí
                                        </button>
                                      )
                                    )}

                                    {roundCriteria.length === 0 && !addingCriterionTo && (
                                      <p className="text-xs text-muted-foreground text-center py-2">Chưa có tiêu chí nào</p>
                                    )}
                                  </>
                                )}
                              </div>
                            )}
                          </div>
                        )
                      })
                    )}

                    {/* Add round button */}
                    {isAdmin && !showRoundForm[phase.id] && (
                      <button onClick={() => openCreateRound(phase.id)}
                        className="flex items-center gap-1.5 text-sm text-primary font-medium hover:underline py-1">
                        <Plus className="w-4 h-4" /> Thêm đợt đánh giá
                      </button>
                    )}
                  </div>
                )}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}

import { useState, useEffect, useCallback, useRef } from 'react'
import { jobSearchApi } from '@/services/api'
import { JobListSkeleton } from '@/components/Skeleton'
import { Search, MapPin, Briefcase, DollarSign, Calendar, ExternalLink } from 'lucide-react'
import toast from 'react-hot-toast'

const ERROR_TOAST_ID = 'job-search-error'

export default function JobSearchPage() {
  const [jobs, setJobs] = useState([])
  const [pagination, setPagination] = useState({ currentPage: 1, totalPages: 1 })
  const [loading, setLoading] = useState(true)
  const abortRef = useRef(null)

  // Search parameters — typing here does NOT trigger API calls
  const [keyword, setKeyword] = useState('')
  const [location, setLocation] = useState('')
  const [page, setPage] = useState(1)
  const [byMajor, setByMajor] = useState(true)

  // Increment this to trigger a fresh fetch (form submit / reset / page change)
  const [searchTrigger, setSearchTrigger] = useState(0)

  // Hold the *active* search params (the ones actually sent to the API)
  // in a ref so fetchJobs doesn't need them in its dependency array.
  const activeParamsRef = useRef({ keyword: '', location: '', byMajor: true })

  const fetchJobs = useCallback(async () => {
    setLoading(true)
    try {
      let res
      const { keyword: kw, location: loc, byMajor: major } = activeParamsRef.current
      const params = { page, pageSize: 10 }

      if (major) {
        res = await jobSearchApi.searchByMajor(params, { signal: abortRef.current?.signal })
      } else {
        if (kw) params.keyword = kw
        if (loc) params.location = loc
        res = await jobSearchApi.search(params, { signal: abortRef.current?.signal })
      }

      setJobs(res.data.data.jobs || [])
      setPagination({
        currentPage: res.data.data.currentPage || 1,
        totalPages: res.data.data.totalPages || 1
      })
    } catch (err) {
      if (err.name === 'AbortError' || err.code === 'ERR_CANCELED') return

      // Extract the actual backend error message if available
      const serverMsg = err.response?.data?.message
      const fallback = 'Không thể tìm kiếm công việc. Vui lòng thử lại sau.'
      const message = serverMsg || fallback

      // Deduplicate: only one error toast at a time
      toast.error(message, { id: ERROR_TOAST_ID })
    } finally {
      setLoading(false)
    }
  }, [page, searchTrigger])

  // Fetch on mount and when page or searchTrigger changes.
  // keyword/location changes do NOT cause a re-fetch (keystroke spam fix).
  useEffect(() => {
    abortRef.current = new AbortController()
    fetchJobs()
    return () => { abortRef.current?.abort() }
  }, [fetchJobs])

  const handleSearch = useCallback((e) => {
    e.preventDefault()
    activeParamsRef.current = { keyword, location, byMajor: false }
    setPage(1)
    setByMajor(false)
    setSearchTrigger(t => t + 1)
  }, [keyword, location])

  const handleResetByMajor = useCallback(() => {
    activeParamsRef.current = { keyword: '', location: '', byMajor: true }
    setKeyword('')
    setLocation('')
    setPage(1)
    setByMajor(true)
    setSearchTrigger(t => t + 1)
  }, [])

  // When page changes via pagination buttons, update activeParams to stay in sync
  // (keyword/location/mode already in ref, just re-fetch with new page)
  const handlePageChange = useCallback((newPage) => {
    setPage(newPage)
    setSearchTrigger(t => t + 1)
  }, [])

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      <div>
        <h1 className="text-2xl font-bold">Tìm kiếm việc làm thực tập</h1>
        <p className="text-muted-foreground text-sm">Tìm kiếm các cơ hội thực tập bên ngoài phù hợp với định hướng của bạn</p>
      </div>

      {/* Search Bar */}
      <form onSubmit={handleSearch} className="bg-white p-4 rounded-xl border border-border shadow-sm space-y-4 md:space-y-0 md:flex gap-3 items-end">
        <div className="flex-1 space-y-1">
          <label className="block text-xs font-semibold text-muted-foreground uppercase">Từ khóa</label>
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              placeholder="Vị trí, kỹ năng, công ty..."
              value={keyword}
              onChange={e => setKeyword(e.target.value)}
              className="w-full pl-10 pr-4 py-2 rounded-lg border border-input text-sm focus:outline-none"
            />
          </div>
        </div>

        <div className="w-full md:w-64 space-y-1">
          <label className="block text-xs font-semibold text-muted-foreground uppercase">Địa điểm</label>
          <div className="relative">
            <MapPin className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-muted-foreground" />
            <input
              type="text"
              placeholder="Thành phố, quốc gia..."
              value={location}
              onChange={e => setLocation(e.target.value)}
              className="w-full pl-10 pr-4 py-2 rounded-lg border border-input text-sm focus:outline-none"
            />
          </div>
        </div>

        <div className="flex gap-2">
          <button
            type="submit"
            className="px-5 py-2 rounded-lg bg-primary hover:bg-primary/90 text-white font-semibold text-sm transition-colors shadow-lg shadow-primary/20 shrink-0"
          >
            Tìm kiếm
          </button>
          <button
            type="button"
            onClick={handleResetByMajor}
            className={`px-4 py-2 rounded-lg text-sm font-semibold transition-colors shrink-0 ${
              byMajor
                ? 'bg-accent/15 text-accent border border-accent/20'
                : 'bg-secondary text-muted-foreground hover:bg-secondary/80'
            }`}
          >
            Theo Ngành Học
          </button>
        </div>
      </form>

      {/* Jobs List */}
      {loading ? <JobListSkeleton /> : (
        <div className="space-y-4">
          {jobs.length === 0 ? (
            <div className="bg-white rounded-xl border border-border p-12 text-center text-muted-foreground">
              <Briefcase className="w-12 h-12 mx-auto text-muted-foreground/30 mb-3" />
              <p className="text-sm">Không tìm thấy cơ hội thực tập nào.</p>
              <p className="text-xs text-muted-foreground/70 mt-1">Thử thay đổi từ khóa hoặc tìm kiếm theo ngành học.</p>
            </div>
          ) : (
            <>
              <div className="grid grid-cols-1 gap-4">
                {jobs.map((job, idx) => (
                  <div key={job.externalId || `job-${idx}`} className="bg-white rounded-xl border border-border p-5 hover:shadow-md transition-all flex flex-col md:flex-row justify-between gap-4">
                    <div className="space-y-2.5">
                      <div>
                        <span className="px-2 py-0.5 rounded bg-secondary text-muted-foreground text-[10px] font-bold uppercase tracking-wider">
                          {job.category || 'Internship'}
                        </span>
                        <h3 className="text-base font-bold text-foreground mt-1">{job.title}</h3>
                        <p className="text-sm font-medium text-primary">{job.company}</p>
                      </div>

                      <div className="flex flex-wrap gap-x-4 gap-y-1.5 text-xs text-muted-foreground">
                        <span className="flex items-center gap-1">
                          <MapPin className="w-3.5 h-3.5" />
                          {job.location || 'Remote'}
                        </span>
                        {(job.salaryMin != null || job.salaryMax != null) && (
                          <span className="flex items-center gap-0.5">
                            <DollarSign className="w-3.5 h-3.5" />
                            {job.salaryMin != null ? `$${job.salaryMin.toLocaleString()}` : '0'} - {job.salaryMax != null ? `$${job.salaryMax.toLocaleString()}` : 'Negotiable'}
                          </span>
                        )}
                        {job.postedAt && (
                          <span className="flex items-center gap-1">
                            <Calendar className="w-3.5 h-3.5" />
                            Đăng ngày: {new Date(job.postedAt).toLocaleDateString('vi-VN')}
                          </span>
                        )}
                      </div>

                      {job.description && (
                        <p className="text-xs text-muted-foreground line-clamp-2 leading-relaxed">
                          {job.description}
                        </p>
                      )}
                    </div>

                    <div className="flex items-end justify-start md:justify-end shrink-0">
                      {job.applyUrl && (job.applyUrl.startsWith('http://') || job.applyUrl.startsWith('https://')) ? (
                        <a
                          href={job.applyUrl}
                          target="_blank"
                          rel="noopener noreferrer"
                          className="flex items-center gap-1.5 px-4 py-2 rounded-lg bg-secondary text-foreground hover:bg-primary hover:text-white text-xs font-bold transition-all"
                        >
                          Ứng tuyển ngay
                          <ExternalLink className="w-3.5 h-3.5" />
                        </a>
                      ) : (
                        <span className="text-xs text-muted-foreground font-semibold">Liên hệ nhà tuyển dụng</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>

              {/* Pagination */}
              {pagination.totalPages > 1 && (
                <div className="flex justify-center gap-2 pt-4">
                  <button
                    onClick={() => handlePageChange(Math.max(page - 1, 1))}
                    disabled={page === 1}
                    className="px-3 py-1.5 rounded-lg border border-border bg-white text-sm disabled:opacity-40"
                  >
                    Trước
                  </button>
                  <span className="px-3 py-1.5 text-sm font-semibold">
                    Trang {page} / {pagination.totalPages}
                  </span>
                  <button
                    onClick={() => handlePageChange(Math.min(page + 1, pagination.totalPages))}
                    disabled={page >= pagination.totalPages}
                    className="px-3 py-1.5 rounded-lg border border-border bg-white text-sm disabled:opacity-40"
                  >
                    Sau
                  </button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  )
}

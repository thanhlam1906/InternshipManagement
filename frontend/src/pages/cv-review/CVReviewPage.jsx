import { useState, useEffect } from 'react'
import { cvReviewApi } from '@/services/api'
import { UploadCloud, FileText, CheckCircle2, AlertCircle, Cpu, Loader2 } from 'lucide-react'
import toast from 'react-hot-toast'

export default function CVReviewPage() {
  const [file, setFile] = useState(null)
  const [apiKey, setApiKey] = useState('')
  const [provider, setProvider] = useState('GEMINI') // GEMINI or OPENAI
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)

  // Cleanup on unmount
  useEffect(() => {
    return () => {
      setLoading(false)
      setResult(null)
    }
  }, [])

  const handleFileChange = (e) => {
    const selected = e.target.files[0]
    if (!selected) return
    const isValidType = selected.type === 'application/pdf'
    const isValidExt = selected.name && selected.name.toLowerCase().endsWith('.pdf')
    const isValidSize = selected.size <= 10 * 1024 * 1024 // 10MB
    if (!isValidType && !isValidExt) {
      toast.error('Vui lòng chọn một file PDF hợp lệ')
      return
    }
    if (!isValidSize) {
      toast.error('File không được vượt quá 10MB')
      return
    }
    setFile(selected)
  }

  const handleReview = async (e) => {
    e.preventDefault()
    if (!file) {
      toast.error('Vui lòng tải lên file CV PDF')
      return
    }
    if (!apiKey || apiKey.trim().length < 10) {
      toast.error('Vui lòng nhập API Key cá nhân hợp lệ (tối thiểu 10 ký tự)')
      return
    }

    setLoading(true)
    setResult(null)
    try {
      let res
      if (provider === 'GEMINI') {
        res = await cvReviewApi.reviewWithGemini(file, apiKey)
      } else {
        res = await cvReviewApi.reviewWithOpenAi(file, apiKey)
      }
      setResult(res.data.data)
      toast.success('Phân tích CV hoàn tất!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Lỗi khi gọi API phân tích CV')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Review CV bằng AI</h1>
        <p className="text-muted-foreground text-sm">Tải lên CV (PDF) và sử dụng API cá nhân của bạn để nhận đánh giá tức thì</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Settings & Upload */}
        <div className="md:col-span-1 bg-white p-5 rounded-xl border border-border space-y-4 shadow-sm h-fit">
          <h2 className="text-sm font-bold uppercase tracking-wider text-muted-foreground">Cấu hình AI</h2>
          
          <form onSubmit={handleReview} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold mb-1">AI Provider</label>
              <select
                value={provider}
                onChange={e => setProvider(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-input text-sm bg-white"
              >
                <option value="GEMINI">Google Gemini</option>
                <option value="OPENAI">OpenAI Compatible (Groq / OpenAI)</option>
              </select>
            </div>

            <div>
              <label className="block text-xs font-semibold mb-1">
                API Key cá nhân
              </label>
              <input
                type="password"
                placeholder={provider === 'GEMINI' ? 'Nhập Gemini API Key...' : 'Nhập Groq/OpenAI API Key...'}
                value={apiKey}
                onChange={e => setApiKey(e.target.value)}
                className="w-full px-3 py-2 rounded-lg border border-input text-sm focus:outline-none"
                required
              />
              <p className="text-[10px] text-muted-foreground mt-1 leading-relaxed">
                {provider === 'GEMINI' 
                  ? 'Lấy khóa miễn phí từ Google AI Studio.' 
                  : 'Lấy khóa miễn phí từ console.groq.com hoặc OpenAI Platform.'}
              </p>
            </div>

            {/* Upload Area */}
            <div>
              <label className="block text-xs font-semibold mb-1">File CV (PDF)</label>
              <div className="border-2 border-dashed border-border rounded-xl p-4 text-center hover:bg-secondary/30 transition-colors relative cursor-pointer">
                <input
                  type="file"
                  accept="application/pdf"
                  onChange={handleFileChange}
                  className="absolute inset-0 opacity-0 cursor-pointer"
                />
                <UploadCloud className="w-8 h-8 text-muted-foreground mx-auto mb-2" />
                {file ? (
                  <div className="flex items-center justify-center gap-1.5 text-xs text-primary font-medium">
                    <FileText className="w-4 h-4 shrink-0" />
                    <span className="truncate max-w-[120px]">{file.name}</span>
                  </div>
                ) : (
                  <p className="text-xs text-muted-foreground">Kéo thả hoặc click để chọn file PDF</p>
                )}
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2 rounded-lg bg-primary text-white text-sm font-semibold hover:bg-primary/90
                transition-all disabled:opacity-50 flex items-center justify-center gap-2 shadow-lg shadow-primary/20"
            >
              {loading ? (
                <>
                  <Loader2 className="w-4 h-4 animate-spin" />
                  Đang phân tích...
                </>
              ) : (
                <>
                  <Cpu className="w-4 h-4" />
                  Bắt đầu Review
                </>
              )}
            </button>
          </form>
        </div>

        {/* Results */}
        <div className="md:col-span-2 space-y-4">
          {result ? (
            <div className="bg-white rounded-xl border border-border shadow-sm p-6 space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-300">
              <div className="flex items-center justify-between pb-4 border-b border-border">
                <div>
                  <h2 className="text-lg font-bold">Kết quả Đánh giá CV</h2>
                  <p className="text-xs text-muted-foreground">Thời gian: {new Date(result.reviewedAt).toLocaleString('vi-VN')}</p>
                </div>
                {/* Overall Score circle */}
                <div className="flex items-center gap-3">
                  <div className="text-right">
                    <p className="text-xs text-muted-foreground">Điểm số tổng quan</p>
                    <p className="text-sm font-bold text-muted-foreground">Thang điểm 10</p>
                  </div>
                  <div className="w-14 h-14 rounded-full border-4 border-primary flex items-center justify-center font-bold text-lg text-primary bg-primary/5">
                    {result.overallScore}
                  </div>
                </div>
              </div>

              {/* Sections */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="bg-secondary/40 p-4 rounded-xl space-y-2">
                  <h3 className="text-xs font-bold uppercase tracking-wider text-primary">Tóm tắt CV</h3>
                  <p className="text-sm text-foreground/90 leading-relaxed">{result.summary}</p>
                </div>
              </div>

              <div className="space-y-4">
                <div className="flex items-start gap-2.5">
                  <CheckCircle2 className="w-5 h-5 text-success shrink-0 mt-0.5" />
                  <div>
                    <h4 className="text-sm font-bold">Điểm mạnh</h4>
                    <p className="text-sm text-muted-foreground mt-1 leading-relaxed">{result.strengthPoints}</p>
                  </div>
                </div>

                <div className="flex items-start gap-2.5">
                  <AlertCircle className="w-5 h-5 text-warning shrink-0 mt-0.5" />
                  <div>
                    <h4 className="text-sm font-bold">Điểm cần cải thiện</h4>
                    <p className="text-sm text-muted-foreground mt-1 leading-relaxed">{result.improvementSuggestions}</p>
                  </div>
                </div>

                <div className="border-t border-border pt-4 text-xs">
                  <div>
                    <span className="font-bold block text-muted-foreground mb-1 uppercase tracking-wider">Nội dung & Kỹ năng</span>
                    <p className="text-foreground/80 leading-relaxed">{result.contentFeedback}</p>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="bg-white rounded-xl border border-border border-dashed p-12 text-center text-muted-foreground flex flex-col items-center justify-center min-h-[300px]">
              <FileText className="w-12 h-12 text-muted-foreground/40 mb-3" />
              <p className="text-sm">Kết quả đánh giá CV bằng AI sẽ hiển thị tại đây.</p>
              <p className="text-xs text-muted-foreground/70 mt-1">Cung cấp API Key và tải file PDF để bắt đầu.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

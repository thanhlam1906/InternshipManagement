import { cn } from '@/lib/utils'

type SkeletonVariant = 'text' | 'title' | 'circle' | 'rect' | 'card'

interface SkeletonProps {
  className?: string
  variant?: SkeletonVariant
}

const SKELETON_ARR_3 = [1, 2, 3]
const SKELETON_ARR_4 = [1, 2, 3, 4]

const base = 'animate-pulse rounded bg-muted/60'

const variants: Record<SkeletonVariant, string> = {
  text: 'h-4 w-full',
  title: 'h-6 w-1/2',
  circle: 'h-10 w-10 rounded-full',
  rect: 'h-24 w-full',
  card: 'h-40 w-full rounded-xl',
}

export default function Skeleton({ className, variant = 'text' }: SkeletonProps) {
  return (
    <div
      className={cn(base, variants[variant] || variants.text, className)}
      aria-hidden="true"
    />
  )
}

/** Profile page skeleton — mirrors the profile card layout. */
export function ProfileSkeleton() {
  return (
    <div className="space-y-6" aria-hidden="true">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="space-y-2">
          <Skeleton variant="title" className="w-48" />
          <Skeleton variant="text" className="w-72" />
        </div>
        <Skeleton className="h-9 w-28 rounded-lg" />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Left Card */}
        <div className="lg:col-span-3 bg-white rounded-xl border border-border overflow-hidden">
          <div className="p-6 flex items-center gap-4">
            <Skeleton variant="circle" className="h-16 w-16" />
            <div className="space-y-2">
              <Skeleton variant="title" className="w-36" />
              <Skeleton variant="text" className="w-24" />
              <Skeleton className="h-4 w-20 rounded-full" />
            </div>
          </div>
          <div className="p-6 grid grid-cols-1 sm:grid-cols-2 gap-4">
            {SKELETON_ARR_4.map(i => (
              <div key={i} className="p-3 rounded-xl bg-secondary/30 space-y-1.5">
                <Skeleton className="h-3 w-20" />
                <Skeleton variant="text" className="w-32" />
              </div>
            ))}
          </div>
        </div>

        {/* Right Cards */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-xl border border-border p-6 space-y-4">
            <Skeleton variant="title" className="w-40" />
            {SKELETON_ARR_3.map(i => (
              <div key={i} className="flex items-start gap-3">
                <Skeleton variant="circle" className="h-5 w-5 shrink-0" />
                <div className="space-y-1 flex-1">
                  <Skeleton className="h-3 w-20" />
                  <Skeleton variant="text" className="w-28" />
                </div>
              </div>
            ))}
          </div>
          <div className="bg-white rounded-xl border border-border p-6 space-y-3">
            <Skeleton variant="title" className="w-36" />
            <div className="flex items-center gap-3">
              <Skeleton variant="circle" className="h-3 w-3" />
              <Skeleton variant="text" className="w-32" />
            </div>
            <Skeleton variant="text" className="w-48" />
          </div>
        </div>
      </div>
    </div>
  )
}

/** Job list skeleton — mirrors the job card layout. */
export function JobListSkeleton() {
  return (
    <div className="space-y-4" aria-hidden="true">
      {SKELETON_ARR_3.map(i => (
        <div key={i} className="bg-white rounded-xl border border-border p-5 flex flex-col md:flex-row justify-between gap-4">
          <div className="space-y-3 flex-1">
            <Skeleton className="h-5 w-24 rounded" />
            <Skeleton variant="title" className="w-64" />
            <Skeleton variant="text" className="w-40" />
            <div className="flex gap-4">
              <Skeleton variant="text" className="w-24" />
              <Skeleton variant="text" className="w-20" />
              <Skeleton variant="text" className="w-28" />
            </div>
            <Skeleton variant="text" className="w-full" />
          </div>
          <div className="flex items-end">
            <Skeleton className="h-9 w-32 rounded-lg" />
          </div>
        </div>
      ))}
    </div>
  )
}

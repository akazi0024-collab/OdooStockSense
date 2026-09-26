import type { ButtonHTMLAttributes, InputHTMLAttributes, ReactNode, SelectHTMLAttributes } from 'react'
import { AlertCircle, LoaderCircle, Search } from 'lucide-react'

export function Button({ className = '', variant = 'primary', loading, children, ...props }: ButtonHTMLAttributes<HTMLButtonElement> & { variant?: 'primary' | 'secondary' | 'ghost' | 'danger'; loading?: boolean }) {
  const variants = { primary: 'bg-brand-600 text-white hover:bg-brand-700 shadow-sm shadow-brand-600/15', secondary: 'border border-slate-200 bg-white text-ink hover:bg-slate-50', ghost: 'text-muted hover:bg-slate-100 hover:text-ink', danger: 'bg-rose-600 text-white hover:bg-rose-700' }
  return <button className={`inline-flex min-h-10 items-center justify-center gap-2 rounded-lg px-4 text-sm font-semibold transition disabled:cursor-not-allowed disabled:opacity-55 ${variants[variant]} ${className}`} disabled={loading || props.disabled} {...props}>{loading && <LoaderCircle size={16} className="animate-spin" />}{children}</button>
}

export function Input({ className = '', ...props }: InputHTMLAttributes<HTMLInputElement>) {
  return <input className={`h-11 w-full rounded-lg border border-slate-200 bg-white px-3.5 text-sm text-ink outline-none transition placeholder:text-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 disabled:bg-slate-50 ${className}`} {...props} />
}

export function Select({ className = '', children, ...props }: SelectHTMLAttributes<HTMLSelectElement>) {
  return <select className={`h-11 w-full rounded-lg border border-slate-200 bg-white px-3.5 text-sm text-ink outline-none transition focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10 ${className}`} {...props}>{children}</select>
}

export function Field({ label, children, hint }: { label: string; children: ReactNode; hint?: string }) {
  return <label className="block space-y-1.5"><span className="text-sm font-medium text-slate-700">{label}</span>{children}{hint && <span className="block text-xs text-muted">{hint}</span>}</label>
}

export function PageHeader({ eyebrow, title, description, action }: { eyebrow?: string; title: string; description?: string; action?: ReactNode }) {
  return <div className="mb-7 flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between"><div>{eyebrow && <p className="mb-1 text-xs font-bold uppercase tracking-[.16em] text-brand-600">{eyebrow}</p>}<h1 className="text-2xl font-bold tracking-tight text-ink sm:text-[28px]">{title}</h1>{description && <p className="mt-1.5 max-w-2xl text-sm text-muted">{description}</p>}</div>{action && <div className="shrink-0">{action}</div>}</div>
}

export function Card({ children, className = '' }: { children: ReactNode; className?: string }) {
  return <section className={`rounded-2xl border border-slate-100 bg-white shadow-card ${className}`}>{children}</section>
}

export function Spinner({ label = 'Loading...' }: { label?: string }) {
  return <div className="flex min-h-40 flex-col items-center justify-center gap-3 text-sm text-muted"><LoaderCircle className="animate-spin text-brand-600" size={22} /><span>{label}</span></div>
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return <div className="flex min-h-40 flex-col items-center justify-center gap-3 px-5 text-center"><AlertCircle className="text-rose-500" size={26} /><p className="max-w-lg text-sm text-slate-600">{message}</p>{onRetry && <Button variant="secondary" onClick={onRetry}>Try again</Button>}</div>
}

export function EmptyState({ title, description }: { title: string; description: string }) {
  return <div className="flex min-h-40 flex-col items-center justify-center px-5 text-center"><div className="mb-3 rounded-full bg-slate-100 p-3 text-slate-400"><Search size={20} /></div><p className="font-semibold text-ink">{title}</p><p className="mt-1 max-w-sm text-sm text-muted">{description}</p></div>
}

export function Modal({ title, children, onClose }: { title: string; children: ReactNode; onClose: () => void }) {
  return <div className="fixed inset-0 z-50 flex items-end justify-center bg-ink/45 p-0 backdrop-blur-[2px] sm:items-center sm:p-5" onMouseDown={(event) => { if (event.target === event.currentTarget) onClose() }}><div role="dialog" aria-modal="true" aria-label={title} className="max-h-[92vh] w-full overflow-y-auto rounded-t-2xl bg-white p-5 shadow-2xl sm:max-w-xl sm:rounded-2xl sm:p-6"><div className="mb-5 flex items-center justify-between"><h2 className="text-lg font-bold text-ink">{title}</h2><button className="rounded-lg p-2 text-muted hover:bg-slate-100" onClick={onClose} aria-label="Close dialog">×</button></div>{children}</div></div>
}

export function formatDate(value?: string) {
  if (!value) return '—'
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : new Intl.DateTimeFormat(undefined, { dateStyle: 'medium' }).format(date)
}

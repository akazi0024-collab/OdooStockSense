import { createContext, useCallback, useContext, useState, type ReactNode } from 'react'
import { CheckCircle2, CircleAlert, Info, X } from 'lucide-react'

type Toast = { id: number; message: string; kind: 'success' | 'error' | 'info' }
type ToastContextValue = { notify: (message: string, kind?: Toast['kind']) => void }
const ToastContext = createContext<ToastContextValue | null>(null)

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([])
  const notify = useCallback((message: string, kind: Toast['kind'] = 'success') => {
    const id = Date.now() + Math.random()
    setToasts((current) => [...current, { id, message, kind }])
    window.setTimeout(() => setToasts((current) => current.filter((toast) => toast.id !== id)), 5000)
  }, [])
  return <ToastContext.Provider value={{ notify }}>
    {children}
    <div aria-live="polite" className="fixed right-4 top-4 z-[100] flex w-[min(92vw,380px)] flex-col gap-2">
      {toasts.map((toast) => <div key={toast.id} role="status" className={`flex items-start gap-3 rounded-xl border bg-white p-4 shadow-xl ${toast.kind === 'error' ? 'border-rose-200' : toast.kind === 'info' ? 'border-blue-200' : 'border-emerald-200'}`}>
        {toast.kind === 'error' ? <CircleAlert className="mt-0.5 shrink-0 text-rose-500" size={18} /> : toast.kind === 'info' ? <Info className="mt-0.5 shrink-0 text-blue-500" size={18} /> : <CheckCircle2 className="mt-0.5 shrink-0 text-emerald-500" size={18} />}
        <p className="flex-1 text-sm text-ink">{toast.message}</p>
        <button aria-label="Dismiss notification" onClick={() => setToasts((current) => current.filter((item) => item.id !== toast.id))} className="text-muted hover:text-ink"><X size={16} /></button>
      </div>)}
    </div>
  </ToastContext.Provider>
}

export function useToast() {
  const context = useContext(ToastContext)
  if (!context) throw new Error('useToast must be used within ToastProvider')
  return context
}

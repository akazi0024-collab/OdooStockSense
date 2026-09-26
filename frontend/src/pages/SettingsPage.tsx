import { UserRound } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { Button, Card, PageHeader } from '../components/ui'
import { useNavigate } from 'react-router-dom'
import { ArrowRight, KeyRound, ShieldCheck } from 'lucide-react'

export function SettingsPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  return <div><PageHeader eyebrow="Your account" title="Profile & settings" description="Review the profile returned by your authenticated StockSense session and manage account security." />
    <div className="grid items-start gap-5 xl:grid-cols-2">
      <Card><div className="flex items-center gap-3 border-b border-slate-100 px-5 py-4"><span className="grid h-9 w-9 place-items-center rounded-lg bg-indigo-50 text-brand-600"><UserRound size={18} /></span><div><h2 className="text-sm font-bold text-ink">Profile</h2><p className="text-xs text-muted">Account identity from the authentication API.</p></div></div><dl className="space-y-5 p-5"><div><dt className="text-xs font-medium text-muted">Name</dt><dd className="mt-1 text-sm font-semibold text-ink">{user?.name ?? '—'}</dd></div><div><dt className="text-xs font-medium text-muted">Email</dt><dd className="mt-1 text-sm font-semibold text-ink">{user?.email ?? '—'}</dd></div><div><dt className="text-xs font-medium text-muted">Role</dt><dd className="mt-1 text-sm font-semibold capitalize text-ink">{user?.role?.toLowerCase() ?? '—'}</dd></div><p className="rounded-lg bg-slate-50 p-3 text-xs leading-5 text-muted">Profile editing is not exposed by the configured API. These details are read directly from the login/register response.</p></dl></Card>
      <Card><div className="flex items-center gap-3 border-b border-slate-100 px-5 py-4"><span className="grid h-9 w-9 place-items-center rounded-lg bg-emerald-50 text-emerald-600"><ShieldCheck size={18} /></span><div><h2 className="text-sm font-bold text-ink">Account security</h2><p className="text-xs text-muted">Password reset uses the API's one-time verification code.</p></div></div><div className="p-5"><div className="mb-5 rounded-xl border border-slate-100 p-4"><div className="flex items-center gap-3"><KeyRound className="text-brand-600" size={19} /><div><p className="text-sm font-semibold text-ink">Password</p><p className="mt-1 text-xs text-muted">Request a time-limited OTP and choose a new password.</p></div></div></div><Button onClick={() => navigate('/forgot-password')}>Reset password <ArrowRight size={15} /></Button></div></Card>
    </div>
  </div>
}

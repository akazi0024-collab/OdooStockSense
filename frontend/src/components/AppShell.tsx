import { useState } from 'react'
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { Activity, Boxes, ChevronDown, CircleUserRound, ClipboardList, LayoutDashboard, LogOut, MapPin, Menu, Package, Settings2, Truck, Warehouse, X } from 'lucide-react'
import { useAuth } from '../context/AuthContext'
import { useToast } from '../context/ToastContext'
import { apiMessage } from '../services/api'

const links = [
  { label: 'Dashboard', to: '/dashboard', icon: LayoutDashboard, section: 'Workspace' },
  { label: 'Products', to: '/products', icon: Package, section: 'Workspace' },
  { label: 'Categories', to: '/categories', icon: Boxes, section: 'Workspace' },
  { label: 'Warehouses', to: '/warehouses', icon: Warehouse, section: 'Workspace' },
  { label: 'Stock locations', to: '/locations', icon: MapPin, section: 'Workspace' },
  { label: 'Suppliers', to: '/suppliers', icon: Truck, section: 'Workspace' },
  { label: 'Customers', to: '/customers', icon: CircleUserRound, section: 'Workspace' },
  { label: 'Operations', to: '/operations/receipts', icon: ClipboardList, section: 'Inventory' },
  { label: 'Stock ledger', to: '/ledger', icon: Activity, section: 'Inventory' },
  { label: 'Settings', to: '/settings', icon: Settings2, section: 'Account' },
]

export function AppShell() {
  const [mobileOpen, setMobileOpen] = useState(false)
  const [userMenu, setUserMenu] = useState(false)
  const { user, logout } = useAuth()
  const { notify } = useToast()
  const navigate = useNavigate()
  const location = useLocation()
  const title = links.find((link) => link.to === location.pathname || (link.to.startsWith('/operations') && location.pathname.startsWith('/operations')))?.label ?? 'StockSense'
  const initials = (user?.name ?? user?.fullName ?? user?.email ?? 'U').slice(0, 1).toUpperCase()

  async function handleLogout() {
    try { await logout(); navigate('/login') } catch (error) { notify(apiMessage(error), 'error') }
  }

  return <div className="min-h-screen bg-canvas">
    {mobileOpen && <button className="fixed inset-0 z-30 bg-ink/40 lg:hidden" aria-label="Close menu" onClick={() => setMobileOpen(false)} />}
    <aside className={`fixed inset-y-0 left-0 z-40 flex w-[258px] flex-col bg-[#111a34] px-4 pb-4 pt-6 text-white transition-transform lg:translate-x-0 ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`}>
      <div className="mb-9 flex items-center justify-between px-2"><NavLink to="/dashboard" onClick={() => setMobileOpen(false)} className="flex items-center gap-3"><span className="grid h-10 w-10 place-items-center rounded-xl bg-brand-500"><Package size={21} /></span><span><b className="font-[Manrope] text-lg tracking-tight">stocksense</b><small className="block text-[10px] tracking-[.15em] text-slate-400">INVENTORY, IN FOCUS</small></span></NavLink><button className="lg:hidden" onClick={() => setMobileOpen(false)} aria-label="Close navigation"><X size={19} /></button></div>
      <div className="mb-3 rounded-xl border border-white/10 bg-white/[.06] p-3"><div className="flex items-center gap-3"><div className="grid h-9 w-9 place-items-center rounded-lg bg-[#2b385d] text-sm font-bold">{initials}</div><div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold">{user?.name ?? user?.fullName ?? 'Your workspace'}</p><p className="truncate text-xs text-slate-400">{user?.email ?? 'Inventory team'}</p></div><ChevronDown size={15} className="text-slate-400" /></div></div>
      <nav className="flex-1 space-y-6 overflow-y-auto pt-2">{['Workspace', 'Inventory', 'Account'].map((section) => <div key={section}><p className="mb-2 px-3 text-[10px] font-bold uppercase tracking-[.17em] text-slate-500">{section}</p><div className="space-y-1">{links.filter((link) => link.section === section).map(({ label, to, icon: Icon }) => <NavLink key={to} to={to} end={to === '/dashboard'} onClick={() => setMobileOpen(false)} className={({ isActive }) => `flex items-center gap-3 rounded-lg px-3 py-2.5 text-[13px] font-medium transition ${isActive ? 'bg-brand-500 text-white shadow-lg shadow-brand-500/20' : 'text-slate-300 hover:bg-white/[.07] hover:text-white'}`}><Icon size={17} strokeWidth={1.8} /><span>{label}</span></NavLink>)}</div></div>)}</nav>
      <div className="rounded-xl border border-white/10 bg-gradient-to-br from-white/[.08] to-transparent p-3.5"><p className="text-xs font-semibold">Inventory insights</p><p className="mt-1 text-[11px] leading-relaxed text-slate-400">Keep every item moving in the right direction.</p><button onClick={() => navigate('/ledger')} className="mt-3 text-xs font-semibold text-indigo-300 hover:text-white">View stock activity →</button></div>
      <div className="mt-3 border-t border-white/10 pt-3"><button onClick={() => void handleLogout()} className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm text-slate-300 hover:bg-white/[.07] hover:text-white"><LogOut size={17} />Sign out</button></div>
    </aside>
    <div className="min-h-screen lg:pl-[258px]">
      <header className="sticky top-0 z-20 flex h-[70px] items-center justify-between border-b border-slate-200/75 bg-white/90 px-4 backdrop-blur-md sm:px-7 lg:px-9"><div className="flex items-center gap-3"><button className="rounded-lg p-2 text-ink hover:bg-slate-100 lg:hidden" onClick={() => setMobileOpen(true)} aria-label="Open navigation"><Menu size={20} /></button><div><p className="text-sm font-semibold text-ink">{title}</p><p className="hidden text-xs text-muted sm:block">StockSense / {title}</p></div></div><div className="relative flex items-center gap-3"><span className="hidden text-right sm:block"><span className="block text-xs font-semibold text-ink">{user?.name ?? user?.fullName ?? 'Account'}</span><span className="text-[11px] capitalize text-muted">{user?.role ?? 'Inventory manager'}</span></span><button onClick={() => setUserMenu((open) => !open)} className="grid h-9 w-9 place-items-center rounded-full bg-indigo-50 text-sm font-bold text-brand-700" aria-label="Account menu">{initials}</button>{userMenu && <div className="absolute right-0 top-12 z-30 w-44 rounded-xl border border-slate-100 bg-white p-1.5 shadow-card"><button onClick={() => { setUserMenu(false); navigate('/settings') }} className="w-full rounded-lg px-3 py-2 text-left text-sm hover:bg-slate-50">Profile & settings</button><button onClick={() => void handleLogout()} className="w-full rounded-lg px-3 py-2 text-left text-sm text-rose-600 hover:bg-rose-50">Sign out</button></div>}</div></header>
      <main className="mx-auto max-w-[1500px] p-4 sm:p-6 lg:p-9"><Outlet /></main>
    </div>
  </div>
}

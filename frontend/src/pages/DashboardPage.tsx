import { Link } from 'react-router-dom'
import { Activity, ArrowDownRight, ArrowRight, ArrowUpRight, Boxes, Package, RefreshCw, Warehouse } from 'lucide-react'
import { Area, AreaChart, CartesianGrid, Cell, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { dashboardService } from '../services/dashboard'
import type { DashboardStats, Entity, Movement } from '../services/types'
import { Card, EmptyState, ErrorState, PageHeader, Spinner, formatDate } from '../components/ui'
import { useRemote } from '../hooks/useRemote'

const palette = ['#596df5', '#27bda7', '#ffad5c', '#9a78eb', '#5a9ce8']
const movementLabel = (item: Movement) => item.productName ?? item.product?.name ?? 'Stock movement'

function Section({ title, action, children, className = '' }: { title: string; action?: React.ReactNode; children: React.ReactNode; className?: string }) {
  return <Card className={className}><div className="flex items-center justify-between border-b border-slate-100 px-5 py-4"><h2 className="text-sm font-bold text-ink">{title}</h2>{action}</div>{children}</Card>
}

export function DashboardPage() {
  const stats = useRemote<DashboardStats>(dashboardService.stats)
  const movements = useRemote<Movement[]>(dashboardService.movements)
  const categories = useRemote<Awaited<ReturnType<typeof dashboardService.categories>>>(dashboardService.categories)
  const lowStock = useRemote<Entity[]>(dashboardService.lowStock)
  const recent = useRemote<Movement[]>(dashboardService.recentOperations)
  const pending = useRemote<Movement[]>(dashboardService.pendingOperations)
  const data = stats.data ?? {}
  const cards = [
    { label: 'Products in catalog', value: data.products, icon: Package, shade: 'bg-indigo-50 text-indigo-600', path: '/products' },
    { label: 'Inventory units', value: data.inventoryUnits, icon: Boxes, shade: 'bg-teal-50 text-teal-600', path: '/ledger' },
    { label: 'Low-stock lines', value: data.lowStockLines, icon: Activity, shade: 'bg-amber-50 text-amber-600', path: '/products?status=LOW_STOCK' },
    { label: 'Stock locations', value: data.locations, icon: Warehouse, shade: 'bg-violet-50 text-violet-600', path: '/locations' },
  ]
  const trend = (movements.data ?? []).map((row) => ({
    date: formatDate(row.occurredAt ?? row.date ?? row.createdAt),
    quantity: row.quantityDelta ?? row.quantity ?? 0,
    label: movementLabel(row),
  })).slice(-10)
  const categoryData = (categories.data ?? []).map((row) => ({ name: row.name ?? row.categoryName ?? 'Uncategorized', value: row.quantity ?? 0 })).filter((row) => row.value > 0)

  return <div>
    <PageHeader eyebrow="Your inventory at a glance" title="Good day, here's your overview" description="A live snapshot of stock, movement, and what needs your attention." action={<button onClick={() => { stats.retry(); movements.retry(); categories.retry(); lowStock.retry(); recent.retry(); pending.retry() }} className="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3.5 py-2.5 text-sm font-semibold text-slate-600 hover:bg-slate-50"><RefreshCw size={15} /> Refresh</button>} />
    {stats.error && <div className="mb-5 rounded-xl border border-rose-200 bg-rose-50 p-1"><ErrorState message={stats.error} onRetry={stats.retry} /></div>}
    <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-4">{cards.map(({ label, value, icon: Icon, shade, path }) => <Link to={path} key={label} className="group rounded-2xl border border-slate-100 bg-white p-5 shadow-card transition hover:-translate-y-0.5 hover:shadow-lg"><div className="flex items-start justify-between"><div className={`grid h-10 w-10 place-items-center rounded-xl ${shade}`}><Icon size={19} /></div><ArrowUpRight size={16} className="text-slate-300 transition group-hover:text-brand-600" /></div><p className="mt-5 text-[13px] font-medium text-muted">{label}</p>{stats.loading ? <div className="mt-1 h-8 w-24 animate-pulse rounded bg-slate-100" /> : <p className="mt-1 text-[26px] font-bold tracking-tight text-ink">{value === undefined || value === null ? '—' : value}</p>}</Link>)}</div>
    <div className="mb-6 grid gap-5 xl:grid-cols-[1.65fr_1fr]">
      <Section title="Stock movement" action={<span className="inline-flex items-center gap-1.5 text-xs text-muted"><span className="h-2 w-2 rounded-full bg-brand-500" />Recent activity</span>}>
        {movements.loading ? <Spinner /> : movements.error ? <ErrorState message={movements.error} onRetry={movements.retry} /> : trend.length === 0 ? <EmptyState title="No movement yet" description="Inventory movement will appear here when stock is received, delivered, or adjusted." /> : <div className="h-[255px] p-4 pr-6"><ResponsiveContainer width="100%" height="100%"><AreaChart data={trend} margin={{ left: 0, right: 8, top: 12, bottom: 0 }}><defs><linearGradient id="movementFill" x1="0" y1="0" x2="0" y2="1"><stop offset="0%" stopColor="#596df5" stopOpacity={0.23} /><stop offset="95%" stopColor="#596df5" stopOpacity={0.01} /></linearGradient></defs><CartesianGrid stroke="#edf0f6" vertical={false} /><XAxis dataKey="date" axisLine={false} tickLine={false} interval="preserveStartEnd" /><YAxis axisLine={false} tickLine={false} width={38} /><Tooltip contentStyle={{ border: '1px solid #edf0f6', borderRadius: 12, boxShadow: '0 10px 30px #18234a12' }} /><Area type="monotone" dataKey="quantity" stroke="#596df5" strokeWidth={2.5} fill="url(#movementFill)" /></AreaChart></ResponsiveContainer></div>}
      </Section>
      <Section title="Category mix" action={<Link to="/categories" className="text-xs font-semibold text-brand-600">View categories <ArrowRight className="ml-1 inline" size={13} /></Link>}>
        {categories.loading ? <Spinner /> : categories.error ? <ErrorState message={categories.error} onRetry={categories.retry} /> : categoryData.length === 0 ? <EmptyState title="No category data" description="Create categories and assign products to see your inventory mix." /> : <div className="flex flex-col items-center justify-center gap-2 p-4 sm:flex-row"><div className="h-[190px] w-full max-w-[220px]"><ResponsiveContainer width="100%" height="100%"><PieChart><Pie data={categoryData} dataKey="value" nameKey="name" innerRadius={56} outerRadius={82} paddingAngle={4} stroke="none">{categoryData.map((entry, index) => <Cell key={entry.name} fill={palette[index % palette.length]} />)}</Pie><Tooltip /></PieChart></ResponsiveContainer></div><div className="w-full space-y-2">{categoryData.slice(0, 5).map((row, index) => <div key={row.name} className="flex items-center justify-between gap-3 text-xs"><span className="flex min-w-0 items-center gap-2 text-slate-600"><i className="h-2 w-2 shrink-0 rounded-full" style={{ background: palette[index % palette.length] }} /><span className="truncate">{row.name}</span></span><b className="text-ink">{row.value.toLocaleString()}</b></div>)}</div></div>}
      </Section>
    </div>
    <div className="grid gap-5 xl:grid-cols-2">
      <Section title="Low stock watch" action={<Link to="/products?status=LOW_STOCK" className="text-xs font-semibold text-brand-600">All products <ArrowRight className="ml-1 inline" size={13} /></Link>}>
        {lowStock.loading ? <Spinner /> : lowStock.error ? <ErrorState message={lowStock.error} onRetry={lowStock.retry} /> : (lowStock.data ?? []).length === 0 ? <EmptyState title="You're in good shape" description="No low-stock items were reported by the inventory service." /> : <div className="divide-y divide-slate-100">{lowStock.data!.slice(0, 5).map((item) => <Link to={`/products/${item.productId}`} key={item.id} className="flex items-center justify-between gap-3 px-5 py-3.5 hover:bg-slate-50"><div className="flex min-w-0 items-center gap-3"><span className="grid h-9 w-9 shrink-0 place-items-center rounded-lg bg-amber-50 text-amber-600"><Boxes size={17} /></span><span className="min-w-0"><b className="block truncate text-sm text-ink">{item.productName ?? 'Unnamed product'}</b><small className="text-xs text-muted">{item.sku ?? item.code ?? 'No SKU'} · {item.locationName ?? item.warehouseName ?? 'Location'}</small></span></div><span className="shrink-0 text-right"><b className="block text-sm text-amber-600">{item.quantity ?? 0} left</b><small className="text-xs text-muted">Reorder at {item.lowStockThreshold ?? '—'}</small></span></Link>)}</div>}
      </Section>
      <Section title="Recent operations" action={<Link to="/operations/receipts" className="text-xs font-semibold text-brand-600">Manage operations <ArrowRight className="ml-1 inline" size={13} /></Link>}>
        {recent.loading ? <Spinner /> : recent.error ? <ErrorState message={recent.error} onRetry={recent.retry} /> : (recent.data ?? []).length === 0 ? <EmptyState title="No operations yet" description="Receipts, deliveries, transfers, and adjustments will show up here." /> : <div className="divide-y divide-slate-100">{recent.data!.slice(0, 5).map((row, index) => { const quantity = row.quantityDelta ?? row.quantity; return <div key={row.id ?? `${row.occurredAt}-${index}`} className="flex items-center gap-3 px-5 py-3.5"><span className={`grid h-9 w-9 place-items-center rounded-lg ${String(row.type ?? row.operationType).toLowerCase().includes('delivery') ? 'bg-rose-50 text-rose-500' : 'bg-emerald-50 text-emerald-600'}`}>{quantity !== undefined && quantity < 0 ? <ArrowDownRight size={17} /> : <ArrowUpRight size={17} />}</span><span className="min-w-0 flex-1"><b className="block truncate text-sm text-ink">{movementLabel(row)}</b><small className="text-xs capitalize text-muted">{row.type ?? row.operationType ?? 'Stock update'} · {formatDate(row.occurredAt ?? row.date ?? row.createdAt)}</small></span><b className="text-sm text-ink">{quantity === undefined ? '' : `${quantity > 0 ? '+' : ''}${quantity}`}</b></div>})}</div>}
      </Section>
      <Section title="Pending operations" action={<Link to="/operations/receipts" className="text-xs font-semibold text-brand-600">Open operations <ArrowRight className="ml-1 inline" size={13} /></Link>} className="xl:col-span-2">
        {pending.loading ? <Spinner /> : pending.error ? <ErrorState message={pending.error} onRetry={pending.retry} /> : (pending.data ?? []).length === 0 ? <EmptyState title="Nothing waiting for you" description="Pending stock operations will appear here when action is needed." /> : <div className="grid divide-y divide-slate-100 sm:grid-cols-2 sm:divide-x sm:divide-y-0 xl:grid-cols-3">{pending.data!.slice(0, 6).map((row, index) => <div key={row.id ?? index} className="flex items-center gap-3 p-4"><span className="grid h-9 w-9 place-items-center rounded-lg bg-indigo-50 text-brand-600"><Activity size={17} /></span><div className="min-w-0 flex-1"><p className="truncate text-sm font-semibold text-ink">{row.productName ?? row.type ?? row.operationType ?? 'Pending operation'}</p><p className="truncate text-xs text-muted">{row.status ?? 'Awaiting processing'} · {formatDate(row.date ?? row.createdAt)}</p></div><Link to={`/operations/${String(row.type ?? row.operationType ?? 'receipts').toLowerCase()}`} className="text-xs font-semibold text-brand-600">Review</Link></div>)}</div>}
      </Section>
    </div>
  </div>
}

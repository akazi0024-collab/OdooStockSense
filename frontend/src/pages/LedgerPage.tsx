import { useCallback, useMemo, useState } from 'react'
import { RotateCcw, Search, TrendingDown, TrendingUp } from 'lucide-react'
import { ledgerService } from '../services/ledger'
import { productService } from '../services/product'
import { locationService } from '../services/location'
import type { Entity, Movement } from '../services/types'
import { useRemote } from '../hooks/useRemote'
import { Button, Card, EmptyState, ErrorState, Input, PageHeader, Select, Spinner, formatDate } from '../components/ui'

export function LedgerPage() {
  const [type, setType] = useState('')
  const [productId, setProductId] = useState('')
  const [locationId, setLocationId] = useState('')
  const [from, setFrom] = useState('')
  const [to, setTo] = useState('')
  const [search, setSearch] = useState('')
  const request = useCallback(() => ledgerService.list({ locationId: locationId || undefined, size: 200 }), [locationId])
  const ledger = useRemote(request)
  const products = useRemote<Entity[]>(productService.list)
  const locations = useRemote<Entity[]>(locationService.list)
  const filtered = useMemo(() => (ledger.data ?? []).filter((row) => {
    const date = row.occurredAt ?? ''
    const content = `${row.productName ?? ''} ${row.reference ?? ''} ${row.type ?? ''}`.toLowerCase()
    return (!type || row.type === type)
      && (!productId || String(row.productId) === productId)
      && (!from || date.slice(0, 10) >= from)
      && (!to || date.slice(0, 10) <= to)
      && (!search || content.includes(search.toLowerCase()))
  }), [ledger.data, type, productId, from, to, search])
  function clear() { setType(''); setProductId(''); setLocationId(''); setFrom(''); setTo(''); setSearch('') }
  const records: Movement[] = filtered
  return <div><PageHeader eyebrow="Audit trail" title="Stock ledger" description="A traceable history of stock changes across products and locations." />
    <Card><div className="grid gap-3 border-b border-slate-100 p-4 sm:grid-cols-2 xl:grid-cols-[1.25fr_1fr_1fr_1fr_1fr_1fr_auto]">
      <div className="relative"><Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" /><Input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search ledger" className="pl-9" /></div>
      <Select aria-label="Filter movement type" value={type} onChange={(event) => setType(event.target.value)}><option value="">All movement types</option>{['RECEIPT', 'DELIVERY', 'TRANSFER_IN', 'TRANSFER_OUT', 'ADJUSTMENT_IN', 'ADJUSTMENT_OUT'].map((item) => <option key={item} value={item}>{item.replace('_', ' ')}</option>)}</Select>
      <Select aria-label="Filter product" value={productId} onChange={(event) => setProductId(event.target.value)}><option value="">All products</option>{(products.data ?? []).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select>
      <Select aria-label="Filter location" value={locationId} onChange={(event) => setLocationId(event.target.value)}><option value="">All locations</option>{(locations.data ?? []).map((item) => <option key={item.id} value={item.id}>{item.name} · {item.warehouseName}</option>)}</Select>
      <Input aria-label="From date" type="date" value={from} onChange={(event) => setFrom(event.target.value)} /><Input aria-label="To date" type="date" value={to} onChange={(event) => setTo(event.target.value)} /><Button variant="ghost" className="px-3" onClick={clear}><RotateCcw size={15} /> Clear</Button>
    </div>
      {ledger.loading ? <Spinner label="Loading ledger" /> : ledger.error ? <ErrorState message={ledger.error} onRetry={ledger.retry} /> : records.length === 0 ? <EmptyState title="No stock movements found" description="Adjust filters or check back after your first inventory operation." /> : <div className="table-scroll"><table className="w-full min-w-[790px] text-left"><thead className="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500"><tr>{['Movement', 'Product', 'Location', 'Quantity change', 'Balance after', 'Reference / user', 'Date'].map((heading) => <th key={heading} className="px-5 py-3.5">{heading}</th>)}</tr></thead><tbody className="divide-y divide-slate-100">{records.map((row, index) => { const quantity = Number(row.quantityDelta ?? row.quantity ?? 0); const positive = quantity >= 0; return <tr key={row.id ?? index} className="hover:bg-slate-50/70"><td className="px-5 py-4"><span className="inline-flex items-center gap-2"><span className={`grid h-8 w-8 place-items-center rounded-lg ${positive ? 'bg-emerald-50 text-emerald-600' : 'bg-rose-50 text-rose-600'}`}>{positive ? <TrendingUp size={15} /> : <TrendingDown size={15} />}</span><span className="text-sm font-medium capitalize text-ink">{String(row.type ?? 'Movement').toLowerCase().replace(/_/g, ' ')}</span></span></td><td className="px-5 py-4"><p className="text-sm font-semibold text-ink">{row.productName ?? row.product?.name ?? '—'}</p><p className="text-xs text-muted">{row.product?.sku ?? ''}</p></td><td className="px-5 py-4 text-sm text-slate-600">{row.locationName ?? '—'}</td><td className={`px-5 py-4 text-sm font-bold ${positive ? 'text-emerald-600' : 'text-rose-600'}`}>{positive ? '+' : ''}{quantity.toLocaleString()}</td><td className="px-5 py-4 text-sm text-slate-600">{row.balanceAfter ?? '—'}</td><td className="px-5 py-4 text-sm text-slate-600"><span className="block">{row.reference ?? '—'}</span><small className="text-xs text-muted">{row.performedBy ?? 'system'}</small></td><td className="px-5 py-4 text-sm text-muted">{formatDate(row.occurredAt ?? row.date ?? row.createdAt)}</td></tr> })}</tbody></table></div>}
      {!ledger.loading && !ledger.error && records.length > 0 && <div className="border-t border-slate-100 px-5 py-3 text-xs text-muted">Showing {records.length} matching movements from the latest 200 ledger entries</div>}
    </Card>
  </div>
}

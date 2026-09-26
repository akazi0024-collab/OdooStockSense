import { useCallback, useState, type FormEvent } from 'react'
import { NavLink, useParams } from 'react-router-dom'
import { ArrowRightLeft, ArrowUpFromLine, ClipboardPlus, PackageCheck, Plus } from 'lucide-react'
import { adjustmentsService } from '../services/adjustments'
import { deliveriesService } from '../services/deliveries'
import { receiptsService } from '../services/receipts'
import { transfersService } from '../services/transfers'
import { productService } from '../services/product'
import { locationService } from '../services/location'
import { resourceService } from '../services/resources'
import type { Entity } from '../services/types'
import { apiMessage } from '../services/api'
import { useRemote } from '../hooks/useRemote'
import { useToast } from '../context/ToastContext'
import { Button, Card, EmptyState, ErrorState, Field, Input, Modal, PageHeader, Select, Spinner, formatDate } from '../components/ui'

const operationMap = { receipts: receiptsService, deliveries: deliveriesService, transfers: transfersService, adjustments: adjustmentsService }
const partyServices = { receipts: resourceService('suppliers'), deliveries: resourceService('customers') }
const descriptions: Record<OperationKind, string> = {
  receipts: 'Record incoming stock and keep supplier deliveries accounted for.',
  deliveries: 'Record outgoing stock against customer orders.',
  transfers: 'Move stock between locations with a complete audit trail.',
  adjustments: 'Reconcile a stock count and record the reason for each change.',
}
type OperationKind = keyof typeof operationMap

function OperationForm({ kind, products, locations, parties, operation, onCancel, onSave }: {
  kind: OperationKind; products: Entity[]; locations: Entity[]; parties: Entity[]; operation?: Entity; onCancel: () => void; onSave: (body: Record<string, unknown>) => Promise<void>
}) {
  const [loading, setLoading] = useState(false)
  const [validation, setValidation] = useState('')
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setValidation('')
    const data = new FormData(event.currentTarget)
    const quantity = Number(data.get('quantity'))
    const from = String(data.get('fromLocationId') ?? '')
    const to = String(data.get('toLocationId') ?? '')
    if (!Number.isFinite(quantity) || (kind === 'adjustments' ? quantity < 0 : quantity <= 0)) { setValidation(kind === 'adjustments' ? 'Physical count cannot be negative.' : 'Quantity must be greater than zero.'); return }
    if (kind === 'transfers' && from === to) { setValidation('Choose two different locations for a transfer.'); return }
    const productId = Number(data.get('productId'))
    const payload: Record<string, unknown> = {}
    if (kind === 'receipts' || kind === 'deliveries') {
      payload.locationId = Number(data.get('locationId'))
      payload.items = [{ productId, quantity, ...(kind === 'receipts' && data.get('unitCost') ? { unitCost: Number(data.get('unitCost')) } : {}) }]
      const partyId = data.get('partyId')
      if (partyId) payload[kind === 'receipts' ? 'supplierId' : 'customerId'] = Number(partyId)
    } else if (kind === 'transfers') {
      payload.productId = productId
      payload.quantity = quantity
      payload.fromLocationId = Number(from)
      payload.toLocationId = Number(to)
    } else {
      payload.productId = productId
      payload.physicalQuantity = quantity
      payload.locationId = Number(data.get('locationId'))
      payload.reason = data.get('reason')
    }
    setLoading(true)
    try { await onSave(payload) } catch { /* parent displays the API error */ } finally { setLoading(false) }
  }
  const locationSelect = (name: string, label: string) => <Field label={label}><Select name={name} required defaultValue={String(name === 'fromLocationId' ? operation?.fromLocationId ?? '' : name === 'toLocationId' ? operation?.toLocationId ?? '' : operation?.locationId ?? '')}><option value="" disabled>Select a location</option>{locations.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.warehouseName}</option>)}</Select></Field>
  return <form onSubmit={(event) => void submit(event)} className="space-y-4">
    <div className="grid gap-4 sm:grid-cols-2">
      <Field label="Product"><Select name="productId" required defaultValue={String(operation?.productId ?? operation?.items?.[0]?.productId ?? '')}><option value="" disabled>Select a product</option>{products.map((item) => <option key={item.id} value={item.id}>{item.name} · {item.sku}</option>)}</Select></Field>
      {kind === 'transfers' ? <>{locationSelect('fromLocationId', 'From location')}{locationSelect('toLocationId', 'To location')}</> : locationSelect('locationId', 'Stock location')}
      <Field label={kind === 'adjustments' ? 'Physical count' : 'Quantity'}><Input name="quantity" type="number" min={kind === 'adjustments' ? '0' : '0.001'} step="0.001" required defaultValue={String(kind === 'adjustments' ? operation?.physicalQuantity ?? 0 : operation?.quantity ?? operation?.items?.[0]?.quantity ?? '')} placeholder="0" /></Field>
      {(kind === 'receipts' || kind === 'deliveries') && <Field label={kind === 'receipts' ? 'Supplier (optional)' : 'Customer (optional)'}><Select name="partyId" defaultValue={String(operation?.partyId ?? '')}><option value="">No {kind === 'receipts' ? 'supplier' : 'customer'}</option>{parties.map((party) => <option key={party.id} value={party.id}>{party.name}</option>)}</Select></Field>}
      {kind === 'receipts' && <Field label="Unit cost (optional)"><Input name="unitCost" type="number" min="0" step="0.01" defaultValue={String(operation?.items?.[0]?.unitCost ?? '')} placeholder="0.00" /></Field>}
      {kind === 'adjustments' && <Field label="Reason"><Input name="reason" required maxLength={250} defaultValue={operation?.reason ?? ''} placeholder="e.g. Physical count correction" /></Field>}
      {kind === 'adjustments' && <p className="sm:col-span-2 text-xs text-muted">Enter the physical quantity counted at this location. The server compares it with current stock when the adjustment is validated.</p>}
    </div>
    {validation && <p role="alert" className="rounded-lg bg-rose-50 px-3 py-2 text-sm text-rose-700">{validation}</p>}
    <div className="flex justify-end gap-2 border-t border-slate-100 pt-4"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit" loading={loading}>{operation ? 'Save changes' : `Create ${kind.slice(0, -1)}`}</Button></div>
  </form>
}

export function OperationsPage({ kind: kindOverride }: { kind?: OperationKind } = {}) {
  const { kind: routeKind = 'receipts' } = useParams()
  const currentKind = kindOverride ?? routeKind
  const kind = (currentKind in operationMap ? currentKind : 'receipts') as OperationKind
  const service = operationMap[kind]
  const request = useCallback(() => service.list(), [service])
  const operations = useRemote(request)
  const products = useRemote(productService.list)
  const locations = useRemote(locationService.list)
  const partyKind = kind === 'receipts' ? 'receipts' : 'deliveries'
  const parties = useRemote(partyServices[partyKind].list)
  const [open, setOpen] = useState(false)
  const [editing, setEditing] = useState<Entity | null>(null)
  const { notify } = useToast()
  async function save(payload: Record<string, unknown>) {
    try {
      if (editing) await service.update(editing.id, payload)
      else await service.create(payload)
      notify(editing ? `${kind.slice(0, -1)} updated.` : `${kind.slice(0, 1).toUpperCase()}${kind.slice(1, -1)} recorded as a draft.`)
      setOpen(false); setEditing(null); operations.retry()
    }
    catch (error) { notify(apiMessage(error), 'error'); throw error }
  }
  async function remove(row: Entity) {
    if (!window.confirm(`Delete draft ${row.reference ?? 'operation'}?`)) return
    try { await service.remove(row.id); notify('Draft deleted.'); operations.retry() }
    catch (error) { notify(apiMessage(error), 'error') }
  }
  async function advance(row: Entity, action: 'validate' | 'pick' | 'pack') {
    try {
      if (action === 'validate') await service.validate(row.id)
      else if (action === 'pick' && kind === 'deliveries') await deliveriesService.pick(row.id)
      else if (action === 'pack' && kind === 'deliveries') await deliveriesService.pack(row.id)
      notify(`${row.reference ?? 'Operation'} ${action === 'validate' ? 'validated' : action === 'pick' ? 'marked picked' : 'marked packed'}.`)
      operations.retry()
    } catch (error) { notify(apiMessage(error), 'error') }
  }
  const tabs = [
    { key: 'receipts', label: 'Receipts', icon: PackageCheck },
    { key: 'deliveries', label: 'Deliveries', icon: ArrowUpFromLine },
    { key: 'transfers', label: 'Transfers', icon: ArrowRightLeft },
    { key: 'adjustments', label: 'Adjustments', icon: ClipboardPlus },
  ] as const
  return <div><PageHeader eyebrow="Inventory control" title="Stock operations" description={descriptions[kind]} action={<Button onClick={() => { setEditing(null); setOpen(true) }}><Plus size={17} /> New {kind.slice(0, -1)}</Button>} />
    <div className="mb-5 flex gap-1 overflow-x-auto rounded-xl border border-slate-100 bg-white p-1.5 shadow-card">{tabs.map(({ key, label, icon: Icon }) => <NavLink key={key} to={`/operations/${key}`} className={({ isActive }) => `inline-flex shrink-0 items-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition ${isActive ? 'bg-brand-600 text-white shadow-sm' : 'text-slate-500 hover:bg-slate-50 hover:text-ink'}`}><Icon size={16} />{label}</NavLink>)}</div>
    <Card><div className="flex items-center justify-between border-b border-slate-100 px-5 py-4"><div><h2 className="text-sm font-bold capitalize text-ink">{kind} history</h2><p className="mt-1 text-xs text-muted">Draft documents can be advanced through the inventory API.</p></div><Button variant="secondary" className="min-h-9 px-3" onClick={operations.retry}>Refresh</Button></div>
      {operations.loading ? <Spinner label={`Loading ${kind}`} /> : operations.error ? <ErrorState message={operations.error} onRetry={operations.retry} /> : (operations.data ?? []).length === 0 ? <EmptyState title={`No ${kind} recorded`} description="Create an operation to start an auditable stock workflow." /> : <div className="table-scroll"><table className="w-full min-w-[860px] text-left"><thead className="bg-slate-50 text-[11px] font-bold uppercase tracking-wider text-slate-500"><tr>{['Reference', 'Product / items', 'Location', 'Quantity', 'Status', 'Created', 'Action'].map((heading) => <th key={heading} className="px-5 py-3.5">{heading}</th>)}</tr></thead><tbody className="divide-y divide-slate-100">{operations.data!.map((row, index) => {
        const status = String(row.status ?? 'DRAFT').toUpperCase()
        const product = row.productName ?? row.items?.map((line) => line.productName).filter(Boolean).join(', ') ?? '—'
        const quantity = row.quantity ?? row.difference ?? row.quantityDelta ?? row.items?.reduce((sum, line) => sum + Number(line.quantity ?? 0), 0)
        const date = row.createdAt
        const action = kind === 'deliveries' ? status === 'DRAFT' ? 'pick' : status === 'PICKED' ? 'pack' : status === 'PACKED' ? 'validate' : null : status === 'DRAFT' ? 'validate' : null
        return <tr key={row.id ?? index} className="hover:bg-slate-50/70"><td className="px-5 py-4 text-sm font-semibold text-ink">{row.reference ?? `#${row.id}`}</td><td className="px-5 py-4 text-sm text-slate-600">{product}</td><td className="px-5 py-4 text-sm text-slate-600">{row.locationName ?? row.fromLocationName ?? '—'}{row.toLocationName ? ` → ${row.toLocationName}` : ''}{row.partyName ? <small className="block text-xs text-muted">{row.partyName}</small> : null}</td><td className="px-5 py-4 text-sm font-semibold text-ink">{quantity ?? '—'}</td><td className="px-5 py-4"><span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${status === 'VALIDATED' ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'}`}>{status}</span></td><td className="px-5 py-4 text-sm text-muted">{formatDate(date)}</td><td className="px-5 py-4"><div className="flex items-center gap-2">{status === 'DRAFT' && <><Button variant="secondary" className="min-h-8 px-2.5 text-xs" onClick={() => { setEditing(row); setOpen(true) }}>Edit</Button><Button variant="danger" className="min-h-8 px-2.5 text-xs" onClick={() => void remove(row)}>Delete</Button></>}{action && <Button className="min-h-8 px-3 text-xs" onClick={() => void advance(row, action)}>{action === 'validate' ? 'Validate' : action === 'pick' ? 'Pick' : 'Pack'}</Button>}{!action && status !== 'DRAFT' && <span className="text-xs text-muted">Complete</span>}</div></td></tr>
      })}</tbody></table></div>}
    </Card>
    {open && <Modal title={`${editing ? 'Edit' : 'New'} ${kind.slice(0, -1)}`} onClose={() => { setOpen(false); setEditing(null) }}>{products.error ? <ErrorState message={products.error} onRetry={products.retry} /> : locations.error ? <ErrorState message={locations.error} onRetry={locations.retry} /> : parties.error ? <ErrorState message={parties.error} onRetry={parties.retry} /> : products.loading || locations.loading || parties.loading ? <Spinner label="Loading products, locations, and contacts" /> : <OperationForm kind={kind} products={products.data ?? []} locations={locations.data ?? []} parties={parties.data ?? []} operation={editing ?? undefined} onCancel={() => { setOpen(false); setEditing(null) }} onSave={save} />}</Modal>}</div>
}

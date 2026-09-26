import { useCallback, useMemo, useState, type FormEvent } from 'react'
import { Link, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { ArrowLeft, Eye, Package, Pencil, Plus, Search, Trash2 } from 'lucide-react'
import { productService } from '../services/product'
import { categoryService } from '../services/category'
import { warehouseService } from '../services/warehouse'
import { stockService } from '../services/stock'
import { ledgerService } from '../services/ledger'
import type { Entity } from '../services/types'
import { apiMessage } from '../services/api'
import { useRemote } from '../hooks/useRemote'
import { useToast } from '../context/ToastContext'
import { Button, Card, EmptyState, ErrorState, Field, Input, Modal, PageHeader, Select, Spinner } from '../components/ui'

type ProductFormProps = { product?: Entity; categories: Entity[]; onCancel: () => void; onSave: (payload: Record<string, unknown>) => Promise<void> }

function ProductForm({ product, categories, onCancel, onSave }: ProductFormProps) {
  const [loading, setLoading] = useState(false)
  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    const form = new FormData(event.currentTarget)
    const payload: Record<string, unknown> = {
      name: form.get('name'), sku: form.get('sku'), description: form.get('description') || undefined,
      unitPrice: Number(form.get('unitPrice')), lowStockThreshold: Number(form.get('lowStockThreshold')),
      unitOfMeasure: form.get('unitOfMeasure'), active: form.get('active') === 'true',
    }
    if (form.get('categoryId')) payload.categoryId = Number(form.get('categoryId'))
    try { await onSave(payload) } catch { /* save failures are announced by the parent */ } finally { setLoading(false) }
  }
  return <form onSubmit={(event) => void submit(event)} className="space-y-4">
    <div className="grid gap-4 sm:grid-cols-2">
      <Field label="Product name"><Input name="name" required defaultValue={product?.name ?? ''} placeholder="e.g. Wireless keyboard" /></Field>
      <Field label="SKU"><Input name="sku" required defaultValue={product?.sku ?? ''} placeholder="e.g. KB-104" /></Field>
      <Field label="Unit price"><Input name="unitPrice" type="number" min="0" step="0.01" required defaultValue={product?.unitPrice ?? 0} /></Field>
      <Field label="Unit of measure"><Input name="unitOfMeasure" required maxLength={24} defaultValue={String(product?.unitOfMeasure ?? 'unit')} placeholder="unit, kg, m..." /></Field>
      <Field label="Low-stock threshold"><Input name="lowStockThreshold" type="number" min="0" step="1" required defaultValue={product?.lowStockThreshold ?? 5} /></Field>
      <Field label="Status"><Select name="active" defaultValue={String(product?.active ?? true)}><option value="true">Active</option><option value="false">Inactive</option></Select></Field>
      <Field label="Category"><Select name="categoryId" defaultValue={String(product?.categoryId ?? '')}><option value="">No category</option>{categories.map((category) => <option key={category.id} value={category.id}>{category.name}</option>)}</Select></Field>
    </div>
    <Field label="Description"><textarea name="description" rows={3} defaultValue={String(product?.description ?? '')} placeholder="Add helpful product details" className="w-full rounded-lg border border-slate-200 px-3.5 py-3 text-sm outline-none focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10" /></Field>
    <p className="text-xs text-muted">Stock quantities are changed through receipts, deliveries, transfers, and adjustments.</p>
    <div className="flex justify-end gap-2 border-t border-slate-100 pt-4"><Button type="button" variant="secondary" onClick={onCancel}>Cancel</Button><Button type="submit" loading={loading}>{product ? 'Save changes' : 'Create product'}</Button></div>
  </form>
}

function totalStock(stock: Entity[]) { return stock.reduce((sum, row) => sum + Number(row.quantity ?? 0), 0) }

export function ProductsPage() {
  const { notify } = useToast()
  const [params, setParams] = useSearchParams()
  const navigate = useNavigate()
  const [search, setSearch] = useState(params.get('search') ?? '')
  const [status, setStatus] = useState(params.get('status') ?? '')
  const [category, setCategory] = useState('')
  const [warehouse, setWarehouse] = useState('')
  const [formProduct, setFormProduct] = useState<Entity | null | undefined>(undefined)
  const products = useRemote(productService.list)
  const inventory = useRemote(stockService.list)
  const categories = useRemote(categoryService.list)
  const warehouses = useRemote(warehouseService.list)
  const rows = useMemo(() => (products.data ?? []).map((product) => {
    const positions = (inventory.data ?? []).filter((row) => String(row.productId) === String(product.id))
    return { ...product, quantity: totalStock(positions), warehouseName: [...new Set(positions.map((row) => row.warehouseName ?? row.locationName).filter(Boolean))].join(', ') }
  }).filter((product) => {
    const text = `${product.name ?? ''} ${product.sku ?? ''}`.toLowerCase()
    const stockStatus = Number(product.quantity) <= 0 ? 'OUT_OF_STOCK' : Number(product.quantity) <= Number(product.lowStockThreshold ?? 0) ? 'LOW_STOCK' : 'IN_STOCK'
    return (!search || text.includes(search.toLowerCase()))
      && (!category || String(product.categoryId) === category)
      && (!warehouse || (inventory.data ?? []).some((row) => String(row.productId) === String(product.id) && row.warehouseName === warehouse))
      && (!status || stockStatus === status)
  }), [products.data, inventory.data, search, category, warehouse, status])
  async function save(payload: Record<string, unknown>) {
    try {
      if (formProduct) await productService.update(formProduct.id, payload)
      else await productService.create(payload)
      notify(formProduct ? 'Product updated.' : 'Product created.')
      setFormProduct(undefined)
      products.retry()
      inventory.retry()
    } catch (error) { notify(apiMessage(error), 'error'); throw error }
  }
  async function remove(product: Entity) {
    if (!window.confirm(`Delete ${product.name ?? 'this product'}? This cannot be undone.`)) return
    try { await productService.remove(product.id); notify('Product deleted.'); products.retry(); inventory.retry() }
    catch (error) { notify(apiMessage(error), 'error') }
  }
  function updateFilter(key: string, value: string) {
    const next = new URLSearchParams(params)
    value ? next.set(key, value) : next.delete(key)
    setParams(next, { replace: true })
  }
  return <div>
    <PageHeader eyebrow="Catalog" title="Products" description="Manage your product catalog and keep stock levels in view." action={<Button onClick={() => setFormProduct(null)}><Plus size={17} /> Add product</Button>} />
    <Card>
      <div className="grid gap-3 border-b border-slate-100 p-4 sm:grid-cols-2 xl:grid-cols-[1.5fr_1fr_1fr_1fr]">
        <div className="relative"><Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" /><Input aria-label="Search products" value={search} onChange={(event) => { setSearch(event.target.value); updateFilter('search', event.target.value) }} placeholder="Search name or SKU" className="pl-9" /></div>
        <Select aria-label="Filter by stock status" value={status} onChange={(event) => { setStatus(event.target.value); updateFilter('status', event.target.value) }}><option value="">All stock statuses</option><option value="IN_STOCK">In stock</option><option value="LOW_STOCK">Low stock</option><option value="OUT_OF_STOCK">Out of stock</option></Select>
        <Select aria-label="Filter by category" value={category} onChange={(event) => setCategory(event.target.value)}><option value="">All categories</option>{(categories.data ?? []).map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</Select>
        <Select aria-label="Filter by warehouse" value={warehouse} onChange={(event) => setWarehouse(event.target.value)}><option value="">All warehouses</option>{(warehouses.data ?? []).map((item) => <option key={item.id} value={item.name}>{item.name}</option>)}</Select>
      </div>
      {products.loading || inventory.loading ? <Spinner label="Loading products and stock" /> : products.error ? <ErrorState message={products.error} onRetry={products.retry} /> : inventory.error ? <ErrorState message={`Products loaded, but stock positions could not be loaded. ${inventory.error}`} onRetry={inventory.retry} /> : rows.length === 0 ? <EmptyState title="No products found" description="Try adjusting your filters, or add your first product to the catalog." /> : <div className="table-scroll"><table className="w-full min-w-[760px] text-left"><thead className="bg-slate-50/80 text-[11px] font-bold uppercase tracking-wider text-slate-500"><tr>{['Product', 'Category', 'Warehouse', 'In stock', 'Unit price', 'Status', ''].map((heading) => <th key={heading} className="px-5 py-3.5">{heading}</th>)}</tr></thead><tbody className="divide-y divide-slate-100">{rows.map((product) => {
        const quantity = Number(product.quantity)
        const stockStatus = quantity <= 0 ? 'OUT_OF_STOCK' : quantity <= Number(product.lowStockThreshold ?? 0) ? 'LOW_STOCK' : 'IN_STOCK'
        const statusStyle = stockStatus === 'OUT_OF_STOCK' ? 'bg-rose-50 text-rose-700' : stockStatus === 'LOW_STOCK' ? 'bg-amber-50 text-amber-700' : 'bg-emerald-50 text-emerald-700'
        return <tr key={product.id} className="group hover:bg-slate-50/70"><td className="px-5 py-3.5"><div className="flex items-center gap-3"><span className="grid h-9 w-9 place-items-center rounded-lg bg-indigo-50 text-brand-600"><Package size={17} /></span><span><Link to={`/products/${product.id}`} className="block text-sm font-semibold text-ink hover:text-brand-600">{product.name ?? 'Unnamed product'}</Link><small className="text-xs text-muted">{product.sku ?? 'No SKU'}</small></span></div></td><td className="px-5 py-3.5 text-sm text-slate-600">{product.categoryName ?? '—'}</td><td className="px-5 py-3.5 text-sm text-slate-600">{product.warehouseName || '—'}</td><td className="px-5 py-3.5"><span className={`text-sm font-semibold ${stockStatus === 'LOW_STOCK' ? 'text-amber-600' : stockStatus === 'OUT_OF_STOCK' ? 'text-rose-600' : 'text-ink'}`}>{quantity.toLocaleString()} <small className="font-normal text-muted">{product.unitOfMeasure ?? 'unit'}</small></span></td><td className="px-5 py-3.5 text-sm text-slate-600">{typeof product.unitPrice === 'number' ? new Intl.NumberFormat(undefined, { style: 'currency', currency: 'USD' }).format(product.unitPrice) : '—'}</td><td className="px-5 py-3.5"><span className={`rounded-full px-2.5 py-1 text-[11px] font-semibold ${statusStyle}`}>{stockStatus.replace('_', ' ')}</span></td><td className="px-5 py-3.5"><div className="flex justify-end gap-1 opacity-70 transition group-hover:opacity-100"><button title="View product" onClick={() => navigate(`/products/${product.id}`)} className="rounded-md p-2 text-slate-500 hover:bg-indigo-50 hover:text-brand-600"><Eye size={16} /></button><button title="Edit product" onClick={() => setFormProduct(product)} className="rounded-md p-2 text-slate-500 hover:bg-indigo-50 hover:text-brand-600"><Pencil size={16} /></button><button title="Delete product" onClick={() => void remove(product)} className="rounded-md p-2 text-slate-500 hover:bg-rose-50 hover:text-rose-600"><Trash2 size={16} /></button></div></td></tr>
      })}</tbody></table></div>}
      {!products.loading && !products.error && rows.length > 0 && <div className="flex items-center justify-between border-t border-slate-100 px-5 py-3 text-xs text-muted"><span>{rows.length} products</span><span>Catalog and stock positions</span></div>}
    </Card>
    {formProduct !== undefined && <Modal title={formProduct ? 'Edit product' : 'Add a product'} onClose={() => setFormProduct(undefined)}><ProductForm product={formProduct ?? undefined} categories={categories.data ?? []} onCancel={() => setFormProduct(undefined)} onSave={save} /></Modal>}
  </div>
}

export function ProductDetailPage() {
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const { notify } = useToast()
  const [editing, setEditing] = useState(false)
  const loader = useCallback(() => productService.get(id), [id])
  const stockLoader = useCallback(() => stockService.list({ productId: id }), [id])
  const product = useRemote(loader)
  const inventory = useRemote(stockLoader)
  const ledgerLoader = useCallback(() => ledgerService.product(id), [id])
  const movements = useRemote(ledgerLoader)
  const categories = useRemote(categoryService.list)
  async function save(payload: Record<string, unknown>) {
    try { await productService.update(id, payload); notify('Product updated.'); setEditing(false); product.retry() }
    catch (error) { notify(apiMessage(error), 'error'); throw error }
  }
  async function remove() {
    if (!product.data || !window.confirm(`Delete ${product.data.name ?? 'this product'}? This cannot be undone.`)) return
    try { await productService.remove(id); notify('Product deleted.'); navigate('/products') }
    catch (error) { notify(apiMessage(error), 'error') }
  }
  if (product.loading) return <Spinner label="Loading product" />
  if (product.error || !product.data) return <ErrorState message={product.error ?? 'Product was not found.'} onRetry={product.retry} />
  const item = product.data
  const positions = inventory.data ?? []
  const units = totalStock(positions)
  return <div><button onClick={() => navigate('/products')} className="mb-5 inline-flex items-center gap-2 text-sm font-semibold text-muted hover:text-ink"><ArrowLeft size={16} /> Back to products</button><PageHeader eyebrow="Product details" title={String(item.name ?? 'Product')} description={`SKU ${item.sku ?? '—'} · ${item.active === false ? 'Inactive' : 'Active'}`} action={<div className="flex gap-2"><Button variant="secondary" onClick={() => setEditing(true)}>Edit product</Button><Button variant="danger" onClick={() => void remove()}><Trash2 size={15} /> Delete</Button></div>} />
    <div className="mb-5 flex flex-wrap gap-2">{[['Receipt', 'receipts'], ['Delivery', 'deliveries'], ['Transfer', 'transfers'], ['Adjustment', 'adjustments']].map(([label, route]) => <Link key={route} to={`/operations/${route}`} className="inline-flex min-h-9 items-center rounded-lg border border-slate-200 bg-white px-3 text-sm font-semibold text-slate-600 transition hover:border-brand-200 hover:bg-indigo-50 hover:text-brand-700">Create {label}</Link>)}</div>
    <div className="grid gap-5 lg:grid-cols-[1.4fr_1fr]"><Card className="p-6"><h2 className="mb-5 text-sm font-bold text-ink">Product information</h2><dl className="grid gap-x-6 gap-y-5 sm:grid-cols-2">{[['SKU', item.sku], ['Category', item.categoryName], ['Status', item.active === false ? 'Inactive' : 'Active'], ['Unit of measure', item.unitOfMeasure ?? 'unit'], ['Unit price', item.unitPrice], ['Low-stock threshold', item.lowStockThreshold], ['Description', item.description]].map(([label, value]) => <div key={String(label)}><dt className="text-xs font-medium text-muted">{label}</dt><dd className="mt-1 text-sm font-semibold text-ink">{value === undefined || value === null || value === '' ? '—' : String(value)}</dd></div>)}</dl></Card><Card className="p-6"><h2 className="mb-5 text-sm font-bold text-ink">Stock position</h2>{inventory.loading ? <Spinner label="Loading stock positions" /> : inventory.error ? <ErrorState message={inventory.error} onRetry={inventory.retry} /> : <><div className="rounded-xl bg-indigo-50 p-5"><p className="text-sm text-indigo-800">Available quantity · all locations</p><p className="mt-2 text-4xl font-bold text-ink">{units.toLocaleString()}</p></div><div className="mt-5 divide-y divide-slate-100">{positions.length ? positions.map((position) => <div key={position.id} className="flex justify-between gap-3 py-3 text-sm"><span className="text-muted">{position.locationName ?? position.warehouseName ?? 'Location'}</span><b>{position.quantity ?? 0} {item.unitOfMeasure ?? 'unit'}</b></div>) : <p className="py-4 text-sm text-muted">No stock has been recorded at any location.</p>}</div></>}</Card></div>
    <Card className="mt-5"><div className="border-b border-slate-100 px-5 py-4"><h2 className="text-sm font-bold text-ink">Recent stock movements</h2></div>{movements.loading ? <Spinner label="Loading product movements" /> : movements.error ? <ErrorState message={movements.error} onRetry={movements.retry} /> : (movements.data ?? []).length === 0 ? <EmptyState title="No movements recorded" description="Validated receipts, deliveries, transfers, and adjustments will appear here." /> : <div className="divide-y divide-slate-100">{movements.data!.slice(0, 8).map((movement) => <div key={movement.id} className="flex flex-wrap items-center justify-between gap-2 px-5 py-3"><span className="text-sm font-medium text-ink">{movement.reference} · {movement.type?.replace(/_/g, ' ')}</span><span className="text-xs text-muted">{movement.locationName} · {movement.performedBy ?? 'system'} · {movement.occurredAt ? new Date(movement.occurredAt).toLocaleString() : '—'}</span><b className={`text-sm ${Number(movement.quantityDelta) >= 0 ? 'text-emerald-600' : 'text-rose-600'}`}>{Number(movement.quantityDelta) > 0 ? '+' : ''}{movement.quantityDelta}</b></div>)}</div>}</Card>
    {editing && <Modal title="Edit product" onClose={() => setEditing(false)}><ProductForm product={item} categories={categories.data ?? []} onCancel={() => setEditing(false)} onSave={save} /></Modal>}
  </div>
}

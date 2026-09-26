export type Entity = {
  id: string | number
  name?: string
  sku?: string
  description?: string
  code?: string
  email?: string
  phone?: string
  status?: string
  quantity?: number
  currentStock?: number
  quantityDelta?: number
  systemQuantity?: number
  physicalQuantity?: number
  difference?: number
  lowStockThreshold?: number
  reorderLevel?: number
  price?: number
  unitPrice?: number
  unitOfMeasure?: string
  active?: boolean
  address?: string
  contactPerson?: string
  locationId?: string | number
  locationName?: string
  warehouseId?: string | number
  warehouseName?: string
  categoryId?: string | number
  categoryName?: string
  reference?: string
  performedBy?: string
  createdAt?: string
  occurredAt?: string
  balanceAfter?: number
  partyName?: string
  productName?: string
  product?: { name?: string; sku?: string }
  items?: { productId?: string | number; productName?: string; sku?: string; quantity?: number; unitCost?: number }[]
  fromLocationId?: string | number
  toLocationId?: string | number
  fromLocationName?: string
  toLocationName?: string
  reason?: string
  [key: string]: unknown
}

export type User = {
  id?: string | number
  name?: string
  fullName?: string
  email: string
  role?: string
  avatarUrl?: string
  roles?: string[]
}

export type DashboardStats = {
  productsInStock?: number
  lowStockItems?: number
  outOfStockItems?: number
  pendingReceipts?: number
  pendingDeliveries?: number
  internalTransfers?: number
  products?: number
  locations?: number
  suppliers?: number
  customers?: number
  lowStockLines?: number
  inventoryUnits?: number
  totalProducts?: number
  totalItems?: number
  inventoryValue?: number
  totalValue?: number
  lowStockCount?: number
  activeWarehouses?: number
  warehouseCount?: number
  [key: string]: unknown
}

export type Movement = {
  id?: string | number
  date?: string
  createdAt?: string
  productName?: string
  product?: { name?: string; sku?: string }
  type?: string
  operationType?: string
  quantity?: number
  quantityDelta?: number
  balanceAfter?: number
  reference?: string
  performedBy?: string
  locationName?: string
  locationId?: string | number
  occurredAt?: string
  warehouseName?: string
  [key: string]: unknown
}

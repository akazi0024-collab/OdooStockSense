import { Navigate, Route, Routes } from 'react-router-dom'
import type { ReactNode } from 'react'
import { useAuth } from './context/AuthContext'
import { Spinner } from './components/ui'
import { AppShell } from './components/AppShell'
import { LoginPage, SignupPage, ForgotPasswordPage, VerifyOtpPage, ResetPasswordPage } from './pages/AuthPages'
import { DashboardPage } from './pages/DashboardPage'
import { ProductsPage, ProductDetailPage } from './pages/ProductsPage'
import { OperationsPage } from './pages/OperationsPage'
import { LedgerPage } from './pages/LedgerPage'
import { DirectoryPage } from './pages/DirectoryPage'
import { SettingsPage } from './pages/SettingsPage'

function Protected({ children }: { children: ReactNode }) {
  const { user, ready } = useAuth()
  if (!ready) return <div className="grid min-h-screen place-items-center"><Spinner label="Preparing your workspace" /></div>
  return user ? <>{children}</> : <Navigate to="/login" replace />
}

function AuthOnly({ children }: { children: ReactNode }) {
  const { user, ready } = useAuth()
  if (!ready) return <div className="grid min-h-screen place-items-center"><Spinner label="Loading" /></div>
  return user ? <Navigate to="/dashboard" replace /> : <>{children}</>
}

export default function App() {
  return <Routes>
    <Route path="/login" element={<AuthOnly><LoginPage /></AuthOnly>} />
    <Route path="/signup" element={<AuthOnly><SignupPage /></AuthOnly>} />
    <Route path="/forgot-password" element={<ForgotPasswordPage />} />
    <Route path="/verify-otp" element={<VerifyOtpPage />} />
    <Route path="/reset-password" element={<ResetPasswordPage />} />
    <Route element={<Protected><AppShell /></Protected>}>
      <Route path="/" element={<Navigate to="/dashboard" replace />} />
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/products" element={<ProductsPage />} />
      <Route path="/products/:id" element={<ProductDetailPage />} />
      <Route path="/operations/:kind" element={<OperationsPage />} />
      <Route path="/receipts" element={<OperationsPage kind="receipts" />} />
      <Route path="/deliveries" element={<OperationsPage kind="deliveries" />} />
      <Route path="/transfers" element={<OperationsPage kind="transfers" />} />
      <Route path="/adjustments" element={<OperationsPage kind="adjustments" />} />
      <Route path="/ledger" element={<LedgerPage />} />
      <Route path="/categories" element={<DirectoryPage kind="categories" />} />
      <Route path="/warehouses" element={<DirectoryPage kind="warehouses" />} />
      <Route path="/locations" element={<DirectoryPage kind="locations" />} />
      <Route path="/suppliers" element={<DirectoryPage kind="suppliers" />} />
      <Route path="/customers" element={<DirectoryPage kind="customers" />} />
      <Route path="/settings" element={<SettingsPage />} />
    </Route>
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>
}

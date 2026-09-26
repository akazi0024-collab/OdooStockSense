import { useCallback, useEffect, useState } from 'react'
import { apiMessage } from '../services/api'

export function useRemote<T>(loader: () => Promise<T>) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(true)
  const [revision, setRevision] = useState(0)
  const retry = useCallback(() => setRevision((value) => value + 1), [])
  useEffect(() => {
    let active = true
    setLoading(true); setError(null)
    loader().then((result) => { if (active) setData(result) }).catch((reason: unknown) => { if (active) setError(apiMessage(reason)) }).finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [loader, revision])
  return { data, error, loading, retry }
}

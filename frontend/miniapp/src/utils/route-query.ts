/**
 * Normalize route query values from uni-app page lifecycle hooks.
 * IDs must stay as strings because backend Snowflake values exceed JS integer precision.
 */
export function normalizeRouteParam(value: unknown): string {
  const rawValue = Array.isArray(value) ? value[0] : value
  if (rawValue === undefined || rawValue === null) return ''

  const text = String(rawValue).trim()
  if (!text) return ''

  try {
    return decodeURIComponent(text)
  } catch {
    return text
  }
}

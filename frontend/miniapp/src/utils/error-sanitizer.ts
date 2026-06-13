/**
 * Error message sanitizer for the user-facing H5 app.
 *
 * Ensures backend error details (SQL, stack traces, provider keys)
 * never reach the user interface. Adapted from admin-web with
 * additional patterns for the user app context.
 */

const GENERIC_MESSAGE = '操作失败，请稍后重试'

/** Patterns that indicate unsafe backend error content */
const UNSAFE_PATTERNS = [
  /\bSELECT\b.*\bFROM\b/i,
  /\bINSERT\b.*\bINTO\b/i,
  /\bUPDATE\b.*\bSET\b/i,
  /\bDELETE\b.*\bFROM\b/i,
  /\bjava\.\w+\.\w+/,
  /\borg\.\w+\.\w+/,
  /\bcom\.\w+\.\w+/,
  /\bat\s+[\w.]+\(/,
  /\bException\b/,
  /\bStackTrace\b/i,
  /\bapi[_-]?key\b/i,
  /\bsk-[a-zA-Z0-9]+/,
  /\bDeepSeek\b/i,
  /\bOpenAI\b/i,
  /\bprovider\b.*\berror\b/i,
  /\bopenid\b/i,
  /\bsession_key\b/i,
  /\baccess_token\b/i,
  /\bpassword\b/i,
  /\bcredential/i,
]

const MAX_SAFE_LENGTH = 200

/**
 * Sanitize an error message for safe display to end users.
 * Returns the generic message if the input contains any unsafe pattern.
 */
export function sanitizeErrorMessage(msg: unknown): string {
  if (typeof msg !== 'string' || msg.length === 0) {
    return GENERIC_MESSAGE
  }

  if (msg.length > MAX_SAFE_LENGTH) {
    return GENERIC_MESSAGE
  }

  for (const pattern of UNSAFE_PATTERNS) {
    if (pattern.test(msg)) {
      return GENERIC_MESSAGE
    }
  }

  return msg
}

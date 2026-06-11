/**
 * H06: Staff & Scheduling Tests
 *
 * Tests verify:
 * 1. Staff page uses shared components (FilterBar, DataTableShell, DetailDrawer, ActionConfirmDialog)
 * 2. Staff page uses PetCare design tokens
 * 3. Staff page uses feedback utils (not ElMessage direct)
 * 4. Disable action uses ActionConfirmDialog (not ElMessageBox)
 * 5. Schedule dialog shows conflict/save failure feedback
 * 6. Skill editing is blocked (no GET endpoint)
 * 7. Permission checks on all action buttons
 * 8. canDisableStaff guard used for disable button visibility
 */

import { describe, it, expect, beforeEach } from 'vitest'
import { fileURLToPath } from 'url'
import { dirname, resolve } from 'path'
import { readFileSync } from 'fs'

const __filename = fileURLToPath(import.meta.url)
const __dirname = dirname(__filename)

function readFile(relativePath: string): string {
  return readFileSync(resolve(__dirname, relativePath), 'utf-8')
}

// ─── Shared Component Integration ───

describe('Staff page shared component integration', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('imports and uses FilterBar component', () => {
    expect(source).toMatch(/FilterBar/)
  })

  it('imports and uses DataTableShell component', () => {
    expect(source).toMatch(/DataTableBarShell|DataTableShell/)
  })

  it('imports and uses ActionConfirmDialog for disable action', () => {
    expect(source).toMatch(/ActionConfirmDialog/)
  })
})

// ─── Design Tokens ───

describe('Staff page design tokens', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('uses PetCare design tokens in styles', () => {
    expect(source).toMatch(/--pc-/)
  })

  it('does not use hardcoded padding: 20px', () => {
    expect(source).not.toMatch(/padding:\s*20px/)
  })
})

// ─── Feedback Utils ───

describe('Staff page feedback utils', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('imports showSuccess from feedback utils', () => {
    expect(source).toMatch(/showSuccess/)
  })

  it('does not use ElMessage.success directly', () => {
    expect(source).not.toMatch(/ElMessage\.success/)
  })

  it('does not use ElMessageBox.confirm (uses ActionConfirmDialog)', () => {
    expect(source).not.toMatch(/ElMessageBox/)
  })
})

// ─── Permission Checks ───

describe('Staff page permission checks', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('checks create permission on add button', () => {
    expect(source).toMatch(/staff:profile:create/)
  })

  it('checks update permission on edit button', () => {
    expect(source).toMatch(/staff:profile:update/)
  })

  it('checks disable permission on disable button', () => {
    expect(source).toMatch(/staff:profile:disable/)
  })

  it('checks schedule read permission on schedule button', () => {
    expect(source).toMatch(/staff:schedule:read/)
  })
})

// ─── State Guard ───

describe('Staff state guard', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('uses canDisableStaff for disable button visibility', () => {
    expect(source).toMatch(/canDisableStaff/)
  })
})

// ─── Schedule Conflict ───

describe('Schedule conflict handling', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('imports showConflict or shows error for schedule save failure', () => {
    expect(source).toMatch(/showConflict|showError/)
  })
})

// ─── Skill Blocking ───

describe('Staff skill editing blocked', () => {
  let source: string

  beforeEach(() => {
    source = readFile('../views/staff/index.vue')
  })

  it('does not expose skill editing entry (no skill button in actions)', () => {
    // The skill update API exists but no GET endpoint, so skill editing should be blocked
    // Should NOT have a "技能" or "skill" action button
    expect(source).not.toMatch(/技能|skill.*button|editSkills|openSkillDialog/i)
  })
})

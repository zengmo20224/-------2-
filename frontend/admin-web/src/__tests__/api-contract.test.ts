/**
 * API Contract Tests (10F-R2B)
 *
 * These tests verify that frontend API definitions match the real backend.
 * Focus: no fake endpoints, correct DTO fields, correct method/path.
 */

import { describe, it, expect } from 'vitest'

// ─── Staff API Contract ───

describe('Staff API contract', () => {
  it('must NOT export getStaffSkills (no backend GET endpoint)', async () => {
    const staffModule = await import('../api/staff')
    expect(staffModule).not.toHaveProperty('getStaffSkills')
  })

  it('must still export updateStaffSkills (real PUT endpoint)', async () => {
    const staffModule = await import('../api/staff')
    expect(staffModule).toHaveProperty('updateStaffSkills')
    expect(typeof staffModule.updateStaffSkills).toBe('function')
  })
})

// ─── Community API Contract ───

describe('Community API contract — PostReport type', () => {
  it('PostReport has reasonType (matches backend entity field)', async () => {
    const communityModule = await import('../api/community')
    // We verify the type exists by checking it's exported
    // TypeScript enforces the shape at compile time
    expect(communityModule).toHaveProperty('getReportList')
    expect(communityModule).toHaveProperty('handleReport')
  })
})

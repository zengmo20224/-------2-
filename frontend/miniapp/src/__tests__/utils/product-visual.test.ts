import { describe, expect, it } from 'vitest'
import { getProductVisual } from '@/utils/product-visual'

describe('product visual fallback', () => {
  it('does not request remote demo images in mini-program product cards', () => {
    expect(getProductVisual('5001')).toBe('')
  })

  it('does not invent a visual for unknown products', () => {
    expect(getProductVisual('unknown')).toBe('')
    expect(getProductVisual()).toBe('')
  })
})

import { describe, expect, it } from 'vitest'
import { readFileSync } from 'fs'
import { resolve, dirname } from 'path'
import { fileURLToPath } from 'url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const src = (rel: string) => resolve(__dirname, '..', '..', rel)

const read = (rel: string) => readFileSync(src(rel), 'utf-8')

const productApi = read('api/product.ts')
const requestApi = read('api/request.ts')
const productTypes = read('types/product.ts')
const serviceTypes = read('types/service.ts')
const productsPage = read('pages/products/index.vue')
const productDetailPage = read('pages/products/detail.vue')
const serviceDetailPage = read('pages/services/detail.vue')
const ensureMpWeixinStaticScript = read('../scripts/ensure-mp-weixin-static.mjs')
const packageJson = JSON.parse(read('../package.json'))

describe('catalog gallery and carousel contracts', () => {
  it('product API forwards a keyword query parameter to product listing', () => {
    expect(productApi).toContain('keyword?: string')
    expect(productApi).toContain("'/api/v1/products'")
  })

  it('product types include product top carousel images and intro images separately', () => {
    expect(productTypes).toContain('imageUrls: string[]')
    expect(productTypes).toContain('detailImageUrls: string[]')
  })

  it('service types include detail image URLs', () => {
    expect(serviceTypes).toContain('imageUrls: string[]')
  })

  it('products page renders search controls and sends keyword to product list API', () => {
    expect(productsPage).toContain('searchKeyword')
    expect(productsPage).toContain('搜索商品')
    expect(productsPage).toContain('handleSearch')
    expect(productsPage).toContain('params.keyword')
    expect(productsPage).not.toContain('productCarouselImages')
    expect(productsPage).not.toContain('getProductCarouselImages')
  })

  it('product detail page renders product imageUrls in the top previewable 5-image swiper', () => {
    expect(productDetailPage).toContain('imageUrls')
    expect(productDetailPage).toContain('galleryImages')
    expect(productDetailPage).toContain('PRODUCT_DETAIL_CAROUSEL_LIMIT = 5')
    expect(productDetailPage).toContain('.slice(0, PRODUCT_DETAIL_CAROUSEL_LIMIT)')
    expect(productDetailPage).toContain('<swiper')
    expect(productDetailPage).toContain('indicator-dots')
    expect(productDetailPage).toContain('previewImage')
  })

  it('product detail page renders detailImageUrls in the product intro image grid', () => {
    expect(productDetailPage).toContain('detailImages')
    expect(productDetailPage).toContain('product.value.detailImageUrls')
    expect(productDetailPage).toContain('previewDetailImage')
    expect(productDetailPage).toContain('detail-desc__images')
    expect(productDetailPage).not.toContain('Falls back to coverUrl when imageUrls is empty')
  })

  it('service detail page shows a single cover image on top and imageUrls as a one-per-row details list', () => {
    // Cover stays on top as a single image (no swiper carousel)...
    expect(serviceDetailPage).toContain('coverImage')
    expect(serviceDetailPage).not.toContain('<swiper')
    expect(serviceDetailPage).toContain('previewCover')
    // ...while imageUrls are shown below in a previewable "服务详情" list,
    // one image per row (single column), full width.
    expect(serviceDetailPage).toContain('detailImages')
    expect(serviceDetailPage).toContain('previewDetailImage')
    expect(serviceDetailPage).toContain('服务详情')
    expect(serviceDetailPage).toContain('grid-template-columns: 1fr')
    expect(serviceDetailPage).not.toContain('desc-images--double')
    expect(serviceDetailPage).not.toContain('desc-images--grid3')
  })

  it('mp-weixin build runs the static asset post-build check', () => {
    expect(packageJson.scripts['build:mp-weixin']).toContain('ensure-mp-weixin-static.mjs')
  })

  it('mp-weixin requests use an absolute local backend URL instead of the H5 proxy path', () => {
    expect(requestApi).toContain('MP-WEIXIN')
    expect(requestApi).toContain('VITE_MP_API_BASE_URL')
    expect(requestApi).toContain('VITE_MP_API_BASE_FALLBACK_URLS')
    expect(requestApi).toContain('DEFAULT_MP_API_BASE_URL')
  })

  it('mp-weixin post-build script keeps project.config on an installed base library', () => {
    expect(ensureMpWeixinStaticScript).toContain('WeappVendor')
    expect(ensureMpWeixinStaticScript).toContain('libVersion')
    expect(ensureMpWeixinStaticScript).toContain('project.private.config.json')
  })
})

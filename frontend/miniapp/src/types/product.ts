/**
 * Product-related types for the user-facing H5 app.
 * Backend source: com.petcare.product.dto.ProductSummaryResponse / ProductDetailResponse
 */

/** Product summary as returned by list API */
export interface ProductItem {
  id: string
  categoryId: string
  name: string
  coverUrl: string | null
  price: number
  stock: number
  salesCount: number
  sort: number
}

/** Product detail as returned by detail API */
export interface ProductDetail {
  id: string
  categoryId: string
  categoryName: string | null
  name: string
  coverUrl: string | null
  price: number
  stock: number
  salesCount: number
  description: string | null
  pickupOnly: number
  imageUrls: string[]
}

/** Cart item */
export interface CartItem {
  productId: string
  productName: string
  price: number
  quantity: number
  imageUrl?: string
}

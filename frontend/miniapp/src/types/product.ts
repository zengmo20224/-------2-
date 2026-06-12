/**
 * Product-related types for user-facing miniapp.
 * Backend source: com.petcare.product.dto.*
 */

/** Product item as shown in list/detail */
export interface ProductItem {
  id: string
  name: string
  description?: string
  price: number
  stockQuantity: number
  imageUrl?: string
  status: string
}

/** Cart item */
export interface CartItem {
  productId: string
  productName: string
  price: number
  quantity: number
  imageUrl?: string
}

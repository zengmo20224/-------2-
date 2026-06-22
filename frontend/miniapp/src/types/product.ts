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

/** Product category as returned by category list API */
export interface ProductCategory {
  id: string
  name: string
  iconUrl: string | null
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
  detailImageUrls: string[]
}

/** Cart item as returned by cart API */
export interface CartItem {
  id: string
  productId: string
  productName: string
  productCoverUrl: string | null
  productPrice: number
  productStock: number
  quantity: number
  checked: boolean
  subtotal: number
}

/** Product order as returned by order list API */
export interface OrderItem {
  id: string
  orderNo: string
  totalAmount: number
  deliveryMethod: string
  addressSnapshot: string | null
  paymentMethod: string | null
  paymentStatus: string
  pickupStatus: string
  status: string
  contactName: string
  contactPhone: string
  remark: string | null
  createTime: string
}

/** Order detail with line items */
export interface OrderDetail {
  id: string
  orderNo: string
  totalAmount: number
  deliveryMethod: string
  addressSnapshot: string | null
  paymentMethod: string | null
  paymentStatus: string
  pickupStatus: string
  status: string
  contactName: string
  contactPhone: string
  remark: string | null
  merchantRemark: string | null
  createTime: string
  items: OrderLineItem[]
}

/** Order line item snapshot */
export interface OrderLineItem {
  id: string
  productId: string
  productName: string
  productCoverUrl: string | null
  price: number
  quantity: number
  totalAmount: number
}

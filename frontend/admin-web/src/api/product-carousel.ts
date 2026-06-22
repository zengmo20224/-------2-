import request from '../utils/request'

export interface ProductCarouselImage {
  id: number
  title: string | null
  imageUrl: string
  linkType: string | null
  linkTargetId: number | null
  status: string
  sort: number | null
}

export interface ProductCarouselImageParams {
  title?: string | null
  imageUrl: string
  linkType?: string | null
  linkTargetId?: number | null
  status?: string
  sort?: number
}

export interface ProductCarouselImagesUpdateParams {
  images: ProductCarouselImageParams[]
}

export const listProductCarouselImages = () => {
  return request.get<ProductCarouselImage[]>('/v1/admin/product-carousel-images')
}

export const replaceProductCarouselImages = (data: ProductCarouselImagesUpdateParams) => {
  return request.put<ProductCarouselImage[]>('/v1/admin/product-carousel-images', data)
}

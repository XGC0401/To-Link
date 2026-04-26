export interface Post {
  id: number
  type: number
  user?: PostUser
  title: string
  content: string
  request_type: number
  custom_category?: string
  tags?: string[]
  // likeUsers?: Array<PostUser>
  share_count: number
  acceptUser?: PostUser | null
  paymentMethod?: number | null
  is_important: boolean
  redeemPoints: number
  postPhotos?: Array<postPhoto> | null
  startTime?: Date
  endTime?: Date
  createTime?: Date
}

export interface PostRequest {
  type: number
  title: string
  content: string
  request_type: number
  custom_category?: string
  tags?: string[]
  paymentMethod?: number
  is_important: boolean
  redeemPoints: number
}

export interface postPhoto {
  id: string
  url: string
  createTime: Date
}

export interface PostUser {
  uuid: string
  username: string
  email?: string
}
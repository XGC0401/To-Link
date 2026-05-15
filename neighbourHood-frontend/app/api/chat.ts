import axios from 'axios'
import type { BasicResponse } from '~/api/types/common'
import { Storage } from '../utils/storage'

const createAxiosInstance = () => {
  const config = useRuntimeConfig()
  const rawBaseUrl = String(config.public.apiBaseUrl || '').replace(/\/$/, '')
  const baseURL = rawBaseUrl.endsWith('/api') ? rawBaseUrl : `${rawBaseUrl}/api`
  return axios.create({
    baseURL,
    timeout: 15000
  })
}

const isValidJwt = (token: string) => token.split('.').length === 3

const getToken = () => {
  const token = Storage.get('token')
  if (token && token !== 'null' && token !== 'undefined') {
    if (isValidJwt(token)) {
      return token
    }
    Storage.remove('token')
  }

  const legacyToken = Storage.get('userToken')
  if (legacyToken && legacyToken !== 'null' && legacyToken !== 'undefined') {
    if (isValidJwt(legacyToken)) {
      Storage.set('token', legacyToken)
      return legacyToken
    }
    Storage.remove('userToken')
  }

  return null
}

const getConfig = () => {
  const token = getToken()
  if (!token) {
    throw new Error('No valid authentication token')
  }
  return {
    headers: {
      Authorization: `Bearer ${token}`
    }
  }
}

export type APIResponse<T> = [null, T, Options?] | [Error, Options?]

export type Options = { headers?: Record<string, any>; code?: number; message?: String }

export interface ChatConversationSummary {
  conversationId: number
  peerEmail: string
  peerName: string
  lastMessage: string
  lastMessageType: string
  lastMessageTime: string
  unreadCount: number
}

export interface ChatMessagePayload {
  id: number
  conversationId: number
  senderEmail: string
  senderName: string
  content: string
  messageType: string
  attachmentUrl?: string
  sentAt: string
  read: boolean
}

export interface PresencePayload {
  email: string
  status: 'active' | 'online' | 'inactive' | 'busy' | 'offline'
  lastActiveAt?: string | null
}

export async function getChatConversations(): Promise<APIResponse<BasicResponse<ChatConversationSummary[]>>> {
  try {
    const { data, headers } = await createAxiosInstance().get<BasicResponse<ChatConversationSummary[]>>('/chat/conversations', getConfig())
    return [null, data, { headers }]
  } catch (error: any) {
    console.error(error)
    return [error, error.response?.status]
  }
}

export async function getChatMessages(peerEmail: string, sinceId?: number): Promise<APIResponse<BasicResponse<ChatMessagePayload[]>>> {
  try {
    const { data, headers } = await createAxiosInstance().get<BasicResponse<ChatMessagePayload[]>>(
      `/chat/messages/${encodeURIComponent(peerEmail)}`,
      {
        ...getConfig(),
        params: sinceId ? { sinceId } : {}
      }
    )
    return [null, data, { headers }]
  } catch (error: any) {
    console.error(error)
    return [error, error.response?.status]
  }
}

export async function sendChatMessage(peerEmail: string, content: string, messageType = 'normal'):
  Promise<APIResponse<BasicResponse<ChatMessagePayload>>> {
  try {
    const { data, headers } = await createAxiosInstance().post<BasicResponse<ChatMessagePayload>>(
      `/chat/messages/${encodeURIComponent(peerEmail)}`,
      { content, messageType },
      getConfig()
    )
    return [null, data, { headers }]
  } catch (error: any) {
    console.error(error)
    return [error, error.response?.status]
  }
}

export async function markConversationRead(peerEmail: string): Promise<APIResponse<BasicResponse<number>>> {
  try {
    const { data, headers } = await createAxiosInstance().post<BasicResponse<number>>(
      `/chat/read/${encodeURIComponent(peerEmail)}`,
      {},
      getConfig()
    )
    return [null, data, { headers }]
  } catch (error: any) {
    console.error(error)
    return [error, error.response?.status]
  }
}

export async function updateMyPresence(status: string): Promise<APIResponse<BasicResponse<PresencePayload>>> {
  try {
    const { data, headers } = await createAxiosInstance().put<BasicResponse<PresencePayload>>(
      '/presence/me',
      { status },
      getConfig()
    )
    return [null, data, { headers }]
  } catch (error: any) {
    console.error(error)
    return [error, error.response?.status]
  }
}

export async function getPresenceByEmails(emails: string[]): Promise<APIResponse<BasicResponse<PresencePayload[]>>> {
  try {
    const { data, headers } = await createAxiosInstance().post<BasicResponse<PresencePayload[]>>(
      '/presence/by-emails',
      { emails },
      getConfig()
    )
    return [null, data, { headers }]
  } catch (error: any) {
    console.error(error)
    return [error, error.response?.status]
  }
}

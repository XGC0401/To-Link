import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { ref, Ref } from 'vue';

interface WebSocketMessage {
  id: number;
  conversationId: number;
  senderEmail: string;
  senderName: string;
  content: string;
  messageType: string;
  attachmentUrl?: string;
  sentAt: string;
  read: boolean;
}

let stompClient: Client | null = null;
let isConnected: Ref<boolean> = ref(false);
const RECONNECT_DELAY = 3000;

// Callbacks for different message types
const messageCallbacks: ((msg: WebSocketMessage) => void)[] = [];
const conversationCallbacks: ((msg: any) => void)[] = [];
const errorCallbacks: ((msg: string) => void)[] = [];
const connectionStateCallbacks: ((connected: boolean) => void)[] = [];

export function useWebSocket() {
  const resolveSocketEndpoints = () => {
    const config = useRuntimeConfig();
    const apiBaseUrl = String(config.public.apiBaseUrl || '').trim();

    if (apiBaseUrl) {
      const withoutApiSuffix = apiBaseUrl.replace(/\/api\/?$/, '');
      return {
        nativeWsUrl: withoutApiSuffix.replace(/^http/, 'ws') + '/ws/chat',
        sockJsHttpUrl: withoutApiSuffix.replace(/^ws/, 'http') + '/ws/chat'
      };
    }

    if (typeof window !== 'undefined') {
      const wsProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
      const httpProtocol = window.location.protocol;
      return {
        nativeWsUrl: `${wsProtocol}//${window.location.host}/ws/chat`,
        sockJsHttpUrl: `${httpProtocol}//${window.location.host}/ws/chat`
      };
    }

    return {
      nativeWsUrl: 'ws://localhost:8080/ws/chat',
      sockJsHttpUrl: 'http://localhost:8080/ws/chat'
    };
  }

  const connectStomp = (token: string, useSockJs: boolean, nativeWsUrl: string, sockJsHttpUrl: string) => {
    return new Promise<void>((resolve, reject) => {
      let settled = false;

      stompClient = new Client();
      stompClient.configure({
        brokerURL: useSockJs ? undefined : nativeWsUrl,
        webSocketFactory: useSockJs
          ? () => new SockJS(sockJsHttpUrl) as unknown as WebSocket
          : undefined,
        login: 'user',
        passcode: token,
        reconnectDelay: RECONNECT_DELAY,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
        onConnect: () => {
          isConnected.value = true;
          connectionStateCallbacks.forEach(cb => cb(true));

          if (stompClient) {
            stompClient.subscribe('/user/queue/messages', (message: IMessage) => {
              try {
                const msg = JSON.parse(message.body);
                messageCallbacks.forEach(cb => cb(msg));
              } catch (e) {
                console.error('Failed to parse message:', e);
              }
            });

            stompClient.subscribe('/user/queue/conversations', (message: IMessage) => {
              try {
                const msg = JSON.parse(message.body);
                conversationCallbacks.forEach(cb => cb(msg));
              } catch (e) {
                console.error('Failed to parse conversation update:', e);
              }
            });

            stompClient.subscribe('/user/queue/errors', (message: IMessage) => {
              errorCallbacks.forEach(cb => cb(message.body));
            });
          }

          if (!settled) {
            settled = true;
            resolve();
          }
        },
        onStompError: (frame) => {
          console.error('WebSocket STOMP error:', frame);
          isConnected.value = false;
          connectionStateCallbacks.forEach(cb => cb(false));
          if (!settled) {
            settled = true;
            reject(new Error('WebSocket STOMP connection failed'));
          }
        },
        onWebSocketError: (event) => {
          console.error('WebSocket connection error:', event);
          isConnected.value = false;
          connectionStateCallbacks.forEach(cb => cb(false));
          if (!settled) {
            settled = true;
            reject(new Error('WebSocket transport connection failed'));
          }
        },
        onWebSocketClose: () => {
          isConnected.value = false;
          connectionStateCallbacks.forEach(cb => cb(false));
          if (!settled) {
            settled = true;
            reject(new Error('WebSocket transport closed before connect'));
          }
        }
      });

      stompClient.activate();
    });
  }

  /**
   * Initialize WebSocket connection to the backend
   */
  async function initWebSocket() {
    if (stompClient && isConnected.value) {
      return; // Already connected
    }

    try {
      const token = localStorage.getItem('token');
      if (!token) {
        console.warn('No token found, cannot connect to WebSocket');
        throw new Error('No authentication token');
      }

      const { nativeWsUrl, sockJsHttpUrl } = resolveSocketEndpoints();

      try {
        await connectStomp(token, false, nativeWsUrl, sockJsHttpUrl);
        console.log('WebSocket connected using native transport');
      } catch (nativeError) {
        console.warn('Native WebSocket failed, switching to SockJS fallback:', nativeError);
        if (stompClient) {
          try {
            stompClient.deactivate();
          } catch {
            // Ignore deactivate errors during fallback switch.
          }
          stompClient = null;
        }
        await connectStomp(token, true, nativeWsUrl, sockJsHttpUrl);
        console.log('WebSocket connected using SockJS fallback');
      }
    } catch (error) {
      console.error('Failed to initialize WebSocket:', error);
      isConnected.value = false;
      connectionStateCallbacks.forEach(cb => cb(false));
      throw error;
    }
  }

  /**
   * Send a chat message over WebSocket
   */
  async function sendMessageViaWebSocket(
    peerEmail: string,
    content: string,
    messageType: string = 'text',
    attachmentUrl?: string
  ) {
    if (!stompClient || !isConnected.value) {
      throw new Error('WebSocket not connected');
    }

    const message = {
      peerEmail,
      content,
      messageType,
      attachmentUrl
    };

    try {
      stompClient.publish({
        destination: '/app/sendMessage',
        body: JSON.stringify(message)
      });
    } catch (error) {
      console.error('Failed to send message via WebSocket:', error);
      throw error;
    }
  }

  /**
   * Register callback for incoming messages
   */
  function onMessage(callback: (msg: WebSocketMessage) => void) {
    messageCallbacks.push(callback);
    return () => {
      const index = messageCallbacks.indexOf(callback);
      if (index > -1) messageCallbacks.splice(index, 1);
    };
  }

  /**
   * Register callback for conversation updates
   */
  function onConversationUpdate(callback: (msg: any) => void) {
    conversationCallbacks.push(callback);
    return () => {
      const index = conversationCallbacks.indexOf(callback);
      if (index > -1) conversationCallbacks.splice(index, 1);
    };
  }

  /**
   * Register callback for errors
   */
  function onError(callback: (msg: string) => void) {
    errorCallbacks.push(callback);
    return () => {
      const index = errorCallbacks.indexOf(callback);
      if (index > -1) errorCallbacks.splice(index, 1);
    };
  }

  /**
   * Register callback for connection state changes
   */
  function onConnectionStateChange(callback: (connected: boolean) => void) {
    connectionStateCallbacks.push(callback);
    return () => {
      const index = connectionStateCallbacks.indexOf(callback);
      if (index > -1) connectionStateCallbacks.splice(index, 1);
    };
  }

  /**
   * Disconnect from WebSocket
   */
  function disconnect() {
    if (stompClient) {
      stompClient.deactivate();
      stompClient = null;
      isConnected.value = false;
      connectionStateCallbacks.forEach(cb => cb(false));
    }
  }

  /**
   * Check if connected
   */
  function isWebSocketConnected() {
    return isConnected.value;
  }

  return {
    initWebSocket,
    sendMessageViaWebSocket,
    onMessage,
    onConversationUpdate,
    onError,
    onConnectionStateChange,
    disconnect,
    isWebSocketConnected,
    isConnected
  };
}

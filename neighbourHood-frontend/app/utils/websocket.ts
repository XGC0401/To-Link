import { Client, IMessage } from '@stomp/stompjs';
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
let connectionAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 5;
const RECONNECT_DELAY = 3000;

// Callbacks for different message types
const messageCallbacks: ((msg: WebSocketMessage) => void)[] = [];
const conversationCallbacks: ((msg: any) => void)[] = [];
const errorCallbacks: ((msg: string) => void)[] = [];
const connectionStateCallbacks: ((connected: boolean) => void)[] = [];

export function useWebSocket() {
  /**
   * Initialize WebSocket connection to the backend
   */
  async function initWebSocket() {
    if (stompClient && isConnected.value) {
      return; // Already connected
    }

    return new Promise<void>((resolve, reject) => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          console.warn('No token found, cannot connect to WebSocket');
          reject(new Error('No authentication token'));
          return;
        }

        stompClient = new Client();

        // Get the backend URL
        const config = useRuntimeConfig();
        const backendUrl = config.public.backendUrl || 'http://localhost:8080';
        const wsUrl = backendUrl.replace(/^http/, 'ws') + '/ws/chat';

        stompClient.configure({
          brokerURL: wsUrl,
          login: 'user',
          passcode: token,
          reconnectDelay: RECONNECT_DELAY,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
          onConnect: () => {
            console.log('WebSocket connected');
            isConnected.value = true;
            connectionAttempts = 0;
            connectionStateCallbacks.forEach(cb => cb(true));

            // Subscribe to message queue
            if (stompClient) {
              stompClient.subscribe('/user/queue/messages', (message: IMessage) => {
                try {
                  const msg = JSON.parse(message.body);
                  messageCallbacks.forEach(cb => cb(msg));
                } catch (e) {
                  console.error('Failed to parse message:', e);
                }
              });

              // Subscribe to conversation updates
              stompClient.subscribe('/user/queue/conversations', (message: IMessage) => {
                try {
                  const msg = JSON.parse(message.body);
                  conversationCallbacks.forEach(cb => cb(msg));
                } catch (e) {
                  console.error('Failed to parse conversation update:', e);
                }
              });

              // Subscribe to errors
              stompClient.subscribe('/user/queue/errors', (message: IMessage) => {
                errorCallbacks.forEach(cb => cb(message.body));
              });
            }

            resolve();
          },
          onStompError: (frame) => {
            console.error('WebSocket error:', frame);
            isConnected.value = false;
            connectionStateCallbacks.forEach(cb => cb(false));
            attemptReconnect();
            reject(new Error('WebSocket connection failed'));
          },
          onWebSocketError: (event) => {
            console.error('WebSocket connection error:', event);
            isConnected.value = false;
            connectionStateCallbacks.forEach(cb => cb(false));
            attemptReconnect();
          }
        });

        stompClient.activate();
      } catch (error) {
        console.error('Failed to initialize WebSocket:', error);
        isConnected.value = false;
        connectionStateCallbacks.forEach(cb => cb(false));
        reject(error);
      }
    });
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
   * Attempt to reconnect
   */
  function attemptReconnect() {
    if (connectionAttempts < MAX_RECONNECT_ATTEMPTS) {
      connectionAttempts++;
      console.log(`Attempting to reconnect (${connectionAttempts}/${MAX_RECONNECT_ATTEMPTS})...`);
      setTimeout(() => {
        initWebSocket().catch(err => {
          console.error('Reconnection failed:', err);
        });
      }, RECONNECT_DELAY * connectionAttempts);
    } else {
      console.error('Max reconnection attempts reached');
    }
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

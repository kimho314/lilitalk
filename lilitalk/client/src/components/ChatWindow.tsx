import React, { useState, useEffect, useRef, useCallback } from 'react';
import { ChatRoom, Message, User, WebSocketMessage } from '../types/index';
import { messageApi } from '../services/api.ts';
import { useWebSocket } from '../hooks/useWebSocket.ts';

interface ChatWindowProps {
  currentUser: User;
  chatRoom: ChatRoom;
  onError: (error: string) => void;
}

export const ChatWindow: React.FC<ChatWindowProps> = ({
  currentUser,
  chatRoom,
  onError,
}) => {
  const [messages, setMessages] = useState<Message[]>([]);
  const [messageInput, setMessageInput] = useState('');
  const [isLoadingMessages, setIsLoadingMessages] = useState(false);
  const [isTyping, setIsTyping] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  // WebSocket 연결
  const {
    isConnected,
    lastMessage,
    sendMessage: sendWebSocketMessage,
    error: wsError,
  } = useWebSocket({
    userId: currentUser.id,
    onConnect: () => console.log('🔌 WebSocket 연결됨'),
    onDisconnect: () => console.log('🔌 WebSocket 연결 해제됨'),
    onError: (error) => console.error('🔌 WebSocket 에러:', error),
  });
  
  // WebSocket 메시지 도착 시 처리
  useEffect(() => {
    console.log('--- 📬 WebSocket 메시지 수신 ---', lastMessage);
    if (!lastMessage) return;

    // 'lastMessage'는 서버로부터 받은 다양한 타입의 메시지일 수 있으므로 any로 처리
    const wsMessage: any = lastMessage;

    console.log(`[디버그] 현재 방 ID: ${chatRoom.id} | 메시지 방 ID: ${wsMessage.chatRoomId}`);
    // 이 메시지가 현재 열려있는 채팅방의 메시지인지 확인
    if (wsMessage.chatRoomId && wsMessage.chatRoomId !== chatRoom.id) {
      console.log('[디버그] 다른 방의 메시지이므로 UI를 업데이트하지 않습니다.');
      return;
    }

    // CHAT_MESSAGE 타입 처리 (서버에서 온 ChatMessage DTO는 이런 필드들을 가짐)
    if (wsMessage && typeof wsMessage.content === 'string' && wsMessage.senderId) {
      console.log('[디버그] 채팅 메시지 타입 확인. UI 상태 업데이트를 준비합니다.');
      setMessages(prev => {
        console.log(`[디버그] setMessages 실행. 이전 메시지 개수: ${prev.length}`);
        // 이미 메시지 목록에 있는지 확인 (중복 추가 방지)
        if (prev.some(msg => msg.id === wsMessage.id)) {
          console.log(`[디버그] 중복 메시지(ID: ${wsMessage.id})이므로 추가하지 않습니다.`);
          return prev;
        }

        const newMessage: Message = {
          id: wsMessage.id,
          chatRoomId: wsMessage.chatRoomId,
          sender: {
            id: wsMessage.senderId,
            username: wsMessage.senderName,
            displayName: wsMessage.senderName,
            isActive: true,
            createdAt: new Date().toISOString(),
          },
          type: wsMessage.type,
          content: wsMessage.content,
          sequenceNumber: wsMessage.sequenceNumber,
          isEdited: false,
          isDeleted: false,
          createdAt: new Date(wsMessage.timestamp).toISOString(),
        };
        console.log(`[디버그] 새 메시지 추가 완료. 새로운 메시지 개수: ${prev.length + 1}`);
        return [...prev, newMessage];
      });
    } 
    // TYPING_INDICATOR 타입 처리
    else if (wsMessage.type === 'TYPING_INDICATOR') {
      if (wsMessage.userId !== currentUser.id) {
        setIsTyping(wsMessage.isTyping || false);
        if (wsMessage.isTyping) {
          setTimeout(() => setIsTyping(false), 3000);
        }
      }
    } 
    // ERROR 타입 처리
    else if (wsMessage.type === 'ERROR') {
      console.error('WebSocket 에러 메시지:', wsMessage);
      onError((wsMessage as any).message || 'WebSocket 에러가 발생했습니다.');
    }
  }, [lastMessage, chatRoom.id, currentUser.id, onError]);

  // 메시지 목록 로드
  const loadMessages = useCallback(async () => {
    setIsLoadingMessages(true);
    try {
      const response = await messageApi.getMessages(chatRoom.id, currentUser.id, 0, 50);
      // 메시지를 시간순으로 정렬 (oldest first)
      const sortedMessages = response.content.sort((a, b) => 
        new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
      );
      setMessages(sortedMessages);
    } catch (error: any) {
      onError(error.response?.data?.message || '메시지를 불러올 수 없습니다.');
    } finally {
      setIsLoadingMessages(false);
    }
  }, [chatRoom.id, currentUser.id, onError]);

  // 메시지 전송
  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault();
    const content = messageInput.trim();
    
    if (!content || !isConnected) {
      return;
    }

    try {
      const wsMessage: WebSocketMessage = {
        type: 'SEND_MESSAGE',
        chatRoomId: chatRoom.id,
        messageType: 'TEXT',
        content,
      };

      sendWebSocketMessage(wsMessage);
      setMessageInput('');

    } catch (error) {
      console.error('📤 메시지 전송 에러:', error);
      onError('메시지 전송에 실패했습니다. 다시 시도해주세요.');
    }
  };

  // 채팅방 변경 시 메시지 로드
  useEffect(() => {
    loadMessages();
  }, [chatRoom.id, loadMessages]);

  // 새 메시지 시 스크롤
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // WebSocket 에러 처리
  useEffect(() => {
    if (wsError) {
      onError(wsError);
    }
  }, [wsError, onError]);

  const containerStyle: React.CSSProperties = {
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    backgroundColor: 'white',
  };

  const headerStyle: React.CSSProperties = {
    padding: '1rem',
    borderBottom: '1px solid #e1e8ed',
    backgroundColor: '#f8f9fa',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'space-between',
  };

  const messagesStyle: React.CSSProperties = {
    flex: 1,
    overflow: 'auto',
    padding: '1rem',
    display: 'flex',
    flexDirection: 'column',
    gap: '12px',
  };

  const messageStyle = (isOwn: boolean): React.CSSProperties => ({
    maxWidth: '70%',
    padding: '12px 16px',
    borderRadius: '18px',
    marginLeft: isOwn ? 'auto' : '0',
    marginRight: isOwn ? '0' : 'auto',
    backgroundColor: isOwn ? '#667eea' : '#f1f3f4',
    color: isOwn ? 'white' : '#333',
    wordBreak: 'break-word',
  });

  const inputAreaStyle: React.CSSProperties = {
    padding: '1rem',
    borderTop: '1px solid #e1e8ed',
    backgroundColor: '#f8f9fa',
  };

  const inputContainerStyle: React.CSSProperties = {
    display: 'flex',
    gap: '8px',
    alignItems: 'flex-end',
  };

  const inputStyle: React.CSSProperties = {
    flex: 1,
    padding: '12px 16px',
    border: '1px solid #e1e8ed',
    borderRadius: '24px',
    fontSize: '14px',
    outline: 'none',
    resize: 'none',
    minHeight: '20px',
    maxHeight: '100px',
  };

  const sendButtonStyle: React.CSSProperties = {
    padding: '12px 20px',
    backgroundColor: isConnected ? '#667eea' : '#ccc',
    color: 'white',
    border: 'none',
    borderRadius: '24px',
    cursor: isConnected ? 'pointer' : 'not-allowed',
    fontSize: '14px',
    fontWeight: 'bold',
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  };

  return (
    <div style={containerStyle}>
      {/* 헤더 */}
      <div style={headerStyle}>
        <div>
          <h3 style={{ margin: 0, fontSize: '18px', color: '#333' }}>
            {chatRoom.type === 'GROUP' ? '👥' : '📢'} {chatRoom.name}
          </h3>
          <div style={{ fontSize: '12px', color: '#666', marginTop: '4px' }}>
            멤버 {chatRoom.memberCount}명 • ID: {chatRoom.id}
            {chatRoom.description && ` • ${chatRoom.description}`}
          </div>
        </div>
      </div>

      {/* 메시지 영역 */}
      <div style={messagesStyle}>
        {isLoadingMessages ? (
          <div style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
            메시지를 불러오는 중...
          </div>
        ) : messages.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '2rem', color: '#666' }}>
            첫 번째 메시지를 보내보세요! 👋
          </div>
        ) : (
          messages.map((message) => {
            const isOwn = message.sender?.id === currentUser.id;
            return (
              <div key={message.id} style={{ display: 'flex', flexDirection: 'column' }}>
                {!isOwn && (
                  <div style={{ fontSize: '12px', color: '#666', marginBottom: '4px', marginLeft: '4px' }}>
                    {message.sender?.displayName || message.sender?.username}
                  </div>
                )}
                <div style={messageStyle(isOwn)}>
                  <div>{message.content}</div>
                  <div style={{ 
                    fontSize: '11px', 
                    opacity: 0.7, 
                    marginTop: '4px',
                    textAlign: 'right'
                  }}>
                    {formatTime(message.createdAt)}
                  </div>
                </div>
              </div>
            );
          })
        )}
        
        {/* 타이핑 인디케이터 */}
        {isTyping && (
          <div style={{ fontSize: '12px', color: '#666', fontStyle: 'italic', padding: '8px' }}>
            누군가가 입력 중입니다...
          </div>
        )}
        
        <div ref={messagesEndRef} />
      </div>

      {/* 입력 영역 */}
      <div style={inputAreaStyle}>
        <form onSubmit={handleSendMessage}>
          <div style={inputContainerStyle}>
            <textarea
              value={messageInput}
              onChange={(e) => {
                setMessageInput(e.target.value);
                // handleTyping(); // 타이핑 인디케이터 제거
              }}
              onKeyPress={(e) => {
                if (e.key === 'Enter' && !e.shiftKey) {
                  e.preventDefault();
                  handleSendMessage(e);
                }
              }}
              placeholder={isConnected ? "메시지를 입력하세요..." : "연결 중..."}
              disabled={!isConnected}
              style={inputStyle}
              rows={1}
            />
            <button
              type="submit"
              disabled={!isConnected || !messageInput.trim()}
              style={sendButtonStyle}
            >
              전송
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}; 
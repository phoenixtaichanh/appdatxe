import { useState, useEffect, useRef } from 'react';
import { supportAPI } from '../services/api';
import { MessageCircle, Send, Phone, Mail, Clock, CheckCircle, AlertCircle, User } from 'lucide-react';

const STATUS_COLORS = {
  waiting: 'bg-yellow-100 text-yellow-700',
  active: 'bg-blue-100 text-blue-700',
  resolved: 'bg-green-100 text-green-700',
  closed: 'bg-gray-100 text-gray-600',
  escalated: 'bg-red-100 text-red-700',
};

const STATUS_LABELS = {
  waiting: 'Đang chờ',
  active: 'Đang trò chuyện',
  resolved: 'Đã giải quyết',
  closed: 'Đã đóng',
  escalated: 'Escalated',
};

function formatTime(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
}

function formatDate(dateStr) {
  if (!dateStr) return '';
  const d = new Date(dateStr);
  return d.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
}

export default function Chat() {
  const [conversations, setConversations] = useState([]);
  const [selectedId, setSelectedId] = useState(null);
  const [messages, setMessages] = useState([]);
  const [replyText, setReplyText] = useState('');
  const [loading, setLoading] = useState(true);
  const [loadingMessages, setLoadingMessages] = useState(false);
  const [sending, setSending] = useState(false);
  const [filterStatus, setFilterStatus] = useState('all');
  const [stats, setStats] = useState({ total: 0, waiting: 0, active: 0, resolved: 0 });
  const messagesEndRef = useRef(null);

  const fetchConversations = async () => {
    try {
      const res = await supportAPI.conversations({});
      const data = res.data.data || [];
      setConversations(data);
      setStats({
        total: data.length,
        waiting: data.filter(c => c.status === 'waiting').length,
        active: data.filter(c => c.status === 'active').length,
        resolved: data.filter(c => c.status === 'resolved' || c.status === 'closed').length,
      });
    } catch (err) {
      console.error('Failed to load conversations:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchConversations();
    // Poll every 5 seconds for new messages
    const interval = setInterval(fetchConversations, 5000);
    return () => clearInterval(interval);
  }, []);

  const fetchMessages = async (conversationId) => {
    setLoadingMessages(true);
    try {
      const res = await supportAPI.messages(conversationId);
      setMessages(res.data.data || []);
      // Mark as read
      await supportAPI.conversation(conversationId);
      fetchConversations();
    } catch (err) {
      console.error('Failed to load messages:', err);
    } finally {
      setLoadingMessages(false);
    }
  };

  useEffect(() => {
    if (selectedId) {
      fetchMessages(selectedId);
      const interval = setInterval(() => fetchMessages(selectedId), 3000);
      return () => clearInterval(interval);
    }
  }, [selectedId]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const handleSelect = (conv) => {
    setSelectedId(conv.id);
    if (conv.unread_count > 0) {
      fetchConversations();
    }
  };

  const handleSend = async () => {
    if (!replyText.trim() || !selectedId || sending) return;
    setSending(true);
    try {
      await supportAPI.reply(selectedId, replyText.trim());
      setReplyText('');
      fetchMessages(selectedId);
      fetchConversations();
    } catch (err) {
      console.error('Failed to send reply:', err);
    } finally {
      setSending(false);
    }
  };

  const handleResolve = async (id) => {
    try {
      await supportAPI.resolve(id);
      fetchConversations();
      if (selectedId === id) fetchMessages(id);
    } catch (err) {
      console.error('Failed to resolve:', err);
    }
  };

  const filteredConversations = filterStatus === 'all'
    ? conversations
    : conversations.filter(c => c.status === filterStatus);

  const selectedConv = conversations.find(c => c.id === selectedId);

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-800">Hỗ trợ khách hàng</h1>
          <p className="text-sm text-gray-500 mt-0.5">Trò chuyện realtime với khách hàng</p>
        </div>
        {/* Stats */}
        <div className="flex gap-3">
          {[
            { label: 'Chờ', count: stats.waiting, color: 'text-yellow-600', bg: 'bg-yellow-50' },
            { label: 'Đang chat', count: stats.active, color: 'text-blue-600', bg: 'bg-blue-50' },
            { label: 'Đã xong', count: stats.resolved, color: 'text-green-600', bg: 'bg-green-50' },
          ].map(s => (
            <div key={s.label} className={`px-4 py-2 rounded-xl ${s.bg}`}>
              <p className={`text-xl font-bold ${s.color}`}>{s.count}</p>
              <p className="text-xs text-gray-500">{s.label}</p>
            </div>
          ))}
        </div>
      </div>

      {/* Filter tabs */}
      <div className="flex gap-2 mb-4">
        {['all', 'waiting', 'active', 'resolved', 'closed'].map(status => (
          <button
            key={status}
            onClick={() => setFilterStatus(status)}
            className={`px-4 py-1.5 rounded-full text-sm font-medium transition-colors ${
              filterStatus === status
                ? 'bg-blue-600 text-white'
                : 'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'
            }`}
          >
            {status === 'all' ? 'Tất cả' : STATUS_LABELS[status] || status}
          </button>
        ))}
      </div>

      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden" style={{ height: 'calc(100vh - 260px)' }}>
        <div className="flex h-full">
          {/* Conversation list */}
          <div className="w-80 border-r border-gray-100 flex flex-col">
            <div className="p-4 border-b border-gray-100">
              <h3 className="font-semibold text-gray-800">Cuộc trò chuyện ({filteredConversations.length})</h3>
            </div>
            <div className="flex-1 overflow-y-auto">
              {loading ? (
                <div className="flex items-center justify-center h-32">
                  <div className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" />
                </div>
              ) : filteredConversations.length === 0 ? (
                <div className="flex flex-col items-center justify-center h-32 text-gray-400">
                  <MessageCircle size={32} className="mb-2" />
                  <p className="text-sm">Không có cuộc trò chuyện</p>
                </div>
              ) : (
                filteredConversations.map(conv => (
                  <div
                    key={conv.id}
                    onClick={() => handleSelect(conv)}
                    className={`px-4 py-3 border-b border-gray-50 cursor-pointer transition-colors ${
                      selectedId === conv.id ? 'bg-blue-50' : 'hover:bg-gray-50'
                    } ${conv.unread_count > 0 ? 'bg-blue-50/50' : ''}`}
                  >
                    <div className="flex items-start gap-3">
                      <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center flex-shrink-0">
                        <User size={18} className="text-blue-600" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center justify-between">
                          <p className="font-medium text-gray-800 text-sm truncate">{conv.customer_name || 'Khách hàng'}</p>
                          <div className="flex items-center gap-1">
                            {conv.unread_count > 0 && (
                              <span className="bg-blue-600 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                                {conv.unread_count}
                              </span>
                            )}
                            <span className={`px-1.5 py-0.5 rounded text-xs font-medium ${STATUS_COLORS[conv.status] || 'bg-gray-100 text-gray-600'}`}>
                              {STATUS_LABELS[conv.status] || conv.status}
                            </span>
                          </div>
                        </div>
                        <p className="text-xs text-gray-500 truncate mt-0.5">{conv.last_message || conv.subject || 'Cuộc trò chuyện mới'}</p>
                        <div className="flex items-center gap-2 mt-1">
                          <span className="text-xs text-gray-400 flex items-center gap-1">
                            <Clock size={10} /> {formatTime(conv.last_message_at || conv.created_at)}
                          </span>
                          {conv.category && (
                            <span className="text-xs text-gray-400">• {conv.category}</span>
                          )}
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Chat area */}
          {selectedId ? (
            <div className="flex-1 flex flex-col">
              {/* Header */}
              <div className="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-gray-800">{selectedConv?.customer_name || 'Khách hàng'}</h3>
                  <div className="flex items-center gap-3 mt-0.5">
                    <span className="text-xs text-gray-500 flex items-center gap-1">
                      <Mail size={12} /> {selectedConv?.customer_email || ''}
                    </span>
                    <span className={`px-2 py-0.5 rounded text-xs font-medium ${STATUS_COLORS[selectedConv?.status] || 'bg-gray-100 text-gray-600'}`}>
                      {STATUS_LABELS[selectedConv?.status] || selectedConv?.status}
                    </span>
                    {selectedConv?.subject && (
                      <span className="text-xs text-gray-400">• {selectedConv.subject}</span>
                    )}
                  </div>
                </div>
                <div className="flex items-center gap-2">
                  {selectedConv?.status !== 'resolved' && selectedConv?.status !== 'closed' && (
                    <button
                      onClick={() => handleResolve(selectedId)}
                      className="flex items-center gap-1.5 px-3 py-1.5 bg-green-600 text-white rounded-lg text-sm hover:bg-green-700 transition-colors"
                    >
                      <CheckCircle size={14} /> Đánh dấu xong
                    </button>
                  )}
                </div>
              </div>

              {/* Messages */}
              <div className="flex-1 overflow-y-auto p-6 space-y-4">
                {loadingMessages ? (
                  <div className="flex items-center justify-center h-full">
                    <div className="animate-spin w-6 h-6 border-2 border-blue-600 border-t-transparent rounded-full" />
                  </div>
                ) : messages.length === 0 ? (
                  <div className="flex flex-col items-center justify-center h-full text-gray-400">
                    <MessageCircle size={40} className="mb-3" />
                    <p>Chưa có tin nhắn nào</p>
                  </div>
                ) : (
                  messages.map(msg => {
                    const isConsultant = msg.sender_type === 'consultant' || msg.sender_type === 'admin';
                    return (
                      <div key={msg.id} className={`flex ${isConsultant ? 'justify-end' : 'justify-start'}`}>
                        <div className={`max-w-[70%] ${isConsultant ? 'order-2' : 'order-1'}`}>
                          <div className={`rounded-2xl px-4 py-3 ${
                            isConsultant
                              ? 'bg-blue-600 text-white rounded-br-md'
                              : 'bg-gray-100 text-gray-800 rounded-bl-md'
                          }`}>
                            <p className="text-sm whitespace-pre-wrap">{msg.message}</p>
                          </div>
                          <p className={`text-xs text-gray-400 mt-1 ${isConsultant ? 'text-right' : 'text-left'}`}>
                            {msg.sender_name} • {formatTime(msg.created_at)}
                          </p>
                        </div>
                      </div>
                    );
                  })
                )}
                <div ref={messagesEndRef} />
              </div>

              {/* Reply box */}
              <div className="p-4 border-t border-gray-100">
                <div className="flex gap-3">
                  <input
                    type="text"
                    value={replyText}
                    onChange={e => setReplyText(e.target.value)}
                    onKeyDown={e => e.key === 'Enter' && !e.shiftKey && (e.preventDefault(), handleSend())}
                    placeholder="Nhập tin nhắn trả lời..."
                    disabled={sending || selectedConv?.status === 'resolved' || selectedConv?.status === 'closed'}
                    className="flex-1 px-4 py-2.5 border border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 outline-none transition disabled:bg-gray-50 disabled:text-gray-400"
                  />
                  <button
                    onClick={handleSend}
                    disabled={!replyText.trim() || sending || selectedConv?.status === 'resolved' || selectedConv?.status === 'closed'}
                    className="px-5 py-2.5 bg-blue-600 text-white rounded-xl font-medium hover:bg-blue-700 disabled:bg-blue-300 disabled:cursor-not-allowed transition-colors flex items-center gap-2"
                  >
                    {sending ? (
                      <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                    ) : (
                      <Send size={16} />
                    )}
                    Gửi
                  </button>
                </div>
              </div>
            </div>
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center text-gray-400">
              <MessageCircle size={64} className="mb-4 opacity-30" />
              <p className="text-lg font-medium">Chọn một cuộc trò chuyện</p>
              <p className="text-sm mt-1">Chọn cuộc trò chuyện từ danh sách bên trái để xem tin nhắn</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

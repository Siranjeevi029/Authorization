import React, { useState, useEffect, useRef } from 'react';
import { useParams } from 'react-router-dom';
import api from './axios';

const ChatPage = () => {
  const { friendEmail } = useParams();
  const [userProfile, setUserProfile] = useState(null);
  const [friendUsername, setFriendUsername] = useState('');
  const [messages, setMessages] = useState([]);
  const [messageInput, setMessageInput] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const chatRef = useRef(null);

  const fetchData = async () => {
    try {
      const profileRes = await api.get('/api/user/profile');
      setUserProfile(profileRes.data);

      const friendsRes = await api.get('/api/friend/friends');
      const friend = friendsRes.data.find(f => f.email === friendEmail);
      if (friend) {
        setFriendUsername(friend.username);
      } else {
        setError('Friend not found');
        return;
      }

      const messagesRes = await api.get(`/api/messages/${friendEmail}`);
      setMessages(messagesRes.data || []);

      await api.post(`/api/messages/mark-read/${friendEmail}`);
    } catch (err) {
      setError(err.response?.data || err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, [friendEmail]);

  useEffect(() => {
    if (chatRef.current) {
      chatRef.current.scrollTop = chatRef.current.scrollHeight;
    }
  }, [messages]);

  const handleSendMessage = async () => {
    if (!messageInput.trim()) return;
    try {
      await api.post('/api/message', { receiverEmail: friendEmail, content: messageInput });
      setMessageInput('');
      await fetchData();
    } catch (err) {
      setError(err.response?.data || err.message);
    }
  };

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="glass-morphism rounded-2xl p-8 text-center animate-fadeInUp">
        <div className="w-16 h-16 bg-red-500/20 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <span className="text-red-400 text-2xl">💬</span>
        </div>
        <p className="text-white text-lg">Loading chat...</p>
      </div>
    </div>
  );

  if (error) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="glass-morphism rounded-2xl p-8 text-center animate-fadeInUp">
        <div className="w-16 h-16 bg-red-500/20 rounded-2xl flex items-center justify-center mx-auto mb-4">
          <span className="text-red-400 text-2xl">⚠</span>
        </div>
        <p className="text-white text-lg">Error: {error}</p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen py-8 px-4">
      <div className="max-w-4xl mx-auto glass-morphism rounded-2xl p-6 animate-fadeInUp">
        <div className="flex items-center space-x-4 mb-6">
          <div className="w-12 h-12 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center">
            <span className="text-white font-bold text-lg">{friendUsername?.charAt(0)?.toUpperCase()}</span>
          </div>
          <h2 className="text-2xl font-bold text-white">Chat with {friendUsername}</h2>
        </div>
        <div ref={chatRef} className="h-96 overflow-y-auto mb-4 space-y-4 p-4 border border-red-600/30 rounded-xl bg-black/20 backdrop-blur-sm">
          {messages.length === 0 ? (
            <p className="text-center text-gray-300">No messages yet. Start the conversation!</p>
          ) : (
            messages.map((msg, index) => {
              const isOwnMessage = msg.senderEmail === userProfile.email;
              const senderName = isOwnMessage ? userProfile.username : friendUsername;
              return (
                <div key={index} className={`flex flex-col ${isOwnMessage ? 'items-end' : 'items-start'}`}>
                  <div className={`max-w-md px-4 py-3 rounded-xl shadow-lg backdrop-blur-sm border ${isOwnMessage ? 'bg-gradient-to-r from-red-600 to-red-700 text-white border-red-500/30' : 'bg-black/40 text-white border-red-600/30'}`}>
                    <p className="text-sm font-semibold mb-1 opacity-80">{senderName}</p>
                    <p className="text-base">{msg.content}</p>
                  </div>
                  <p className={`text-xs text-gray-400 mt-1 ${isOwnMessage ? 'text-right' : 'text-left'}`}>
                    {new Date(msg.timestamp).toLocaleString()}
                  </p>
                </div>
              );
            })
          )}
        </div>
        <div className="flex gap-3">
          <input
            type="text"
            value={messageInput}
            onChange={(e) => setMessageInput(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSendMessage()}
            className="flex-1 border border-red-600/30 rounded-xl px-4 py-3 focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 bg-black/30 text-white placeholder-gray-400 backdrop-blur-sm"
            placeholder="Type a message..."
          />
          <button
            className="btn-gradient text-white px-6 py-3 rounded-xl font-medium transition-all duration-300 hover:scale-105"
            onClick={handleSendMessage}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
};

export default ChatPage;
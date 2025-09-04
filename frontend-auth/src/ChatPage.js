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

  if (loading) return <div className="text-center py-10">Loading...</div>;
  if (error) return <div className="text-center py-10 text-red-500">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 py-8 px-4">
      <div className="max-w-4xl mx-auto bg-white shadow-md rounded-lg p-6">
        <h2 className="text-2xl font-bold text-gray-800 mb-4">Chat with {friendUsername}</h2>
        <div ref={chatRef} className="h-96 overflow-y-auto mb-4 space-y-4 p-4 border border-gray-200 rounded-lg">
          {messages.length === 0 ? (
            <p className="text-center text-gray-500">No messages yet. Start the conversation!</p>
          ) : (
            messages.map((msg, index) => {
              const isOwnMessage = msg.senderEmail === userProfile.email;
              const senderName = isOwnMessage ? userProfile.username : friendUsername;
              return (
                <div key={index} className={`flex flex-col ${isOwnMessage ? 'items-end' : 'items-start'}`}>
                  <div className={`max-w-md px-4 py-3 rounded-lg shadow ${isOwnMessage ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800'}`}>
                    <p className="text-sm font-semibold mb-1">{senderName}</p>
                    <p className="text-base">{msg.content}</p>
                  </div>
                  <p className={`text-xs text-gray-500 mt-1 ${isOwnMessage ? 'text-right' : 'text-left'}`}>
                    {new Date(msg.timestamp).toLocaleString()}
                  </p>
                </div>
              );
            })
          )}
        </div>
        <div className="flex gap-2">
          <input
            type="text"
            value={messageInput}
            onChange={(e) => setMessageInput(e.target.value)}
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2 focus:outline-none focus:border-blue-500"
            placeholder="Type a message..."
          />
          <button
            className="bg-blue-500 text-white px-6 py-2 rounded-lg hover:bg-blue-600"
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
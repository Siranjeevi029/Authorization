import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import api from './axios';

const HomePage = ({ setErrorMessage }) => {
  const [userProfile, setUserProfile] = useState(null);
  const [matches, setMatches] = useState([]);
  const [friendRequests, setFriendRequests] = useState([]);
  const [friends, setFriends] = useState([]);
  const [activeTab, setActiveTab] = useState('matches');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [emailToUsername, setEmailToUsername] = useState({});
  const [unreadNotifications, setUnreadNotifications] = useState(0);
  const [unreadMessages, setUnreadMessages] = useState(0);
  const [unreadMessagesPerFriend, setUnreadMessagesPerFriend] = useState({});

  const fetchData = async () => {
    try {
      setLoading(true);
      setErrorMessage('');

      const profileRes = await api.get('/api/user/profile');
      setUserProfile(profileRes.data);

      const matchesRes = await api.get('/api/users/matches');
      setMatches(matchesRes.data.matches || []);

      const requestsRes = await api.get('/api/friend/requests');
      setFriendRequests(requestsRes.data || []);

      const friendsRes = await api.get('/api/friend/friends');
      setFriends(friendsRes.data || []);
      const newMap = {};
      friendsRes.data.forEach(f => newMap[f.email] = f.username);
      setEmailToUsername(newMap);

      const unreadRequestsRes = await api.get('/api/friend/requests/unread-count');
      setUnreadNotifications(unreadRequestsRes.data.count || 0);

      const unreadMessagesRes = await api.get('/api/messages/unread-count');
      setUnreadMessages(unreadMessagesRes.data.count || 0);

      try {
        const unreadCountsPerFriendRes = await api.get('/api/messages/unread-counts-per-friend');
        setUnreadMessagesPerFriend(unreadCountsPerFriendRes.data || {});
      } catch (err) {
        console.warn('Failed to fetch unread messages per friend:', err.response?.data || err.message);
        setUnreadMessagesPerFriend({});
      }
    } catch (err) {
      setError(err.response?.data || err.message);
      setErrorMessage('Failed to load homepage data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, [setErrorMessage]);

  const handleSendFriendRequest = async (receiverEmail) => {
    try {
      const res = await api.post('/api/friend/request', { receiverEmail });
      alert(res.data);
      await fetchData();
    } catch (err) {
      alert(err.response?.data || 'Failed to send friend request');
    }
  };

  const handleAcceptRequest = async (requestId) => {
    try {
      const res = await api.post(`/api/friend/request/${requestId}/accept`);
      alert(res.data);
      await fetchData();
    } catch (err) {
      alert(err.response?.data || 'Failed to accept friend request');
      setError(err.response?.data || err.message);
      setErrorMessage('Failed to accept friend request');
    }
  };

  const handleRejectRequest = async (requestId) => {
    try {
      const res = await api.post(`/api/friend/request/${requestId}/reject`);
      alert(res.data);
      await fetchData();
    } catch (err) {
      alert(err.response?.data || 'Failed to reject friend request');
      setError(err.response?.data || err.message);
      setErrorMessage('Failed to reject friend request');
    }
  };

  const handleTabChange = async (tab) => {
    setActiveTab(tab);
    if (tab === 'notifications') {
      try {
        await api.post('/api/friend/requests/mark-read');
        setUnreadNotifications(0);
        const requestsRes = await api.get('/api/friend/requests');
        setFriendRequests(requestsRes.data || []);
      } catch (err) {
        console.warn('Failed to mark friend requests as read:', err.message);
      }
    }
  };

  const getUsername = (email) => emailToUsername[email] || email;

  const UserCard = ({ user }) => (
    <div className="bg-white shadow-md rounded-lg p-6 hover:shadow-lg transition-shadow duration-200">
      <h3 className="text-lg font-semibold text-blue-800">{user.username}</h3>
      <p className="text-sm text-gray-600">
        <span className="font-medium">Skills offered:</span> {user.skillsOffered?.join(', ') || 'None'}
      </p>
      <p className="text-sm text-gray-600">
        <span className="font-medium">Skills wanted:</span> {user.skillsWanted?.join(', ') || 'None'}
      </p>
      <p className="text-sm text-gray-500 mt-2 truncate">{user.bio || 'No bio provided'}</p>
      <div className="mt-4 flex gap-3">
        <Link
          to={`/profile/${user.id}`}
          className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md transition-colors duration-200"
        >
          View Profile
        </Link>
        <button
          className="bg-teal-500 hover:bg-teal-600 text-white px-4 py-2 rounded-md transition-colors duration-200"
          onClick={() => handleSendFriendRequest(user.email)}
        >
          Connect
        </button>
      </div>
    </div>
  );

  const NotificationCard = ({ request }) => (
    <div className="bg-white shadow-md rounded-lg p-6 flex justify-between items-center hover:shadow-lg transition-shadow duration-200">
      <div>
        <p className="text-sm text-gray-600">
          Friend request from: <span className="font-medium">{request.senderUsername}</span>
        </p>
      </div>
      <div className="flex gap-3">
        <button
          className="bg-teal-500 hover:bg-teal-600 text-white px-4 py-2 rounded-md transition-colors duration-200"
          onClick={() => handleAcceptRequest(request.requestId)}
        >
          Accept
        </button>
        <button
          className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md transition-colors duration-200"
          onClick={() => handleRejectRequest(request.requestId)}
        >
          Reject
        </button>
      </div>
    </div>
  );

  const FriendCard = ({ friend }) => {
    const unreadCount = unreadMessagesPerFriend[friend.email]?.unreadCount || 0;
    const lastMessage = unreadMessagesPerFriend[friend.email]?.lastMessage;
    const lastMessageTimestamp = unreadMessagesPerFriend[friend.email]?.lastMessageTimestamp;

    const formatTimestamp = (timestamp) => {
      if (!timestamp) return '';
      const date = new Date(timestamp);
      const today = new Date();
      const isToday = date.toDateString() === today.toDateString();
      return isToday
        ? date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
        : date.toLocaleDateString([], { month: 'short', day: 'numeric' });
    };

    return (
      <Link
        to={`/chat/${friend.email}`}
        className="bg-white shadow-md rounded-lg p-4 flex items-center gap-4 hover:bg-gray-50 transition-colors duration-200 relative"
      >
        <div className="w-10 h-10 bg-blue-100 rounded-full flex items-center justify-center text-blue-800 font-semibold">
          {friend.username.charAt(0).toUpperCase()}
        </div>
        <div className="flex-1">
          <div className="flex items-center justify-between">
            <h3 className="text-base font-semibold text-blue-800">{friend.username}</h3>
            {unreadCount > 0 && (
              <span className="bg-red-500 text-white text-xs rounded-full h-5 w-5 flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </div>
          <p className="text-sm text-gray-600 truncate max-w-[180px]">
            {lastMessage ? (
              <>
                {lastMessage.substring(0, 30)}
                {lastMessage.length > 30 ? '...' : ''}
              </>
            ) : (
              'No messages yet'
            )}
          </p>
          {lastMessageTimestamp && (
            <p className="text-xs text-gray-500">{formatTimestamp(lastMessageTimestamp)}</p>
          )}
        </div>
      </Link>
    );
  };

  if (loading) return <div className="text-center py-10 text-gray-600">Loading...</div>;
  if (error) return <div className="text-center py-10 text-red-500">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-50 py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-5xl mx-auto">
        {userProfile && (
          <div className="mb-8 bg-white shadow-md rounded-lg p-6">
            <h1 className="text-2xl font-bold text-blue-800 mb-2">Welcome, {userProfile.username}</h1>
            <p className="text-gray-600 mb-1">
              <span className="font-medium">Your skills:</span> {userProfile.skillsOffered?.join(', ') || 'None'}
            </p>
            <p className="text-gray-600 mb-3">
              <span className="font-medium">Want to learn:</span> {userProfile.skillsWanted?.join(', ') || 'None'}
            </p>
            <Link to="/editprofile" className="text-blue-600 hover:text-blue-800 font-medium transition-colors duration-200">
              Edit Profile
            </Link>
          </div>
        )}

        <div className="mb-6 flex gap-2 bg-gray-100 rounded-lg p-1">
          <button
            className={`flex-1 py-2 rounded-md font-medium transition-colors duration-200 ${
              activeTab === 'matches' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
            onClick={() => handleTabChange('matches')}
          >
            Matches
          </button>
          <button
            className={`flex-1 relative py-2 rounded-md font-medium transition-colors duration-200 ${
              activeTab === 'notifications' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
            onClick={() => handleTabChange('notifications')}
          >
            Notifications
            {unreadNotifications > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                {unreadNotifications}
              </span>
            )}
          </button>
          <button
            className={`flex-1 relative py-2 rounded-md font-medium transition-colors duration-200 ${
              activeTab === 'friends' ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
            }`}
            onClick={() => handleTabChange('friends')}
          >
            Friends
            {unreadMessages > 0 && (
              <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                {unreadMessages}
              </span>
            )}
          </button>
        </div>

        {activeTab === 'matches' && (
          <div>
            <h2 className="text-xl font-semibold text-blue-800 mb-4">Matches</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              {matches.length > 0 ? (
                matches.map((user) => <UserCard key={user.id} user={user} />)
              ) : (
                <p className="text-gray-600 text-center">No matches found.</p>
              )}
            </div>
          </div>
        )}

        {activeTab === 'notifications' && (
          <div>
            <h2 className="text-xl font-semibold text-blue-800 mb-4">Notifications</h2>
            <div className="grid grid-cols-1 gap-4">
              {friendRequests.length > 0 ? (
                friendRequests.map((request) => <NotificationCard key={request.requestId} request={request} />)
              ) : (
                <p className="text-gray-600 text-center">No pending friend requests.</p>
              )}
            </div>
          </div>
        )}

        {activeTab === 'friends' && (
          <div>
            <h2 className="text-xl font-semibold text-blue-800 mb-4">Friends</h2>
            <div className="grid grid-cols-1 gap-3">
              {friends.length > 0 ? (
                friends.map((friend) => <FriendCard key={friend.email} friend={friend} />)
              ) : (
                <p className="text-gray-600 text-center">No friends yet.</p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomePage;
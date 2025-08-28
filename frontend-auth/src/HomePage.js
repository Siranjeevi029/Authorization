import React, { useState, useEffect, useRef } from 'react';
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

  useEffect(() => {
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
      } catch (err) {
        setError(err.message);
        setErrorMessage('Failed to load homepage data');
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [setErrorMessage]);

  const handleSendFriendRequest = async (receiverEmail) => {
    try {
      const res = await api.post('/api/friend/request', { receiverEmail });
      alert(res.data);
      // Refresh all relevant data only on success
      const requestsRes = await api.get('/api/friend/requests');
      setFriendRequests(requestsRes.data || []);
      const friendsRes = await api.get('/api/friend/friends');
      setFriends(friendsRes.data || []);
      const matchesRes = await api.get('/api/users/matches');
      setMatches(matchesRes.data.matches || []);
    } catch (err) {
      // Show the specific error message from backend
      alert(err.response?.data || 'Failed to send friend request');
      // No refresh on error, page remains as is
    }
  };

  const handleAcceptRequest = async (requestId) => {
    try {
      const res = await api.post(`/api/friend/request/${requestId}/accept`);
      alert(res.data);
      const requestsRes = await api.get('/api/friend/requests');
      setFriendRequests(requestsRes.data || []);
      const friendsRes = await api.get('/api/friend/friends');
      setFriends(friendsRes.data || []);
      const newMap = {};
      friendsRes.data.forEach(f => newMap[f.email] = f.username);
      setEmailToUsername(prev => ({ ...prev, ...newMap }));
    } catch (err) {
      alert(err.response?.data || 'Failed to accept friend request');
      setError(err.message);
      setErrorMessage('Failed to accept friend request');
    }
  };

  const handleRejectRequest = async (requestId) => {
    try {
      const res = await api.post(`/api/friend/request/${requestId}/reject`);
      alert(res.data);
      const requestsRes = await api.get('/api/friend/requests');
      setFriendRequests(requestsRes.data || []);
    } catch (err) {
      alert(err.response?.data || 'Failed to reject friend request');
      setError(err.message);
      setErrorMessage('Failed to reject friend request');
    }
  };

  const getUsername = (email) => emailToUsername[email] || email;

  const UserCard = ({ user }) => (
    <div className="bg-white shadow-md rounded-lg p-4 hover:shadow-lg transition">
      <h3 className="text-lg font-semibold text-gray-800">{user.username}</h3>
      <p className="text-sm text-gray-600">
        Skills offered: <span className="font-medium">{user.skillsOffered?.join(', ') || ''}</span>
      </p>
      <p className="text-sm text-gray-600">
        Skills wanted: <span className="font-medium">{user.skillsWanted?.join(', ') || ''}</span>
      </p>
      <p className="text-sm text-gray-500 mt-1 truncate">{user.bio || 'No bio provided'}</p>
      <div className="mt-2 flex gap-2">
        <Link
          to={`/profile/${user.id}`}
          className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
        >
          View Profile
        </Link>
        <button
          className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
          onClick={() => handleSendFriendRequest(user.email)}
        >
          Connect
        </button>
      </div>
    </div>
  );

  const NotificationCard = ({ request }) => (
    <div className="bg-white shadow-md rounded-lg p-4 flex justify-between items-center">
      <div>
        <p className="text-sm text-gray-600">
          Friend request from: <span className="font-medium">{request.senderUsername}</span>
        </p>
      </div>
      <div className="flex gap-2">
        <button
          className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600"
          onClick={() => handleAcceptRequest(request.requestId)}
        >
          Accept
        </button>
        <button
          className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
          onClick={() => handleRejectRequest(request.requestId)}
        >
          Reject
        </button>
      </div>
    </div>
  );

  const FriendCard = ({ friend }) => (
    <div className="bg-white shadow-md rounded-lg p-4 flex justify-between items-center">
      <div>
        <h3 className="text-lg font-semibold text-gray-800">{friend.username}</h3>
        <p className="text-sm text-gray-600">
          Skills offered: <span className="font-medium">{friend.skillsOffered?.join(', ') || ''}</span>
        </p>
        <p className="text-sm text-gray-600">
          Skills wanted: <span className="font-medium">{friend.skillsWanted?.join(', ') || ''}</span>
        </p>
      </div>
      <Link
        to={`/chat/${friend.email}`}
        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
      >
        Chat
      </Link>
    </div>
  );

  if (loading) return <div className="text-center py-10">Loading...</div>;
  if (error) return <div className="text-center py-10 text-red-500">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 py-8 px-4">
      <div className="max-w-6xl mx-auto">
        {userProfile && (
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-gray-800">Welcome, {userProfile.username}</h1>
            <p className="text-gray-600">
              Your skills: <span className="font-medium">{userProfile.skillsOffered?.join(', ') || ''}</span>
            </p>
            <p className="text-gray-600">
              Want to learn: <span className="font-medium">{userProfile.skillsWanted?.join(', ') || ''}</span>
            </p>

            <Link to="/editprofile" className="text-blue-500 hover:underline">Edit Profile</Link>
          </div>
        )}

        <div className="mb-4 flex gap-2">
          <button
            className={`px-4 py-2 rounded ${activeTab === 'matches' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
            onClick={() => setActiveTab('matches')}
          >
            Matches
          </button>
          <button
            className={`px-4 py-2 rounded ${activeTab === 'notifications' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
            onClick={() => setActiveTab('notifications')}
          >
            Notifications
          </button>
          <button
            className={`px-4 py-2 rounded ${activeTab === 'friends' ? 'bg-blue-500 text-white' : 'bg-gray-200'}`}
            onClick={() => setActiveTab('friends')}
          >
            Friends
          </button>
        </div>

        {activeTab === 'matches' && (
          <div>
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Matches</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
              {matches.length > 0 ? (
                matches.map((user) => <UserCard key={user.id} user={user} />)
              ) : (
                <p className="text-gray-600">No matches found.</p>
              )}
            </div>
          </div>
        )}

        {activeTab === 'notifications' && (
          <div>
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Notifications</h2>
            <div className="grid grid-cols-1 gap-4 mb-8">
              {friendRequests.length > 0 ? (
                friendRequests.map((request) => <NotificationCard key={request.requestId} request={request} />)
              ) : (
                <p className="text-gray-600">No pending friend requests.</p>
              )}
            </div>
          </div>
        )}

        {activeTab === 'friends' && (
          <div>
            <h2 className="text-xl font-semibold text-gray-800 mb-4">Friends</h2>
            <div className="grid grid-cols-1 gap-4 mb-8">
              {friends.length > 0 ? (
                friends.map((friend) => <FriendCard key={friend.email} friend={friend} />)
              ) : (
                <p className="text-gray-600">No friends yet.</p>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default HomePage;
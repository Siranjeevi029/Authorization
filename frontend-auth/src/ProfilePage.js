import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import api from './axios';

const ProfilePage = () => {
  const { id } = useParams();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await api.get(`/api/profile/${id}`);
        // console.log(res.data)
        setProfile(res.data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [id]);

  const handleSendFriendRequest = async () => {
    try {
      const res = await api.post('/api/friend/request', { receiverEmail: profile.email });
      alert(res.data);
      // No refresh needed on profile page
    } catch (err) {
      // Show the specific error message from backend
      alert(err.response?.data || 'Failed to send friend request');
      // No refresh on error, page remains as is
    }
  };

  if (loading) return <div className="text-center py-10">Loading...</div>;
  if (error) return <div className="text-center py-10 text-red-500">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 py-8 px-4">
      <div className="max-w-6xl mx-auto bg-white shadow-md rounded-lg p-4">
        <h1 className="text-2xl font-bold text-gray-800">{profile.username}</h1>
        <p className="text-gray-600">
          Skills offered: <span className="font-medium">{profile.skillsOffered?.join(', ') || ''}</span>
        </p>
        <p className="text-gray-600">
          Skills wanted: <span className="font-medium">{profile.skillsWanted?.join(', ') || ''}</span>
        </p>
        <p className="text-gray-500 mt-2">{profile.bio || 'No bio provided'}</p>
        <button
          className="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600 mt-4"
          onClick={handleSendFriendRequest}
        >
          Connect
        </button>
      </div>
    </div>
  );
};

export default ProfilePage;
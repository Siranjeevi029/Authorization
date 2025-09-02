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
    } catch (err) {
      alert(err.response?.data || 'Failed to send friend request');
    }
  };

  if (loading) return <div className="text-center py-10 text-gray-600">Loading...</div>;
  if (error) return <div className="text-center py-10 text-red-500">Error: {error}</div>;

  return (
    <div className="min-h-screen bg-gray-100 py-8 px-4">
      <div className="max-w-2xl mx-auto bg-white shadow-md rounded-lg p-6">
        <h1 className="text-3xl font-bold text-secondary mb-4">{profile.username}</h1>
        <p className="text-gray-600 mb-2">
          <span className="font-medium">Skills offered:</span> {profile.skillsOffered?.join(', ') || 'None'}
        </p>
        <p className="text-gray-600 mb-2">
          <span className="font-medium">Skills wanted:</span> {profile.skillsWanted?.join(', ') || 'None'}
        </p>
        <p className="text-gray-500 mb-4">{profile.bio || 'No bio provided'}</p>
        <button
          className="bg-accent hover:bg-green-600 text-white px-4 py-2 rounded-md transition"
          onClick={handleSendFriendRequest}
        >
          Connect
        </button>
      </div>
    </div>
  );
};

export default ProfilePage;
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
      // Show success message with better styling
      const successDiv = document.createElement('div');
      successDiv.className = 'fixed top-4 right-4 bg-green-600 text-white px-6 py-3 rounded-xl shadow-lg z-50 animate-fadeInUp';
      successDiv.textContent = res.data || 'Friend request sent successfully!';
      document.body.appendChild(successDiv);
      setTimeout(() => successDiv.remove(), 3000);
    } catch (err) {
      // Show error message with better styling
      const errorDiv = document.createElement('div');
      errorDiv.className = 'fixed top-4 right-4 bg-red-600 text-white px-6 py-3 rounded-xl shadow-lg z-50 animate-fadeInUp';
      errorDiv.textContent = err.response?.data || 'Failed to send friend request';
      document.body.appendChild(errorDiv);
      setTimeout(() => errorDiv.remove(), 3000);
    }
  };

  if (loading) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="glass-morphism rounded-2xl p-8 text-center animate-fadeInUp">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-red-500 mx-auto mb-4"></div>
        <p className="text-white text-lg">Loading profile...</p>
      </div>
    </div>
  );
  
  if (error) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="glass-morphism rounded-2xl p-8 text-center animate-fadeInUp">
        <div className="mx-auto h-16 w-16 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center mb-6">
          <span className="text-white font-bold text-2xl">⚠️</span>
        </div>
        <p className="text-red-300 text-lg">Error: {error}</p>
      </div>
    </div>
  );

  return (
    <div className="min-h-screen py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center animate-fadeInUp mb-8">
          <div className="mx-auto h-20 w-20 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center mb-6">
            {profile.profilePicture ? (
              <img 
                src={profile.profilePicture} 
                alt={profile.username}
                className="w-full h-full rounded-xl object-cover"
              />
            ) : (
              <span className="text-white font-bold text-3xl">👤</span>
            )}
          </div>
          <h1 className="text-4xl font-bold gradient-text mb-2">{profile.username}</h1>
          <p className="text-gray-300 text-lg">{profile.location || 'Location not specified'}</p>
        </div>
        
        <div className="glass-morphism rounded-2xl p-8 animate-fadeInUp space-y-6" style={{animationDelay: '0.2s'}}>
          {profile.bio && (
            <div className="text-center">
              <p className="text-gray-300 text-lg italic leading-relaxed">"{profile.bio}"</p>
            </div>
          )}
          
          <div className="grid md:grid-cols-2 gap-6">
            <div className="bg-black/30 border border-red-600/30 rounded-xl p-6">
              <h3 className="text-lg font-semibold text-white mb-3 flex items-center">
                <span className="mr-2">🎯</span> Skills Offered
              </h3>
              {profile.skillsOffered?.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {profile.skillsOffered.map((skill, index) => (
                    <span key={index} className="bg-red-600/20 text-red-300 px-3 py-1 rounded-full text-sm border border-red-600/30">
                      {typeof skill === 'object' ? skill.name : skill}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400 italic">No skills offered</p>
              )}
            </div>
            
            <div className="bg-black/30 border border-red-600/30 rounded-xl p-6">
              <h3 className="text-lg font-semibold text-white mb-3 flex items-center">
                <span className="mr-2">🎓</span> Skills Wanted
              </h3>
              {profile.skillsWanted?.length > 0 ? (
                <div className="flex flex-wrap gap-2">
                  {profile.skillsWanted.map((skill, index) => (
                    <span key={index} className="bg-red-600/20 text-red-300 px-3 py-1 rounded-full text-sm border border-red-600/30">
                      {typeof skill === 'object' ? skill.name : skill}
                    </span>
                  ))}
                </div>
              ) : (
                <p className="text-gray-400 italic">No skills wanted</p>
              )}
            </div>
          </div>
          
          {profile.knownLanguages && profile.knownLanguages.length > 0 && (
            <div className="bg-black/30 border border-red-600/30 rounded-xl p-6">
              <h3 className="text-lg font-semibold text-white mb-3 flex items-center">
                <span className="mr-2">🌍</span> Languages
              </h3>
              <div className="flex flex-wrap gap-2">
                {profile.knownLanguages.map((language, index) => (
                  <span key={index} className="bg-red-600/20 text-red-300 px-3 py-1 rounded-full text-sm border border-red-600/30">
                    {language}
                  </span>
                ))}
              </div>
            </div>
          )}
          
          <div className="text-center pt-4">
            <button
              className="btn-gradient text-white font-semibold py-3 px-8 rounded-xl transition-all duration-300 hover:scale-105"
              onClick={handleSendFriendRequest}
            >
              🤝 Send Friend Request
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProfilePage;
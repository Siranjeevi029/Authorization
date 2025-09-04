import React, { useState, useEffect } from 'react';
import api from '../axios';
import { useNavigate } from 'react-router-dom';

const Profile = ({ setErrorMessage }) => {
  const navigate = useNavigate();
  const [isNewUser, setIsNewUser] = useState(false);
  const [formData, setFormData] = useState({
    fullName: '',
    profilePicture: '',
    location: '',
    age: '',
    skillsOffered: [{ name: '', level: '', description: '', availability: '' }],
    skillsWanted: [{ name: '', level: '' }],
    bio: '',
    knownLanguages: '',
  });
  const [error, setError] = useState('');

  useEffect(() => {
    const checkUserStatus = async () => {
      try {
        const response = await api.get('api/user/status');
        if (response.data === 'new') {
          setIsNewUser(true);
        } else {
          navigate('/home');
        }
      } catch (err) {
        setError('Error checking status');
        setErrorMessage('Session expired');
        localStorage.removeItem('token');
        navigate('/login');
      }
    };
    checkUserStatus();
  }, [navigate, setErrorMessage]);

  const handleInputChange = (e, index, type) => {
    const { name, value } = e.target;
    if (type === 'skillsOffered') {
      const skillsOffered = [...formData.skillsOffered];
      skillsOffered[index][name] = value;
      setFormData({ ...formData, skillsOffered });
    } else if (type === 'skillsWanted') {
      const skillsWanted = [...formData.skillsWanted];
      skillsWanted[index][name] = value;
      setFormData({ ...formData, skillsWanted });
    } else {
      setFormData({ ...formData, [name]: value });
    }
  };

  const addSkill = (type) => {
    if (type === 'skillsOffered') {
      setFormData({
        ...formData,
        skillsOffered: [...formData.skillsOffered, { name: '', level: '', description: '', availability: '' }],
      });
    } else {
      setFormData({
        ...formData,
        skillsWanted: [...formData.skillsWanted, { name: '', level: '' }],
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const profile = {
        ...formData,
        age: parseInt(formData.age),
        knownLanguages: formData.knownLanguages.split(',').map(lang => lang.trim()).filter(lang => lang),
        rating: 0.0,
      };
      await api.post('api/profile', profile);
      navigate('/home');
    } catch (err) {
      setError('Error saving profile');
    }
  };

  if (!isNewUser) return null;

  return (
    <div className="min-h-screen py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center animate-fadeInUp mb-8">
          <div className="mx-auto h-16 w-16 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center mb-6">
            <span className="text-white font-bold text-2xl">👤</span>
          </div>
          <h2 className="text-3xl font-bold gradient-text mb-2">Complete Your Profile</h2>
          <p className="text-gray-300 text-lg">Tell us about yourself and your skills</p>
        </div>
        
        <div className="glass-morphism rounded-2xl p-8 animate-fadeInUp" style={{animationDelay: '0.2s'}}>
          {error && (
            <div className="bg-red-900/50 border border-red-600 text-red-300 px-4 py-3 rounded-xl text-sm text-center mb-6">
              {error}
            </div>
          )}
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Full Name</label>
              <input
                type="text"
                name="fullName"
                value={formData.fullName}
                onChange={(e) => handleInputChange(e)}
                required
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="Enter your full name"
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Profile Picture URL</label>
              <input
                type="url"
                name="profilePicture"
                value={formData.profilePicture}
                onChange={(e) => handleInputChange(e)}
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="https://example.com/your-photo.jpg"
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Location</label>
              <input
                type="text"
                name="location"
                value={formData.location}
                onChange={(e) => handleInputChange(e)}
                required
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="City, Country"
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Age</label>
              <input
                type="number"
                name="age"
                value={formData.age}
                onChange={(e) => handleInputChange(e)}
                required
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="25"
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-white mb-3">Skills Offered</label>
              {formData.skillsOffered.map((skill, index) => (
                <div key={index} className="bg-black/30 border border-red-600/30 p-4 rounded-xl mb-4 space-y-3">
                  <input
                    type="text"
                    name="name"
                    placeholder="Skill Name"
                    value={skill.name}
                    onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                    required
                    className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  />
                  <input
                    type="text"
                    name="level"
                    placeholder="Level (e.g., Intermediate)"
                    value={skill.level}
                    onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                    required
                    className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  />
                  <input
                    type="text"
                    name="description"
                    placeholder="Description"
                    value={skill.description}
                    onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                    className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  />
                  <input
                    type="text"
                    name="availability"
                    placeholder="Availability (e.g., 9-10pm)"
                    value={skill.availability}
                    onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                    className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  />
                </div>
              ))}
              <button
                type="button"
                onClick={() => addSkill('skillsOffered')}
                className="bg-red-600/20 hover:bg-red-600/30 text-white px-4 py-2 rounded-xl transition-all duration-300 border border-red-600/30"
              >
                Add Skill Offered
              </button>
            </div>
            <div>
              <label className="block text-sm font-semibold text-white mb-3">Skills Wanted</label>
              {formData.skillsWanted.map((skill, index) => (
                <div key={index} className="bg-black/30 border border-red-600/30 p-4 rounded-xl mb-4 space-y-3">
                  <input
                    type="text"
                    name="name"
                    placeholder="Skill Name"
                    value={skill.name}
                    onChange={(e) => handleInputChange(e, index, 'skillsWanted')}
                    required
                    className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  />
                  <input
                    type="text"
                    name="level"
                    placeholder="Level (e.g., Beginner)"
                    value={skill.level}
                    onChange={(e) => handleInputChange(e, index, 'skillsWanted')}
                    required
                    className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  />
                </div>
              ))}
              <button
                type="button"
                onClick={() => addSkill('skillsWanted')}
                className="bg-red-600/20 hover:bg-red-600/30 text-white px-4 py-2 rounded-xl transition-all duration-300 border border-red-600/30"
              >
                Add Skill Wanted
              </button>
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Bio</label>
              <textarea
                name="bio"
                value={formData.bio}
                onChange={(e) => handleInputChange(e)}
                rows="4"
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="Tell us about yourself..."
              />
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Known Languages (comma-separated)</label>
              <input
                type="text"
                name="knownLanguages"
                value={formData.knownLanguages}
                onChange={(e) => handleInputChange(e)}
                placeholder="e.g., English, Spanish, French"
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
              />
            </div>
            
            <button
              type="submit"
              className="w-full btn-gradient text-white font-semibold py-3 px-6 rounded-xl transition-all duration-300"
            >
              Save Profile
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Profile;
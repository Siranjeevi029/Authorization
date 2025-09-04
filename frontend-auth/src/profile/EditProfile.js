import React, { useState, useEffect } from 'react';
import api from '../axios';


const EditProfile = ({ navigate }) => {
  const [profile, setProfile] = useState({
    fullName: '',
    bio: '',
    skillsOffered: [],
    skillsWanted: []
  });

  useEffect(() => {
    api.get('/api/user/profile')
      .then(response => {
        setProfile({
          fullName: response.data.username,
          bio: response.data.bio,
          skillsOffered: response.data.skillsOffered.map(name => ({ name })),
          skillsWanted: response.data.skillsWanted.map(name => ({ name }))
        });
      })
      .catch(error => console.error('Error fetching profile:', error));
  }, []);

  const handleChange = (e) => {
    setProfile({ ...profile, [e.target.name]: e.target.value });
  };

  const handleSkillChange = (type, index, value) => {
    const updatedSkills = [...profile[type]];
    updatedSkills[index].name = value;
    setProfile({ ...profile, [type]: updatedSkills });
  };

  const addSkill = (type) => {
    setProfile({ ...profile, [type]: [...profile[type], { name: '' }] });
  };

  const removeSkill = (type, index) => {
    setProfile({
      ...profile,
      [type]: profile[type].filter((_, i) => i !== index)
    });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    api.put('/api/profile', profile)
      .then(response => {
        alert('Profile updated successfully!');
        navigate('/home');
      })
      .catch(error => {
        console.error('Error updating profile:', error);
        alert('Failed to update profile: ' + (error.response?.data || error.message));
      });
  };

  return (
    <div className="min-h-screen py-10 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="text-center animate-fadeInUp mb-8">
          <div className="mx-auto h-16 w-16 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center mb-6">
            <span className="text-white font-bold text-2xl">✏️</span>
          </div>
          <h2 className="text-3xl font-bold gradient-text mb-2">Edit Profile</h2>
          <p className="text-gray-300 text-lg">Update your information and skills</p>
        </div>
        
        <div className="glass-morphism rounded-2xl p-8 animate-fadeInUp" style={{animationDelay: '0.2s'}}>
          <form onSubmit={handleSubmit} className="space-y-6">
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Full Name</label>
              <input
                type="text"
                name="fullName"
                value={profile.fullName}
                onChange={handleChange}
                required
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="Enter your full name"
              />
            </div>
            <div>
              <label className="block text-sm font-semibold text-white mb-2">Bio</label>
              <textarea
                name="bio"
                value={profile.bio}
                onChange={handleChange}
                rows="4"
                className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                placeholder="Tell us about yourself..."
              />
            </div>
            <div>
              <h3 className="text-lg font-semibold text-white mb-3">Skills Offered</h3>
              {profile.skillsOffered.map((skill, index) => (
                <div key={index} className="flex items-center space-x-3 mb-3">
                  <input
                    type="text"
                    value={skill.name}
                    onChange={(e) => handleSkillChange('skillsOffered', index, e.target.value)}
                    required
                    className="flex-1 px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                    placeholder="Skill name"
                  />
                  <button
                    type="button"
                    onClick={() => removeSkill('skillsOffered', index)}
                    className="bg-red-600/80 hover:bg-red-600 text-white px-3 py-3 rounded-xl transition-all duration-300"
                  >
                    ✕
                  </button>
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
              <h3 className="text-lg font-semibold text-white mb-3">Skills Wanted</h3>
              {profile.skillsWanted.map((skill, index) => (
                <div key={index} className="flex items-center space-x-3 mb-3">
                  <input
                    type="text"
                    value={skill.name}
                    onChange={(e) => handleSkillChange('skillsWanted', index, e.target.value)}
                    required
                    className="flex-1 px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                    placeholder="Skill name"
                  />
                  <button
                    type="button"
                    onClick={() => removeSkill('skillsWanted', index)}
                    className="bg-red-600/80 hover:bg-red-600 text-white px-3 py-3 rounded-xl transition-all duration-300"
                  >
                    ✕
                  </button>
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

export default EditProfile;
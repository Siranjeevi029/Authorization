import React, { useState, useEffect } from 'react';
import api from '../axios';
import { useNavigate } from 'react-router-dom';

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
    <div className="max-w-2xl mx-auto bg-white p-6 rounded-lg shadow-md">
      <h2 className="text-2xl font-bold text-secondary mb-4 text-center">Edit Profile</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700">Full Name</label>
          <input
            type="text"
            name="fullName"
            value={profile.fullName}
            onChange={handleChange}
            required
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Bio</label>
          <textarea
            name="bio"
            value={profile.bio}
            onChange={handleChange}
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <h3 className="text-lg font-semibold text-gray-700">Skills Offered</h3>
          {profile.skillsOffered.map((skill, index) => (
            <div key={index} className="flex items-center space-x-2 mb-2">
              <input
                type="text"
                value={skill.name}
                onChange={(e) => handleSkillChange('skillsOffered', index, e.target.value)}
                required
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
              />
              <button
                type="button"
                onClick={() => removeSkill('skillsOffered', index)}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-md transition"
              >
                Remove
              </button>
            </div>
          ))}
          <button
            type="button"
            onClick={() => addSkill('skillsOffered')}
            className="bg-accent hover:bg-green-600 text-white px-4 py-2 rounded-md transition"
          >
            Add Skill
          </button>
        </div>
        <div>
          <h3 className="text-lg font-semibold text-gray-700">Skills Wanted</h3>
          {profile.skillsWanted.map((skill, index) => (
            <div key={index} className="flex items-center space-x-2 mb-2">
              <input
                type="text"
                value={skill.name}
                onChange={(e) => handleSkillChange('skillsWanted', index, e.target.value)}
                required
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
              />
              <button
                type="button"
                onClick={() => removeSkill('skillsWanted', index)}
                className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded-md transition"
              >
                Remove
              </button>
            </div>
          ))}
          <button
            type="button"
            onClick={() => addSkill('skillsWanted')}
            className="bg-accent hover:bg-green-600 text-white px-4 py-2 rounded-md transition"
          >
            Add Skill
          </button>
        </div>
        <button
          type="submit"
          className="w-full bg-primary hover:bg-blue-700 text-white p-2 rounded-md transition"
        >
          Save Profile
        </button>
      </form>
    </div>
  );
};

export default EditProfile;
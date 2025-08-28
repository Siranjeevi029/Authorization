import React, { useState, useEffect } from 'react';
import api from '../axios';

const EditProfile = ({navigate}) => {

  

  const [profile, setProfile] = useState({
    fullName: '',
    bio: '',
    skillsOffered: [],
    skillsWanted: []
  });

  useEffect(() => {
    // Fetch current profile
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
    <form onSubmit={handleSubmit}>
      <h2>Edit Profile</h2>
      <div>
        <label>Full Name:</label>
        <input
          type="text"
          name="fullName"
          value={profile.fullName}
          onChange={handleChange}
          required
        />
      </div>
      <div>
        <label>Bio:</label>
        <textarea
          name="bio"
          value={profile.bio}
          onChange={handleChange}
        />
      </div>
      <div>
        <h3>Skills Offered</h3>
        {profile.skillsOffered.map((skill, index) => (
          <div key={index}>
            <input
              type="text"
              value={skill.name}
              onChange={(e) => handleSkillChange('skillsOffered', index, e.target.value)}
              required
            />
            <button type="button" onClick={() => removeSkill('skillsOffered', index)}>
              Remove
            </button>
          </div>
        ))}
        <button type="button" onClick={() => addSkill('skillsOffered')}>
          Add Skill
        </button>
      </div>
      <div>
        <h3>Skills Wanted</h3>
        {profile.skillsWanted.map((skill, index) => (
          <div key={index}>
            <input
              type="text"
              value={skill.name}
              onChange={(e) => handleSkillChange('skillsWanted', index, e.target.value)}
              required
            />
            <button type="button" onClick={() => removeSkill('skillsWanted', index)}>
              Remove
            </button>
          </div>
        ))}
        <button type="button" onClick={() => addSkill('skillsWanted')}>
          Add Skill
        </button>
      </div>
      <button type="submit">Save Profile</button>
    </form>
  );
};

export default EditProfile;
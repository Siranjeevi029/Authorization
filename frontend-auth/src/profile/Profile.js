import React, { useState, useEffect } from 'react';
import api from '../axios';
import { useNavigate } from 'react-router-dom';
import './Profile.css';

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
          navigate('/home'); // Redirect if existing user
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
    <div className="profile-container">
      <h2>Complete Your Profile</h2>
      {error && <p className="error">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label>
          Full Name:
          <input
            type="text"
            name="fullName"
            value={formData.fullName}
            onChange={(e) => handleInputChange(e)}
            required
          />
        </label>
        <label>
          Profile Picture URL:
          <input
            type="url"
            name="profilePicture"
            value={formData.profilePicture}
            onChange={(e) => handleInputChange(e)}
          />
        </label>
        <label>
          Location:
          <input
            type="text"
            name="location"
            value={formData.location}
            onChange={(e) => handleInputChange(e)}
            required
          />
        </label>
        <label>
          Age:
          <input
            type="number"
            name="age"
            value={formData.age}
            onChange={(e) => handleInputChange(e)}
            required
          />
        </label>
        <label>Skills Offered:</label>
        {formData.skillsOffered.map((skill, index) => (
          <div key={index} className="skill">
            <input
              type="text"
              name="name"
              placeholder="Skill Name"
              value={skill.name}
              onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
              required
            />
            <input
              type="text"
              name="level"
              placeholder="Level (e.g., Intermediate)"
              value={skill.level}
              onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
              required
            />
            <input
              type="text"
              name="description"
              placeholder="Description"
              value={skill.description}
              onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
            />
            <input
              type="text"
              name="availability"
              placeholder="Availability (e.g., 9-10pm)"
              value={skill.availability}
              onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
            />
          </div>
        ))}
        <button type="button" onClick={() => addSkill('skillsOffered')}>
          Add Skill Offered
        </button>
        <label>Skills Wanted:</label>
        {formData.skillsWanted.map((skill, index) => (
          <div key={index} className="skill">
            <input
              type="text"
              name="name"
              placeholder="Skill Name"
              value={skill.name}
              onChange={(e) => handleInputChange(e, index, 'skillsWanted')}
              required
            />
            <input
              type="text"
              name="level"
              placeholder="Level (e.g., Beginner)"
              value={skill.level}
              onChange={(e) => handleInputChange(e, index, 'skillsWanted')}
              required
            />
          </div>
        ))}
        <button type="button" onClick={() => addSkill('skillsWanted')}>
          Add Skill Wanted
        </button>
        <label>
          Bio:
          <textarea
            name="bio"
            value={formData.bio}
            onChange={(e) => handleInputChange(e)}
          />
        </label>
        <label>
          Known Languages (comma-separated):
          <input
            type="text"
            name="knownLanguages"
            value={formData.knownLanguages}
            onChange={(e) => handleInputChange(e)}
            placeholder="e.g., English, Spanish"
          />
        </label>
        <button type="submit">Save Profile</button>
      </form>
    </div>
  );
};

export default Profile;
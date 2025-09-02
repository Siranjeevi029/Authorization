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
    <div className="max-w-2xl mx-auto bg-white p-6 rounded-lg shadow-md">
      <h2 className="text-2xl font-bold text-secondary mb-4 text-center">Complete Your Profile</h2>
      {error && <p className="text-red-500 text-center mb-4">{error}</p>}
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-sm font-medium text-gray-700">Full Name</label>
          <input
            type="text"
            name="fullName"
            value={formData.fullName}
            onChange={(e) => handleInputChange(e)}
            required
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Profile Picture URL</label>
          <input
            type="url"
            name="profilePicture"
            value={formData.profilePicture}
            onChange={(e) => handleInputChange(e)}
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Location</label>
          <input
            type="text"
            name="location"
            value={formData.location}
            onChange={(e) => handleInputChange(e)}
            required
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Age</label>
          <input
            type="number"
            name="age"
            value={formData.age}
            onChange={(e) => handleInputChange(e)}
            required
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Skills Offered</label>
          {formData.skillsOffered.map((skill, index) => (
            <div key={index} className="border border-gray-300 p-4 rounded-md mb-4">
              <input
                type="text"
                name="name"
                placeholder="Skill Name"
                value={skill.name}
                onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                required
                className="w-full p-2 border border-gray-300 rounded-md mb-2 focus:ring-primary focus:border-primary"
              />
              <input
                type="text"
                name="level"
                placeholder="Level (e.g., Intermediate)"
                value={skill.level}
                onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                required
                className="w-full p-2 border border-gray-300 rounded-md mb-2 focus:ring-primary focus:border-primary"
              />
              <input
                type="text"
                name="description"
                placeholder="Description"
                value={skill.description}
                onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                className="w-full p-2 border border-gray-300 rounded-md mb-2 focus:ring-primary focus:border-primary"
              />
              <input
                type="text"
                name="availability"
                placeholder="Availability (e.g., 9-10pm)"
                value={skill.availability}
                onChange={(e) => handleInputChange(e, index, 'skillsOffered')}
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
              />
            </div>
          ))}
          <button
            type="button"
            onClick={() => addSkill('skillsOffered')}
            className="bg-accent hover:bg-green-600 text-white px-4 py-2 rounded-md transition"
          >
            Add Skill Offered
          </button>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Skills Wanted</label>
          {formData.skillsWanted.map((skill, index) => (
            <div key={index} className="border border-gray-300 p-4 rounded-md mb-4">
              <input
                type="text"
                name="name"
                placeholder="Skill Name"
                value={skill.name}
                onChange={(e) => handleInputChange(e, index, 'skillsWanted')}
                required
                className="w-full p-2 border border-gray-300 rounded-md mb-2 focus:ring-primary focus:border-primary"
              />
              <input
                type="text"
                name="level"
                placeholder="Level (e.g., Beginner)"
                value={skill.level}
                onChange={(e) => handleInputChange(e, index, 'skillsWanted')}
                required
                className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
              />
            </div>
          ))}
          <button
            type="button"
            onClick={() => addSkill('skillsWanted')}
            className="bg-accent hover:bg-green-600 text-white px-4 py-2 rounded-md transition"
          >
            Add Skill Wanted
          </button>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Bio</label>
          <textarea
            name="bio"
            value={formData.bio}
            onChange={(e) => handleInputChange(e)}
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">Known Languages (comma-separated)</label>
          <input
            type="text"
            name="knownLanguages"
            value={formData.knownLanguages}
            onChange={(e) => handleInputChange(e)}
            placeholder="e.g., English, Spanish"
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
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

export default Profile;
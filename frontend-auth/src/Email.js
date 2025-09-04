import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from './axios';

const Email = ({email}) => {
  const [code, setCode] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      
      await api.post('/otp/verify', { email,otp:code });
      
      navigate('/login');
    } catch (err) {
      setErrorMessage('Invalid verification code');
      console.error(err);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <label htmlFor="code">Enter Verification Code:</label>
      <input
        type="number"
        value={code}
        onChange={(e) => setCode(e.target.value)}
        required
      />
      <button type="submit">Submit</button>
      <p>{errorMessage}</p>
    </form>
  );
};

export default Email;
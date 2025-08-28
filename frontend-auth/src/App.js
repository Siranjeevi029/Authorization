import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import api from './axios';
import Login from './Login';
import Register from './Register';
import Email from './Email';
import Profile from './profile/Profile';
import HomePage from './HomePage';
import './index.css';
import './App.css';
import EditProfile from './profile/EditProfile';
import ProfilePage from './ProfilePage';
import ChatPage from './ChatPage';

function App() {
  const navigate = useNavigate();
  const location = useLocation();
  const [user, setUser] = useState(null);
  const [errorMessage, setErrorMessage] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');

  useEffect(() => {
    setErrorMessage('');
    const token = localStorage.getItem('token');
    if (token) {
      console.log('Token:', token);
      fetchUserData(token);
    } else if (location.pathname !== '/login' && location.pathname !== '/register' && location.pathname !== '/email') {
      navigate('/login');
    }
  }, [location.pathname]);

  const fetchUserData = async (token) => {
    try {
      const statusRes = await api.get('/api/user/status');
      try {
        const profileRes = await api.get('/api/user/profile');
        setUser({
          email: profileRes.data.email,
          username: profileRes.data.username || 'Unknown'
        });
        if (statusRes.data === 'new' && location.pathname !== '/profile') {
          navigate('/profile');
        } else if (statusRes.data === 'existing' && location.pathname === '/profile') {
          
          navigate('/home');
        }
      } catch (profileErr) {
        console.error('Profile Fetch Error:', profileErr.response?.data || profileErr.message);
        navigate('/profile');
      }
    } catch (err) {
      console.error('Status Fetch Error:', err.response?.data || err.message);
      setErrorMessage('Session expired');
      localStorage.removeItem('token');
      navigate('/login');
    }
  };

  const handleGoogleSignIn = async (credentialResponse) => {
    setErrorMessage('');
    try {
      const res = await fetch('http://localhost:8080/api/auth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: credentialResponse.credential }),
      });
      const data = await res.json();
      if (res.ok) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('email', data.email);
        setUser({ email: data.email, username: data.username || 'Unknown' });
        navigate('/profile');
      } else {
        setErrorMessage(data.error || 'Google authentication failed');
      }
    } catch (err) {
      setErrorMessage('Error communicating with backend');
      console.error(err);
    }
  };

  const handleLogin = async (event) => {
    setErrorMessage('');
    event.preventDefault();
    try {
      const value = await api.post('/login', {
        email,
        password,
        message: 'hello',
      });
      localStorage.setItem('token', value.data);
      localStorage.setItem('email', email);
      setUser({ email, username: 'Unknown' });
      navigate('/profile');
    } catch (error) {
      setErrorMessage('Username and password mismatch');
      console.log(error);
    }
  };

  const handleRegister = async (event) => {
    setErrorMessage('');
    event.preventDefault();
    try {
      const value = await api.post('/register', { email, password });
      console.log(value);
      navigate('/email');
    } catch (error) {
      setErrorMessage('Username already exists');
      console.log(error);
    }
  };

  const handleSignOut = () => {
    setUser(null);
    localStorage.clear();
    setErrorMessage('');
    navigate('/login');
    setEmail('');
  };

  return (
    <GoogleOAuthProvider clientId="268005316048-h46lstqbi86sss91hpdgerorlkf8vkop.apps.googleusercontent.com">
      <div style={{ padding: '20px' }}>
        <nav style={{ marginBottom: '20px' }}>
          {user ? (
            <>
              <span>Welcome, {user.username}</span>
              <button onClick={handleSignOut} style={{ marginLeft: '10px' }}>
                Sign Out
              </button>
            </>
          ) : (
            <>
              <Link to="/login" style={{ marginRight: '10px' }}>Login</Link>
              <Link to="/register">Register</Link>
            </>
          )}
        </nav>

        <Routes>
          <Route
            path="/login"
            element={
              <Login
                handleLogin={handleLogin}
                email={email}
                setEmail={setEmail}
                password={password}
                setPassword={setPassword}
                errorMessage={errorMessage}
                setErrorMessage={setErrorMessage}
                handleGoogleSignIn={handleGoogleSignIn}
              />
            }
          />
          <Route
            path="/register"
            element={
              <Register
                handleRegister={handleRegister}
                errorMessage={errorMessage}
                email={email}
                setEmail={setEmail}
                password={password}
                setPassword={setPassword}
              />
            }
          />
          
          
           <Route  path="/home"
            element={<HomePage setErrorMessage={setErrorMessage} navigate={navigate} />}/>
          
          <Route path="/email" element={<Email email={email} />} />
          <Route path='/editprofile' element={<EditProfile navigate={navigate}/>}/>
          
          <Route path="/profile"
            element={<Profile setErrorMessage={setErrorMessage} />}/>
           
          <Route path='/profile/:id' element={<ProfilePage/>}/>
          <Route path='/chat/:friendEmail' element={<ChatPage/>}/>
          

        </Routes>
      </div>
    </GoogleOAuthProvider>
  );
}

export default App;
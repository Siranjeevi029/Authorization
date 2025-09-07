import React, { useState, useEffect } from 'react';
import { Routes, Route, Link, useNavigate, useLocation } from 'react-router-dom';
import { GoogleOAuthProvider } from '@react-oauth/google';
import api from './axios';
import Login from './Login';
import Register from './Register';
import Email from './Email';
import Profile from './profile/Profile';
import HomePage from './HomePage';
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
  const [waitTime, setWaitTime] = useState(0);
  const [isWaiting, setIsWaiting] = useState(false);

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
      const res = await fetch(`${process.env.REACT_APP_API_URL}/api/auth/google`, {
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
    
    if (isWaiting) {
      return; // Prevent submission while waiting
    }
    
    try {
      const value = await api.post('/register', { email, password });
      console.log(value);
      navigate('/email');
    } catch (error) {
      const errorMsg = error.response?.data || 'Registration failed';
      
      // Check if it's a wait message
      if (errorMsg.includes('Please wait') && errorMsg.includes('seconds')) {
        const waitSeconds = parseInt(errorMsg.match(/\d+/)[0]);
        setWaitTime(waitSeconds);
        setIsWaiting(true);
        setErrorMessage(`Please wait ${waitSeconds} seconds before requesting a new OTP`);
        
        // Start countdown timer
        const timer = setInterval(() => {
          setWaitTime(prev => {
            if (prev <= 1) {
              setIsWaiting(false);
              setErrorMessage('');
              clearInterval(timer);
              return 0;
            }
            const newTime = prev - 1;
            setErrorMessage(`Please wait ${newTime} seconds before requesting a new OTP`);
            return newTime;
          });
        }, 1000);
      } else {
        setErrorMessage(errorMsg.includes('already exists') ? 'Username already exists' : errorMsg);
      }
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
      <div className="min-h-screen">
        <nav className="glass-morphism border-0 border-b border-red-600/30 sticky top-0 z-50">
          <div className="max-w-6xl mx-auto px-6 py-4">
            <div className="flex justify-between items-center">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-gradient-to-br from-red-600 to-red-800 rounded-lg flex items-center justify-center">
                  <span className="text-white font-bold text-lg">S</span>
                </div>
                <span className="text-white font-bold text-xl tracking-tight">SkillConnect</span>
              </div>
              
              {user ? (
                <div className="flex items-center space-x-6 animate-slideInRight">
                  <div className="flex items-center space-x-3">
                    <div className="w-8 h-8 bg-gradient-to-br from-red-500 to-red-700 rounded-full flex items-center justify-center">
                      <span className="text-white font-semibold text-sm">{user.username?.charAt(0)?.toUpperCase()}</span>
                    </div>
                    <span className="text-white font-medium">Welcome, {user.username}</span>
                  </div>
                  <button
                    onClick={handleSignOut}
                    className="bg-red-600 hover:bg-red-700 text-white px-6 py-2 rounded-lg font-medium transition-all duration-300 border border-red-600"
                  >
                    Sign Out
                  </button>
                </div>
              ) : (
                <div className="flex space-x-4">
                  <Link 
                    to="/login" 
                    className="text-gray-300 hover:text-white font-medium px-4 py-2 rounded-lg hover:bg-red-600/20 transition-all duration-300"
                  >
                    Login
                  </Link>
                  <Link 
                    to="/register" 
                    className="bg-red-600 hover:bg-red-700 text-white font-medium px-6 py-2 rounded-lg transition-all duration-300"
                  >
                    Register
                  </Link>
                </div>
              )}
            </div>
          </div>
        </nav>
        <main className="max-w-6xl mx-auto p-6">
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
                  isWaiting={isWaiting}
                />
              }
            />
            <Route path="/home" element={<HomePage setErrorMessage={setErrorMessage} navigate={navigate} />} />
            <Route path="/email" element={<Email email={email} />} />
            <Route path="/editprofile" element={<EditProfile navigate={navigate} />} />
            <Route path="/profile" element={<Profile setErrorMessage={setErrorMessage} />} />
            <Route path="/profile/:id" element={<ProfilePage />} />
            <Route path="/chat/:friendEmail" element={<ChatPage />} />
          </Routes>
        </main>
      </div>
    </GoogleOAuthProvider>
  );
}

export default App;
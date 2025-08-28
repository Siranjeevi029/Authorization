import React from 'react';
import { GoogleLogin } from '@react-oauth/google';

const Login = ({ handleLogin, email, setEmail, password, setPassword, errorMessage, handleGoogleSignIn,setErrorMessage }) => {
  return (
    <div>
      <form onSubmit={handleLogin}>
        <label htmlFor="email">Email: </label>
        <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required />
        <label htmlFor="password">Password: </label>
        <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required />
        <button type="submit">Login</button>
      </form>
      <p>{errorMessage}</p>
      <div>
        <p>Or sign in with Google:</p>
        <GoogleLogin
          onSuccess={handleGoogleSignIn}
          onError={() => setErrorMessage('Google Sign-In failed')}
        />
      </div>
    </div>
  );
};

export default Login;
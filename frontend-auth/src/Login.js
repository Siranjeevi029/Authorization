import React from 'react';
import { GoogleLogin } from '@react-oauth/google';

const Login = ({ handleLogin, email, setEmail, password, setPassword, errorMessage, handleGoogleSignIn, setErrorMessage }) => {
  return (
    <div className="max-w-md mx-auto bg-white p-6 rounded-lg shadow-md">
      <h2 className="text-2xl font-bold text-secondary mb-4 text-center">Login</h2>
      <form onSubmit={handleLogin} className="space-y-4">
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700">Password</label>
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            className="w-full p-2 border border-gray-300 rounded-md focus:ring-primary focus:border-primary"
          />
        </div>
        <button
          type="submit"
          className="w-full bg-primary hover:bg-blue-700 text-white p-2 rounded-md transition"
        >
          Login
        </button>
      </form>
      {errorMessage && <p className="text-red-500 text-center mt-4">{errorMessage}</p>}
      <div className="mt-6 text-center">
        <p className="text-gray-600 mb-2">Or sign in with Google:</p>
        <GoogleLogin
          onSuccess={handleGoogleSignIn}
          onError={() => setErrorMessage('Google Sign-In failed')}
        />
      </div>
    </div>
  );
};

export default Login;
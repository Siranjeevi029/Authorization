import React from 'react';
import { GoogleLogin } from '@react-oauth/google';

const Login = ({ handleLogin, email, setEmail, password, setPassword, errorMessage, handleGoogleSignIn, setErrorMessage }) => {
  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center animate-fadeInUp">
          <div className="mx-auto h-16 w-16 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center mb-6">
            <span className="text-white font-bold text-2xl">S</span>
          </div>
          <h2 className="text-3xl font-bold gradient-text mb-2">Welcome Back</h2>
          <p className="text-gray-300 text-lg">Sign in to your SkillConnect account</p>
        </div>
        
        <div className="glass-morphism rounded-2xl p-8 space-y-6 animate-fadeInUp" style={{animationDelay: '0.2s'}}>
          <form onSubmit={handleLogin} className="space-y-6">
            <div className="space-y-4">
              <div>
                <label htmlFor="email" className="block text-sm font-semibold text-white mb-2">
                  Email Address
                </label>
                <input
                  id="email"
                  name="email"
                  type="email"
                  autoComplete="email"
                  required
                  className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  placeholder="Enter your email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                />
              </div>
              <div>
                <label htmlFor="password" className="block text-sm font-semibold text-white mb-2">
                  Password
                </label>
                <input
                  id="password"
                  name="password"
                  type="password"
                  autoComplete="current-password"
                  required
                  className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
              </div>
            </div>
            
            <button
              type="submit"
              className="w-full btn-gradient text-white font-semibold py-3 px-6 rounded-lg transition-all duration-300"
            >
              Sign In
            </button>
          </form>
          
          {errorMessage && (
            <div className="bg-red-900/50 border border-red-600 text-red-300 px-4 py-3 rounded-lg text-sm">
              {errorMessage}
            </div>
          )}
          
          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-red-600/30"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="px-4 bg-black/50 text-gray-300 font-medium">Or continue with</span>
            </div>
          </div>
          
          <div className="flex justify-center">
            <GoogleLogin
              onSuccess={handleGoogleSignIn}
              onError={() => setErrorMessage('Google Sign-In failed')}
              theme="outline"
              size="large"
              width="320"
            />
          </div>
          
          <div className="text-center">
            <p className="text-center text-sm text-gray-300 mt-6">
              Don't have an account?{' '}
              <a href="/register" className="font-semibold text-red-400 hover:text-red-300 transition-colors duration-300">
                Sign up here
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
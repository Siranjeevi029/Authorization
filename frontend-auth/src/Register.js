import React from 'react';

const Register = ({ handleRegister, email, setEmail, password, setPassword, errorMessage, isWaiting }) => {
  return (
    <div className="min-h-screen flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div className="text-center animate-fadeInUp">
          <div className="mx-auto h-16 w-16 bg-gradient-to-br from-red-600 to-red-800 rounded-xl flex items-center justify-center mb-6">
            <span className="text-white font-bold text-2xl">S</span>
          </div>
          <h2 className="text-3xl font-bold gradient-text mb-2">Create Account</h2>
          <p className="text-gray-300 text-lg">Join SkillConnect and find your perfect match</p>
        </div>
        
        <div className="glass-morphism rounded-2xl p-8 space-y-6 animate-fadeInUp" style={{animationDelay: '0.2s'}}>
          <form onSubmit={handleRegister} className="space-y-6">
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
                  autoComplete="new-password"
                  required
                  className="w-full px-4 py-3 border border-red-600/30 rounded-xl focus:ring-2 focus:ring-red-500 focus:border-red-500 transition-all duration-300 bg-black/50 backdrop-blur-sm text-white placeholder-gray-400"
                  placeholder="Create a password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                />
                <p className="text-gray-400 text-xs mt-1">Password should be at least 8 characters long</p>
              </div>
            </div>
            
            <button
              type="submit"
              disabled={isWaiting}
              className={`w-full font-semibold py-3 px-6 rounded-lg transition-all duration-300 ${
                isWaiting 
                  ? 'bg-gray-600 text-gray-400 cursor-not-allowed' 
                  : 'btn-gradient text-white hover:shadow-lg'
              }`}
            >
              {isWaiting ? 'Please Wait...' : 'Create Account'}
            </button>
          </form>
          
          {errorMessage && (
            <div className="bg-red-900/50 border border-red-600 text-red-300 px-4 py-3 rounded-lg text-sm">
              {errorMessage}
            </div>
          )}
          
          <div className="text-center">
            <p className="text-center text-sm text-gray-300 mt-6">
              Already have an account?{' '}
              <a href="/login" className="font-semibold text-red-400 hover:text-red-300 transition-colors duration-300">
                Sign in here
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Register;
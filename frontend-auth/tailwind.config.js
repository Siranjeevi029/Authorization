/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    "./src/**/*.{js,jsx}",
    "./public/index.html",
  ],
  theme: {
    extend: {
      colors: {
        primary: '#2563eb', // Custom primary color (blue)
        secondary: '#1f2937', // Dark gray for headers/backgrounds
        accent: '#10b981', // Green for buttons (e.g., Connect)
      },
    },
  },
  plugins: [],
};
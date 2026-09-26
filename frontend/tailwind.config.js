/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  theme: {
    extend: {
      colors: {
        ink: '#111a34',
        muted: '#74809b',
        canvas: '#f5f7fb',
        brand: { 50: '#eef2ff', 100: '#dfe6ff', 500: '#596df5', 600: '#4658e8', 700: '#3948ce' },
      },
      boxShadow: { card: '0 8px 30px rgba(22, 34, 65, .045)' },
      fontFamily: { sans: ['Inter', 'ui-sans-serif', 'system-ui', 'sans-serif'] },
    },
  },
  plugins: [],
}

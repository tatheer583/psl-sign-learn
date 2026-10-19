import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Relative base + hash routing: the built site works when opened from any
// folder (GitHub Pages project site, Netlify, a USB stick on a school PC).
export default defineConfig({
  base: './',
  plugins: [react()],
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1600,
  },
});

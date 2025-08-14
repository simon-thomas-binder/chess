import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        secure: false,
      },
      // If you use websocket plain ws (not SockJS), you can add:
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true
      }
    }
  }
});

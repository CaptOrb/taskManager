import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  server: {
    host: "0.0.0.0",
    port: 3000,
    proxy: {
      "/api": {
        target: "http://backend:8080",
        changeOrigin: true,
        secure: false,
      },
      "/ntfy": {
        target: "http://ntfy:80",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  plugins: [react()],
  base: "/",
});

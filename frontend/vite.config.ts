import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  server: {
    host: "0.0.0.0",
    port: 3000,
    proxy: {
      "/api": {
        target: process.env.API_TARGET ?? "http://localhost:8080",
        changeOrigin: true,
        secure: false,
      },
      "/ntfy": {
        target: process.env.NTFY_TARGET ?? "http://localhost:2586",
        changeOrigin: true,
        secure: false,
      },
    },
  },
  plugins: [react()],
  base: "/",
});

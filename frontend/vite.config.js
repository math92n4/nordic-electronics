import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export default defineConfig({
  plugins: [react()],
  root: __dirname,
  publicDir: path.resolve(__dirname, "public"),
  build: {
    outDir: path.resolve(__dirname, "../src/main/resources/static"),
    emptyOutDir: false, // Don't delete existing files like CSS
    assetsDir: "js", // Put JS files in js/ directory to match Spring Boot expectations
  },
  server: {
    port: 3000,
    host: "0.0.0.0",
    allowedHosts: true,
    proxy: {
      "/api": {
        target: process.env.VITE_API_URL || "http://spring-app:8080",
        changeOrigin: true,
      },
    },
  },
});

import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
const t = "http://localhost:8080";
export default defineConfig({
  plugins: [react()],
  server: { proxy: { "/api": t, "/actuator": t } },
  // Broad browser support (Safari 13+, Chrome/Edge 87+, Firefox 78+, modern mobile)
  build: { target: ["es2019", "chrome87", "firefox78", "safari13", "edge88"], cssTarget: ["chrome87", "safari13", "firefox78"], sourcemap: false, chunkSizeWarningLimit: 700 }
});

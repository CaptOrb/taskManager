import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}", // adjust this if your project structure is different
  ],
  theme: {
    extend: {},
  },
  plugins: [],
};

export default config;

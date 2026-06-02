# ===== STAGE 1: BUILD =====
# Node 20 to compile the React app
FROM node:20-alpine AS build

WORKDIR /app

# Copy dependency files first (caching — npm install won't re-run if package.json hasn't changed)
COPY package.json package-lock.json ./

# Install dependencies (ci = clean install, follows lock file exactly)
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build the static files (output goes to /app/dist)
RUN npm run build

# ===== STAGE 2: SERVE =====
# Nginx is a lightweight web server to serve the static files
FROM nginx:1.26-alpine

# Copy built files from stage 1 into Nginx's web root
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx config (handles SPA routing — all paths fallback to index.html)
COPY nginx/default.conf /etc/nginx/conf.d/default.conf

# Nginx default port
EXPOSE 80

# Start Nginx in foreground (daemon off = don't fork, stay in foreground so Docker can track the process)
CMD ["nginx", "-g", "daemon off;"]
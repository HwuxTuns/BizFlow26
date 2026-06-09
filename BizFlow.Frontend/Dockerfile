# Nginx for static frontend + reverse proxy to backend
FROM nginx:1.25-alpine
COPY conf/default.conf /etc/nginx/conf.d/default.conf
# Copy pages and assets as web root content
COPY pages /usr/share/nginx/html/pages
COPY assets /usr/share/nginx/html/assets
# Fix permissions for all files to be readable by nginx
RUN find /usr/share/nginx/html -type f -exec chmod 644 {} \; && \
    find /usr/share/nginx/html -type d -exec chmod 755 {} \;
EXPOSE 80

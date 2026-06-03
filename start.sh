#!/bin/sh

# 修复 nginx 配置
cat > /etc/nginx/conf.d/default.conf << 'EOF'
server {
    listen 80;
    root /usr/share/nginx/html;
    index index.html;
    location / {
        try_files $uri $uri/ /index.html;
    }
}
EOF
rm -f /etc/nginx/sites-enabled/default

# 启动 nginx
nginx

# 启动后端
java -jar /app/backend/app.jar --server.port=8080
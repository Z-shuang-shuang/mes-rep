#!/bin/sh

# 启动 nginx（前端在80端口）
nginx

# 启动后端（8080端口）
java -jar /app/backend/app.jar --server.port=8080
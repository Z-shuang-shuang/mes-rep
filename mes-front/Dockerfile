# # frontend/Dockerfile
# FROM node:22-alpine AS build

# WORKDIR /app

# # 复制依赖文件
# COPY package*.json ./

# # 安装依赖（使用阿里云镜像加速）
# RUN npm config set registry https://registry.npmmirror.com && \
#     npm install

# # 复制源码并构建
# COPY . .
# RUN npm run build

# # 生产阶段使用 nginx
# FROM nginx:alpine

# # 复制构建产物到 nginx 目录
# COPY --from=build /app/dist /usr/share/nginx/html

# # 复制 nginx 配置文件（可选）
# COPY nginx.conf /etc/nginx/conf.d/default.conf

# EXPOSE 80

# CMD ["nginx", "-g", "daemon off;"]

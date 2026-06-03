FROM nginx:stable

# 安装 JDK 17
RUN apt-get update && apt-get install -y openjdk-17-jre && rm -rf /var/lib/apt/lists/*

COPY --from=frontend-build /app/frontend/dist /usr/share/nginx/html
COPY --from=backend-build /app/backend/mes-admin/target/*.jar /app/backend/app.jar

# nginx 配置直接写进镜像
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY start.sh /start.sh
RUN chmod +x /start.sh
EXPOSE 80 8080
CMD ["/start.sh"]
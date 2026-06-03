FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY mes-front/package*.json ./
RUN npm install
COPY mes-front/ ./
RUN npm run build

FROM maven:3-eclipse-temurin-17 AS backend-build
WORKDIR /app/backend
RUN mkdir -p /root/.m2 && \
    echo '<?xml version="1.0" encoding="UTF-8"?><settings><mirrors><mirror><id>aliyun</id><mirrorOf>central</mirrorOf><name>aliyun</name><url>https://maven.aliyun.com/repository/public</url></mirror></mirrors></settings>' > /root/.m2/settings.xml
COPY mes/ ./
RUN mvn clean package -Dmaven.test.skip=true

FROM eclipse-temurin:17-jre
RUN apt-get update && apt-get install -y nginx && rm -rf /var/lib/apt/lists/*
COPY --from=frontend-build /app/frontend/dist /usr/share/nginx/html
COPY --from=backend-build /app/backend/mes-admin/target/*.jar /app/backend/app.jar
COPY start.sh /start.sh
RUN chmod +x /start.sh
EXPOSE 80 8080
CMD ["/start.sh"]
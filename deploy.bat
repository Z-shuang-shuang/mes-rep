@echo off
echo ========== 清理缓存 ==========
docker rmi mes-app:v1.0 -f 2>nul
echo ========== 构建镜像 ==========
docker build --no-cache -t mes-app:v1.0 .
echo ========== 推送镜像 ==========
docker tag mes-app:v1.0 crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-app:v1.0
docker push crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-app:v1.0
echo ========== 完成！30秒后服务器自动更新 ==========
pause
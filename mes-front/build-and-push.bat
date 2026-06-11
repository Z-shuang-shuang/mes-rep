# frontend/build-and-push.bat
@echo off
echo ========== 构建前端镜像 ==========
docker build -t mes-front:v1.0 .

echo ========== 标记镜像 ==========
docker tag mes-front:v1.0 crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-front:v1.0

echo ========== 推送镜像到阿里云 ==========
docker push crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-front:v1.0

echo ========== 完成！ ==========
echo 服务器拉取命令：docker pull crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-front:v1.0
echo 运行命令：docker run -d -p 80:80 --name mes-front mes-front:v1.0
pause
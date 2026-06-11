# deploy-frontend.sh（在服务器上执行）
#!/bin/bash

# 拉取最新镜像
docker pull crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-front:v1.0

# 停止并删除旧容器
docker stop mes-front 2>/dev/null
docker rm mes-front 2>/dev/null

# 运行新容器
docker run -d \
  --name mes-front \
  -p 80:80 \
  --restart=always \
  crpi-8w4iw1g4vwye5tsl.cn-shanghai.personal.cr.aliyuncs.com/mes-rep/mes-front:v1.0

echo "前端部署完成！"
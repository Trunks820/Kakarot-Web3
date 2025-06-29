FROM node:18 AS frontend-builder

# 构建前端
WORKDIR /app/frontend
COPY RuoYi-Vue3/package*.json ./
RUN npm install
COPY RuoYi-Vue3/ ./
RUN npm run build:prod

FROM openjdk:17-jdk-bullseye AS builder

# 补齐字体依赖和Maven
RUN apt-get update && apt-get install -y maven fontconfig fonts-dejavu-core libfreetype6 && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 复制前端构建结果到静态资源目录
COPY --from=frontend-builder /app/frontend/dist ./ruoyi-admin/src/main/resources/static/

# Maven构建Java项目
# RUN mvn clean package -DskipTests

# =================
# 第二阶段运行镜像
# =================
FROM openjdk:17-jdk-bullseye

WORKDIR /app

# 复制构建好的jar包
COPY --from=builder /app/ruoyi-admin/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/app.jar", "--spring.profiles.active=druid"]

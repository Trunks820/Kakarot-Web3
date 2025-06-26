FROM openjdk:17-jdk-bullseye AS builder

# 补齐字体依赖和Maven
RUN apt-get update && apt-get install -y maven fontconfig fonts-dejavu-core libfreetype6 && rm -rf /var/lib/apt/lists/*

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

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

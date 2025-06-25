FROM openjdk:17-jdk-alpine AS builder

# 安装Maven
RUN apk add --no-cache maven

# 设置工作目录
WORKDIR /app

# 复制项目文件
COPY . .

# 构建项目
RUN mvn clean package -DskipTests

# 运行时镜像
FROM openjdk:17-jre-alpine
WORKDIR /app

# 复制构建好的jar包
COPY --from=builder /app/ruoyi-admin/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-Xms512m", "-Xmx1024m", "-jar", "/app/app.jar", "--spring.profiles.active=druid"]
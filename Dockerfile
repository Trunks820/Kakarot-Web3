# ========= 前端构建 =========
FROM node:18-bookworm-slim AS frontend-builder

WORKDIR /app/frontend
# 先只拷贝依赖文件，利用缓存
COPY RuoYi-Vue3/package*.json ./
# 用 ci 比 install 更稳定（若你的项目必须用 install 再改回）
RUN npm ci
# 再拷贝源码
COPY RuoYi-Vue3/ .
# 产物在 /app/frontend/dist
RUN npm run build:prod

# ========= 后端构建（含 Maven + JDK17） =========
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# 先拷贝 Maven 元数据利用缓存（可选：拆分子模块POM以更好缓存）
COPY pom.xml .
COPY ruoyi-admin/pom.xml ruoyi-admin/pom.xml
# 预拉依赖（没有源码也能拉缓存）
RUN mvn -q -e -U -DskipTests package || true

# 再拷源码
COPY . .
# 把前端构建结果塞进静态资源目录（和你原来一致）
COPY --from=frontend-builder /app/frontend/dist ./ruoyi-admin/src/main/resources/static/

# 真正打包
RUN mvn -q -DskipTests clean package

# ========= 运行阶段（JRE更轻） =========
FROM eclipse-temurin:17-jre-jammy

ENV TZ=Asia/Shanghai
WORKDIR /app

# 如需中文/字体渲染再装字体；一般后端不用可去掉
RUN apt-get update && apt-get install -y --no-install-recommends \
    fontconfig fonts-dejavu-core libfreetype6 \
 && rm -rf /var/lib/apt/lists/*

# 拷贝可执行 jar
COPY --from=builder /app/ruoyi-admin/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java","-Xms512m","-Xmx1024m","-jar","/app/app.jar","--spring.profiles.active=druid"]

# ==================== 阶段一：Maven 构建（所有模块共用） ====================
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# 先复制所有 pom.xml，利用 Docker 层缓存加速依赖下载
# 只有 pom.xml 变动时才重新下载依赖
COPY pom.xml .
COPY backend-common/pom.xml backend-common/
COPY backend-main/pom.xml backend-main/
COPY backend-sandbox/pom.xml backend-sandbox/

RUN mvn dependency:go-offline -B

# 复制所有模块源码
# backend-main 依赖了 backend-sandbox 的类（DockerExitCodeConstants 等）
# 必须全部模块一起编译
COPY backend-common/src backend-common/src
COPY backend-main/src backend-main/src
COPY backend-sandbox/src backend-sandbox/src

# 一次性打包所有模块，跳过测试
RUN mvn clean package -DskipTests -B

# ==================== 阶段二-A：backend-main 运行镜像 ====================
FROM eclipse-temurin:17-jre-alpine AS main

WORKDIR /app
COPY --from=builder /build/backend-main/target/*.jar app.jar

ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseContainerSupport"
EXPOSE 8802
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# ==================== 阶段二-B：backend-sandbox 运行镜像 ====================
FROM eclipse-temurin:17-jre-alpine AS sandbox

WORKDIR /app
COPY --from=builder /build/backend-sandbox/target/*.jar app.jar

# sandbox 需要访问宿主机 Docker socket（见 docker-compose.yml volumes）
ENV JAVA_OPTS="-Xms128m -Xmx256m -XX:+UseContainerSupport"
EXPOSE 8801
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

# 1. 基础镜像：还是用你喜欢的那个轻量级 Java 8
FROM bellsoft/liberica-openjdk-alpine:8

# 2. 安装 GNU time 工具 (这是为了解决你统计内存的问题)
# apk 是 Alpine Linux 的安装命令，类似 yum 或 apt
RUN apk update && apk add --no-cache time

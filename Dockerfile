# ============================================================
# 第一阶段：构建
# ============================================================
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Maven 缓存
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================================
# 第二阶段：运行
# ============================================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 创建非 root 用户（安全最佳实践）
RUN groupadd -r appgroup && useradd -r -g appgroup appuser

# 复制构建产物
COPY --from=builder /app/target/*.jar app.jar

# 复制建表脚本（容器启动时自动执行）
COPY src/main/resources/schema.sql /app/schema.sql

# 切换到非 root 用户
USER appuser

# 暴露端口
EXPOSE 8080

# 启动命令：自动建库 + 启动应用
ENTRYPOINT ["sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar app.jar"]

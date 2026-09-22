# ==========================================
# STAGE 1: Build application
# ==========================================
FROM eclipse-temurin:25-jdk-alpine AS builder
WORKDIR /app

RUN apk add --no-cache maven

# Copy root pom.xml và toàn bộ mã nguồn của Monorepo vào container
COPY . .

# SERVICE_NAME truyền vào từ docker-compose (ví dụ: auth-service)
ARG SERVICE_NAME

# Build module chỉ định, cờ -am (also-make) đảm bảo module 'common' sẽ được build trước
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# ==========================================
# STAGE 2: Runtime image (Tối ưu dung lượng)
# ==========================================
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

ARG SERVICE_NAME

# Tạo user không phải root để tăng tính bảo mật cho container
RUN addgroup -S springgroup && adduser -S springuser -G springgroup

# Copy file .jar đã build từ stage trước
COPY --from=builder /app/${SERVICE_NAME}/target/*.jar app.jar

USER springuser:springgroup

# Biến môi trường JVM tối ưu cho Java 25
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+UseStringDeduplication"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
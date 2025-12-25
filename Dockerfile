FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY target/notification-1.0.0.jar /app/notification-1.0.0.jar

EXPOSE 8088

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8088/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "/app/notification-1.0.0.jar"]
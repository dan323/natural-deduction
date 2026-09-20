FROM eclipse-temurin:21-alpine

# Run as an unprivileged user instead of root.
RUN addgroup -S app && adduser -S -G app app

COPY executable/target/*.jar app.jar
USER app

EXPOSE 8080

# /actuator/health is exposed by Spring Boot's default actuator web exposure, so nothing extra is enabled for this.
# busybox wget (part of the alpine base image) exits non-zero on the 503 that a DOWN status returns.
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:8080/actuator/health || exit 1

ENTRYPOINT ["java","-jar","/app.jar"]

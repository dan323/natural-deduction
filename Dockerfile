FROM eclipse-temurin:21-alpine
VOLUME /tmp
COPY executable/target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
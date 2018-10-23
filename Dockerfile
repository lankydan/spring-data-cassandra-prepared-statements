FROM openjdk:11-jre-slim
LABEL maintainer="Dan Newton"
ARG JAR_FILE
ADD target/${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
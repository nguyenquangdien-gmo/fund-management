FROM openjdk:17-jdk-slim
ENV TZ="Asia/Ho_Chi_Minh"
WORKDIR /app
COPY target/fund_management-0.0.1-SNAPSHOT.jar /app/app.jar
EXPOSE 8080 5005
ENTRYPOINT ["java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005", "-Dspring.profiles.active=prod", "-jar", "app.jar"]

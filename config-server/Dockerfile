FROM eclipse-temurin:21-jre-focal
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
ENV JAVA_OPTS="-Xms128m -Xmx512m"
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app.jar"]
EXPOSE 8888

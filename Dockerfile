FROM eclipse-temurin
#ARG JAR_FILE=target/*.jar
#COPY ${JAR_FILE} app.jar
ARG KS_PW=$KS_PW
COPY build/libs/SecureStore-1.0-SNAPSHOT.jar app.jar
COPY server.crt server.crt
RUN keytool -importcert -trustcacerts -file server.crt -keystore /opt/java/openjdk/lib/security/cacerts -storepass ${KS_PW} -noprompt -alias server
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
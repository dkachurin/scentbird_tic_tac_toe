# temp container to build using gradle
FROM gradle:8.7.0 AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle build


# actual container
FROM amazoncorretto:21
ENV ARTIFACT_NAME=xogameserver-27-may-10-39-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/build/libs/$ARTIFACT_NAME .
EXPOSE 8080
ENTRYPOINT exec java -jar ${ARTIFACT_NAME}
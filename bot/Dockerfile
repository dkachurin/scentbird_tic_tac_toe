# temp container to build using gradle
FROM gradle:8.7.0 AS BUILD
WORKDIR /usr/app/
COPY . .
RUN gradle build


# actual container
FROM amazoncorretto:21
ENV ARTIFACT_NAME=xogamebot-27-may-15-16-SNAPSHOT.jar
ENV APP_HOME=/usr/app/
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/build/libs/$ARTIFACT_NAME .
EXPOSE 8083
ENTRYPOINT exec java -jar ${ARTIFACT_NAME} startRobotsCount=6
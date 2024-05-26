FROM amazoncorretto:21
VOLUME /dktmp
COPY build/libs/xogamebot-26-may-17-43-SNAPSHOT.jar xogamebot-26-may-17-43.jar
ENTRYPOINT ["java","-jar","/xogamebot-26-may-17-43.jar", "game.server.url=${SERVER_URL}"]
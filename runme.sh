#!/bin/sh

# build code
cd server || exit
./gradlew build
cd ../bot || exit
./gradlew build

# start the containers stack
cd ../
docker-compose up -d

# wait for the service to be ready
while ! curl --fail --silent --head http://localhost:8080/index.html; do
  sleep 1
done

# open the browser window
open http://localhost:8080/index.html
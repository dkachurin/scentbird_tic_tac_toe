#!/bin/sh
docker-compose up -d

URL="http://localhost:8080/index.html"
TIMEOUT=60  # 1 minute timeout
INTERVAL=1  # Check every 1 second
ELAPSED=0

while [ $ELAPSED -lt $TIMEOUT ]; do
  if curl --output /dev/null --silent --head --fail "$URL"; then
    # Open the URL in the default web browser
    xdg-open "$URL" &> /dev/null  # For Linux
    open "$URL" &> /dev/null      # For macOS
    start "$URL" &> /dev/null     # For Windows (if running in a compatible environment)
    exit 0
  fi
  sleep $INTERVAL
  ELAPSED=$((ELAPSED + INTERVAL))
done

echo "
  Sorry, http://localhost:8080/index.html is not responding.
  Try to run docker compose yourself and open url in browser manually.
"
exit 1

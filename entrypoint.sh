#!/bin/sh -e

echo "Starting post-service..."
java -version
exec java -jar /apps/app.jar
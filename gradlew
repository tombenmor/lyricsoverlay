#!/usr/bin/env sh
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
if [ -f "$DIR/gradle/wrapper/gradle-wrapper.jar" ]; then
  exec java -jar "$DIR/gradle/wrapper/gradle-wrapper.jar" "$@"
else
  echo "gradle-wrapper.jar not found. Please generate the wrapper JAR by running 'gradle wrapper --gradle-version 8.3' with a local Gradle, or let your IDE create the wrapper."
  exit 1
fi

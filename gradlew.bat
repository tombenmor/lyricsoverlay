@echo off
REM ----------------------------------------------------------------------------
REM Gradle Start Up Script for Windows
REM ----------------------------------------------------------------------------
if exist "%~dp0\gradle\wrapper\gradle-wrapper.jar" (
  java -jar "%~dp0\gradle\wrapper\gradle-wrapper.jar" %*
) else (
  echo gradle-wrapper.jar not found. Please generate the wrapper JAR by running "gradle wrapper --gradle-version 8.3" with a local Gradle, or let your IDE create the wrapper.
  exit /b 1
)

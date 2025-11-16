@echo off
REM Stok Takip Backend Build Script for Windows

echo Building Stok Takip Backend...

REM Clean previous builds
echo Cleaning previous builds...
call mvn clean

REM Run tests
echo Running tests...
call mvn test

REM Build JAR
echo Building JAR file...
call mvn package -DskipTests

echo Build completed successfully!
echo JAR file location: target\stoktakip-backend-0.0.1-SNAPSHOT.jar

pause


#!/bin/bash
# Render build script for backend

set -e

echo "Building Stok Takip Backend..."

# Install Maven if not available
if ! command -v mvn &> /dev/null; then
    echo "Maven not found, installing..."
    # Render provides Maven, but if needed:
    # wget https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz
    # tar -xzf apache-maven-3.9.6-bin.tar.gz
    # export PATH=$PATH:$(pwd)/apache-maven-3.9.6/bin
fi

# Build the project
cd backend
mvn clean package -DskipTests

echo "Build completed successfully!"
echo "JAR location: backend/target/stoktakip-backend-0.0.1-SNAPSHOT.jar"


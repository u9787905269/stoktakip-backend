#!/bin/bash

# Stok Takip Backend Build Script

set -e

echo "Building Stok Takip Backend..."

# Clean previous builds
echo "Cleaning previous builds..."
mvn clean

# Run tests
echo "Running tests..."
mvn test

# Build JAR
echo "Building JAR file..."
mvn package -DskipTests

echo "Build completed successfully!"
echo "JAR file location: target/stoktakip-backend-0.0.1-SNAPSHOT.jar"


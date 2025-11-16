#!/bin/bash
# Render start script for backend

set -e

cd backend
java -jar -Dspring.profiles.active=prod target/stoktakip-backend-0.0.1-SNAPSHOT.jar


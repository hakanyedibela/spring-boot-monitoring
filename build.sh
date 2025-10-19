#!/bin/bash
set -e

IMAGE_NAME="spring-boot-monitoring"
IMAGE_TAG="0.0.1"

echo "🏗️  Building application..."
./mvnw clean package -DskipTests

echo "📦 Building container image..."
pack build ${IMAGE_NAME}:${IMAGE_TAG} \
  --builder paketobuildpacks/builder:base \
  --path . \
  --env BP_JVM_VERSION=17

echo "📤 Loading image to Minikube..."
minikube image load ${IMAGE_NAME}:${IMAGE_TAG}

echo "🔍 Verifying image in Minikube..."
minikube image ls | grep ${IMAGE_NAME}

echo "✅ Image loaded successfully!"
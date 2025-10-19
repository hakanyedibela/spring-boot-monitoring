#!/bin/bash
set -e

IMAGE_NAME="spring-boot-monitoring"
IMAGE_TAG="0.0.1"
NAMESPACE="test"

echo "🚀 Starting Minikube workflow..."

if ! minikube status > /dev/null 2>&1; then
  echo "⚠️  Minikube is not running. Starting..."
  minikube start
fi

echo "📦 Creating namespace..."
kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

echo "🏗️  Building application..."
./mvnw clean package -DskipTests

echo "📦 Building container image..."
pack build ${IMAGE_NAME}:${IMAGE_TAG} \
  --builder paketobuildpacks/builder:base \
  --path . \
  --env BP_JVM_VERSION=17 \
  --env BP_MAVEN_BUILD_ARGUMENTS="clean package -DskipTests"

echo "📤 Loading image to Minikube..."
minikube image load ${IMAGE_NAME}:${IMAGE_TAG}

echo "🔍 Verifying image in Minikube..."
if minikube image ls | grep -q "${IMAGE_NAME}:${IMAGE_TAG}"; then
  echo "✅ Image found in Minikube"
else
  echo "❌ Image not found in Minikube"
  exit 1
fi

echo "🚀 Deploying to Kubernetes..."
kubectl apply -k container/k8s/overlays/dev

echo "⏳ Waiting for deployment to be ready..."
kubectl rollout status deployment/hkn-observability -n ${NAMESPACE} --timeout=300s

echo ""
echo "📊 Deployment Status:"
kubectl get pods -n ${NAMESPACE} -l app=hkn-observability

echo ""
echo "🔗 Service Status:"
kubectl get svc -n ${NAMESPACE}

echo ""
echo "✅ Deployment complete!"
echo ""
echo "📝 Useful commands:"
echo "  View logs:        kubectl logs -f deployment/hkn-observability -n ${NAMESPACE}"
echo "  Port forward:     kubectl port-forward svc/hkn-observability 8080:8080 -n ${NAMESPACE}"
echo "  Get service URL:  minikube service hkn-observability -n ${NAMESPACE} --url"
echo "  Shell into pod:   kubectl exec -it deployment/hkn-observability -n ${NAMESPACE} -- /bin/sh"
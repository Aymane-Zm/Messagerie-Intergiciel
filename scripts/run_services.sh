#!/bin/bash
echo "demarrage des conteneur Docker"
cd "$(dirname "$0")/.."

docker-compose up -d
sleep 5

echo "lancement du per-translate-service..."
cd per-translate-service
./mvnw clean package -DskipTests
cd target
nohup java -jar per-translate-service-0.0.1-SNAPSHOT.jar > per-translate-service.log 2>&1 &
cd ../..

echo "Compilation et lancement de client-cons-db..."
cd client-cons-db
./mvnw clean package -DskipTests
cd target
nohup java -jar client-cons-db-0.0.1-SNAPSHOT.jar > client-cons-db.log 2>&1 &
cd ../..

echo "les services sont lancés !lancer les clients CLI"
echo "Pour ClientA : ./scripts/run_client_A.sh ou B : ./scripts/run_client_B.sh"
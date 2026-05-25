#!/bin/bash
echo " Arret des services Spring Boot"

pkill -f per-translate-service-0.0.1-SNAPSHOT.jar
pkill -f client-cons-db-0.0.1-SNAPSHOT.jar

rm -f per-translate-service/target/per-translate-service.log
rm -f client-cons-db/target/client-cons-db.log

echo "Arret de Docker"
cd "$(dirname "$0")/.."
docker-compose down

echo "arreté "
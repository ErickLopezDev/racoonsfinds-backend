#!/bin/bash

# Detener y eliminar containers existentes
docker compose down --volumes --remove-orphans

# Limpiar images no utilizadas
docker system prune -f

# Construir e iniciar servicios
docker compose build
docker compose up -d


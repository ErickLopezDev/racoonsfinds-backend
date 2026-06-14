# syntax=docker/dockerfile:1

# ─────────────────────────────────────────────
# Etapa de construcción
# ─────────────────────────────────────────────
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

# Cachear dependencias primero: solo se re-ejecuta si cambia pom.xml
COPY pom.xml .
RUN mvn -B dependency:go-offline

# Compilar la aplicación
COPY src ./src
RUN mvn -B clean package -DskipTests

# Renombrar a un nombre estable (independiente de la versión) y extraer las
# capas de Spring Boot para cachear mejor en runtime
RUN cp target/*.jar application.jar \
    && java -Djarmode=tools -jar application.jar extract --layers --destination extracted

# ─────────────────────────────────────────────
# Etapa de runtime
# ─────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /application

# Usuario sin privilegios
RUN groupadd --system spring && useradd --system --gid spring spring
USER spring:spring

# Copiar las capas por separado: las que menos cambian primero (mejor cache)
COPY --from=build /app/extracted/dependencies/ ./
COPY --from=build /app/extracted/spring-boot-loader/ ./
COPY --from=build /app/extracted/snapshot-dependencies/ ./
COPY --from=build /app/extracted/application/ ./

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "application.jar"]

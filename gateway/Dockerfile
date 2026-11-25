# Etapa de build: compila e empacota o jar do gateway
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Baixa as dependencias primeiro para aproveitar o cache de camadas
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# Etapa de runtime: imagem enxuta so com o JRE e o jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

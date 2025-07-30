# Stage de Build: Usa uma imagem com Maven e JDK para compilar o projeto
# Mudando para uma tag Maven mais genérica devido a problemas persistentes de "not found"
FROM maven:3-openjdk-17 AS build

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o pom.xml e baixa as dependências do Maven.
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o código fonte da aplicação
COPY src ./src

# Compila e empacota a aplicação Spring Boot em um JAR executável
RUN mvn clean package -DskipTests

# Stage de Execução: Usa uma imagem mínima para rodar a aplicação
# Mudando para uma tag OpenJDK JRE mais genérica também.
FROM openjdk:17-slim

# Metadados opcionais para a imagem
LABEL authors="Luciane"

# Define o diretório de trabalho para a aplicação no contêiner final
WORKDIR /app

# Copia o JAR construído do stage 'build' para o stage 'run'
COPY --from=build /app/target/atletahub-backend-0.0.1-SNAPSHOT.jar app.jar

# Expõe a porta que sua aplicação Spring Boot escuta (padrão 8080)
EXPOSE 8080

# Comando para iniciar a aplicação quando o contêiner for executado
ENTRYPOINT ["java", "-jar", "app.jar"]
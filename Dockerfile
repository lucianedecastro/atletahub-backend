# --- Estágio 1: Build (Construção) ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Baixa as dependências numa camada própria: só refaz quando o pom.xml muda (builds bem mais rápidos).
# O "|| true" garante que, se algum plugin não resolver offline, o build normal abaixo continua funcionando.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline || true

COPY src ./src
RUN mvn -B clean package -DskipTests

# --- Estágio 2: Runtime (Execução) ---
# JRE (sem compilador) = imagem menor
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Não roda como root dentro do container
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/*.jar app.jar
USER app

EXPOSE 8080

# Limita a heap a 70% da memória do container (o plano free do Render tem pouca RAM)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=70 -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

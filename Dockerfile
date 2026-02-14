# Multi-stage build para otimizar tamanho da imagem

# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# Copiar apenas pom.xml primeiro para cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fonte e buildar
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

# Instalar dependências necessárias
RUN apk add --no-cache curl unzip

WORKDIR /app

# Download e instalação do New Relic Java Agent
RUN curl -L -o newrelic-java.zip https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip && \
    unzip newrelic-java.zip && \
    rm newrelic-java.zip

# Copiar JAR do stage de build
COPY --from=build /app/target/*.jar app.jar

# Copiar configuração do New Relic
COPY src/main/resources/newrelic.yml newrelic/newrelic.yml

# Criar usuário não-root
RUN addgroup -g 1001 appgroup && \
    adduser -D -u 1001 -G appgroup appuser && \
    chown -R appuser:appgroup /app

# Trocar para usuário não-root
USER appuser

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8081/actuator/health || exit 1

# Expor porta
EXPOSE 8081

# Instrução para iniciar o app com New Relic Agent
CMD ["java", "-javaagent:/app/newrelic/newrelic.jar", "-Xms512m", "-Xmx1024m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar"]

# ==============================================================================
# Dockerfile — BattleShip2 (Batalha Naval)
# Build: docker build -t battleship-game:latest .
# Run:   docker run --name battleship-container battleship-game:latest
# ==============================================================================

# ── Stage 1: Build ─────────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /build

# Copiar pom.xml primeiro para aproveitar cache de camadas Docker
COPY pom.xml .
RUN mvn dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Copiar código-fonte e compilar
COPY src ./src
RUN mvn clean package -DskipTests -B --no-transfer-progress

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

LABEL maintainer="IGE-123012 BattleShip2 Team"
LABEL org.opencontainers.image.title="BattleShip2"
LABEL org.opencontainers.image.description="Batalha Naval — Jogo Java com IA"
LABEL org.opencontainers.image.version="2.0"

WORKDIR /app

# Criar utilizador não-root para segurança
RUN addgroup --system battleship && adduser --system --ingroup battleship battleship

# Copiar apenas o JAR compilado do stage de build
COPY --from=builder /build/target/BattleshipGamePlayer-2.0.jar app.jar

RUN chown battleship:battleship app.jar

USER battleship

# Força o Log4j a saltar a deteção de contexto que quebra no ambiente do Docker
ENTRYPOINT ["java", \
  "-Dlog4j2.loggerContextFactory=org.apache.logging.log4j.simple.SimpleLoggerContextFactory", \
  "-cp", "app.jar", \
  "battleship.Main"]
# ==============================================================================
# DevContainer Dockerfile — BattleShip2
# Ambiente: Java 21 (Temurin) + Maven 3.9
# Garante: mvn clean compile && mvn test sem erros
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21

# Instalar ferramentas úteis de desenvolvimento
RUN apt-get update && apt-get install -y \
    git \
    curl \
    wget \
    unzip \
    vim \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

# Definir directório de trabalho
WORKDIR /workspace/BattleShip2

# Copiar o pom.xml primeiro para aproveitar a cache de camadas do Docker
# e pré-descarregar as dependências Maven
COPY pom.xml ./
RUN mvn dependency:go-offline -B --no-transfer-progress 2>/dev/null || true

# Variáveis de ambiente úteis
ENV MAVEN_OPTS="-Xmx1024m"
ENV JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"

# Confirmar versões instaladas no build
RUN java -version && mvn -version
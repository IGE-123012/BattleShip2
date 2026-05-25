FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY target/BattleshipGamePlayer-2.0.jar app.jar

# Força o Log4j a saltar a deteção de contexto que quebra no ambiente do Docker
ENTRYPOINT ["java", "-Dlog4j2.loggerContextFactory=org.apache.logging.log4j.simple.SimpleLoggerContextFactory", "-cp", "app.jar", "battleship.Main"]
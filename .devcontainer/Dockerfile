FROM ubuntu:latest
LABEL authors="marga"

FROM maven:3.9-eclipse-temurin-21

RUN apt-get update && apt-get install -y git

ENTRYPOINT ["top", "-b"]
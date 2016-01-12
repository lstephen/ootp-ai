FROM maven:3-jdk-8
MAINTAINER Levi Stephen <levi.stephen@gmail.com>

COPY . /usr/src/ootp-ai

WORKDIR /usr/src/ootp-ai

RUN mvn -B clean install -Dgpg.skip=true

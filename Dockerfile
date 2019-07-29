FROM ubuntu:16.04

SHELL ["/bin/bash", "-c"]

ARG javaversion=zulu12.3.11-ca-jdk12.0.2-linux_x64

RUN apt-get update
RUN apt-get -qq -y install \
    curl \
    wget \
    unzip \
    zip

RUN wget https://cdn.azul.com/zulu/bin/$javaversion.tar.gz

RUN mkdir /usr/lib/jvm
RUN cd /usr/lib/jvm
RUN tar -xzf /$javaversion.tar.gz -C /usr/lib/jvm/

ENV JAVA_HOME /usr/lib/jvm/$javaversion




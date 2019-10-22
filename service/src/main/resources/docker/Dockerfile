#FROM ubuntu:18.04
FROM ubuntu:latest

#ARG OPENJDK_TAG=8u212
ARG OPENJDK_TAG=latest
FROM openjdk:${OPENJDK_TAG}

ARG SBT_VERSION=1.2.8

RUN apk update && apk upgrade

WORKDIR /opt/kafka-pipeline
COPY kafka-pipeline-assembly.jar ./lib/

RUN ( JARFILE=./lib/kafka-pipeline-assembly.jar && mkdir -p lib/ logs/ && unzip $JARFILE 'conf/*' 'scripts/*' )

COPY local-rc.sh ./conf/


ENTRYPOINT ./scripts/run-svc.sh
CMD --help


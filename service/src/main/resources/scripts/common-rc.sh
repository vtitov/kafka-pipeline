#!/bin/sh


SCRIPTSDIR=`dirname $0`


if [ -d ${SCRIPTSDIR}/../lib/ ] ; then
  # prod setup
  #echo PROD
  BASEDIR=$(realpath --relative-to . ${SCRIPTSDIR}/..)
  JARDIR=${BASEDIR}/lib/
  #JARDIR=./lib/ # FIXME remove
elif [ -d ${SCRIPTSDIR}/../../../main/resources/ ] ; then
  # git repo setup
  #echo DEV
  BASEDIR=$(realpath --relative-to . ${SCRIPTSDIR}/../../../../..)
  JARDIR=${BASEDIR}/kafka-pipeline/target/scala-2.12/
else
  { echo "Bad setup no jar to execute" ; exit 1;}
fi
JARFILE=`ls -td ${JARDIR}/kafka-pipeline-assembly*.jar | head -n1`

LOGDIR=${BASEDIR}/logs
PIDDIR=${BASEDIR}/logs
CONFIGDIR=${SCRIPTSDIR}/../conf
LOGBACKFILE=${CONFIGDIR}/logback-template.xml
APPCONFIGFILE=${CONFIGDIR}/application-template.conf

app_main_class_cli="com.github.vtitov.kafka.pipeline.cli.CliMain"


LOCALRC=${BASEDIR}/conf/local-rc.sh
[[ -r ${LOCALRC} ]] && source ${LOCALRC}

#[[ -n ${bootstrap_servers} ]] && JAVA_OPTIONS="${JAVA_OPTIONS} -Dbootstrap.servers=${bootstrap_servers}"

JAVA_OPTIONS="${JAVA_OPTIONS}
  -Dlogback.configurationFile=${LOGBACKFILE}
  -Dconfig.file=${APPCONFIGFILE}
  -Dlog.dir=${LOGDIR}
"
export JAVA_OPTIONS

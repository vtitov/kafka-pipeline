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

JMX_PORT=9999
JMX_AUTH=false # FIXME: insecure
JMX_SSL=false

JMX_ACCESS=${CONFIGDIR}/jmxremote.access
JMX_PASS=${CONFIGDIR}/jmxremote.password

LOCALRC=${BASEDIR}/conf/local-rc.sh
[[ -r ${LOCALRC} ]] && source ${LOCALRC}

#[[ -n ${bootstrap_servers} ]] && JAVA_OPTIONS="${JAVA_OPTIONS} -Dbootstrap.servers=${bootstrap_servers}"

# test JMX_AUTH, pass authenticate if false
#JAVA_OPTIONS="${JAVA_OPTIONS} -Dcom.sun.management.jmxremote.authenticate=false"
# test JMX_AUTH, if true
JAVA_OPTIONS="${JAVA_OPTIONS}
  -Dcom.sun.management.jmxremote.password.file=${JMX_ACCESS}
  -Dcom.sun.management.jmxremote.access.file=${JMX_PASS}
"

JAVA_OPTIONS="${JAVA_OPTIONS}
  -Dcom.sun.management.jmxremote.port=${JMX_PORT}
  -Dcom.sun.management.jmxremote.authenticate=${JMX_AUTH}
  -Dcom.sun.management.jmxremote.ssl=${JMX_SSL}
"
JAVA_OPTIONS="${JAVA_OPTIONS}
  -Dlogback.configurationFile=${LOGBACKFILE}
  -Dconfig.file=${APPCONFIGFILE}
  -Dlog.dir=${LOGDIR}
"

export JAVA_OPTIONS

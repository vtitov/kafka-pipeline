#!/bin/bash

. "$(dirname $0)/common-rc.sh"

for cmd in "$@" ; do
  java \
    ${JAVA_OPTIONS} \
    -Dlog.name=${cmd} \
    -cp ${JARFILE} \
    ${app_main_class_cli} \
    ${cmd} >>${LOGDIR}/${cmd}.out 2>>${LOGDIR}/${cmd}.err & echo "$!" >> ${PIDDIR}/${cmd}.pid
done

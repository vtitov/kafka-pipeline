#!/bin/bash


. "$(dirname $0)/common-rc.sh"

RUNNING_SERVICES=`ls -d ${PIDDIR}/*.pid 2> /dev/null`
[[ -z ${RUNNING_SERVICES} ]] && { echo no running services found, exitting... ; exit 2; }

echo running services found: ${RUNNING_SERVICES}

RUNNING_PROCESSES=`cat ${RUNNING_SERVICES}`
#RUNNING_PROCESSES=`cat ${RUNNING_SERVICES} | tr '[\t\r\n]' ' '`
#RUNNING_PROCESSES=`cat ${RUNNING_SERVICES} | tr '[:space:]' ' '`
#RUNNING_PROCESSES_RAW=`cat ${RUNNING_SERVICES}`
#RUNNING_PROCESSES=${RUNNING_PROCESSES_RAW//[$'\t\r\n']}
echo "killing process: "${RUNNING_PROCESSES}
kill ${RUNNING_PROCESSES} && rm -rf ${RUNNING_SERVICES}

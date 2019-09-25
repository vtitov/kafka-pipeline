#!/bin/bash

BASEURL="$1"
[[ -z ${BASEURL} ]] && BASEURL="http://localhost:8089"

curl -s \
   -H "Content-Type: application/json" \
  -d '{"files":["a"]}' \
  ${BASEURL}/v1/api/rest/integration/send/files

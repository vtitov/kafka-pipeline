#!/bin/bash

BASEURL="$1"
[[ -z ${BASEURL} ]] && BASEURL="http://localhost:8089"

curl -s \
  ${BASEURL}/v1/api/info

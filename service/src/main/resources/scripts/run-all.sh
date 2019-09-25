#!/bin/bash

for cmd in embedded-kafka http streams remote-emulator ; do
  "$(dirname $0)/run-svc.sh" ${cmd}
done

#!/usr/bin/env bash
set -e
export GS_CLI_VERBOSE=true

java -jar gsctl.jar destroy

rm -rf gsctl.jar cluster.yaml services.yaml .gsctl/ logs/ *.json
#rm -rf cluster.yaml services.yaml .gsctl/ logs/ *.json

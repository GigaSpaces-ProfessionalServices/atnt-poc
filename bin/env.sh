#!/usr/bin/env bash

export GSCTL_VERSION=15.5.0
export GS_LICENSE=tryme
export GS_CLI_VERBOSE=true



echo "----============-----"
if [[ -d ".gsctl/" ]]; then
    service=$(java -jar gsctl.jar list-services | grep ops-manager-0)
    export MANAGER_REST=${service:15}
    token_line=$(grep secretId .gsctl/token.yaml | sed s/\"//g)
    export TOKEN=${token_line:10}

    echo "MANAGER_REST = ${MANAGER_REST}"
    echo "TOKEN = ${TOKEN}"
fi

function deploy_dynamic_space {
  local puName="$1"
  local resource="$2"
  echo -e "Deploying space $puName..\n"

  cat > template.json <<EOF
{
     "name": "${puName}",
     "resource": "${resource}",
     "topology": {
       "schema": "partitioned",
       "partitions": 2,
       "backupsPerPartition": 1
     },
     "contextProperties": {
       "license": "${GS_LICENSE}",
       "pu.dynamic-partitioning": "true"
     }
   }
EOF
  #echo "curl -X POST --insecure --silent --header 'Content-Type: application/json' --header 'Accept: text/plain' -u gs-admin:${TOKEN} -d @template.json ${MANAGER_REST}/v2/pus"
  requestId=$(curl -X POST --insecure --silent --header 'Content-Type: application/json' --header 'Accept: text/plain' -u gs-admin:${TOKEN} -d @template.json ${MANAGER_REST}/v2/pus)
  echo -e "$requestId Finished deployment of service $puName...\n"
}

function deploy_space {
  local puName="$1"
  local resource="$2"
  echo -e "Deploying space $puName..\n"

  cat > template.json <<EOF
{
     "name": "${puName}",
     "resource": "${resource}",
     "topology": {
       "schema": "partitioned",
       "partitions": 1,
       "backupsPerPartition": 1
     },
     "contextProperties": {
       "license": "${GS_LICENSE}"
     }
   }
EOF
  #echo "curl -X POST --insecure --silent --header 'Content-Type: application/json' --header 'Accept: text/plain' -u gs-admin:${TOKEN} -d @template.json ${MANAGER_REST}/v2/pus"
  requestId=$(curl -X POST --insecure --silent --header 'Content-Type: application/json' --header 'Accept: text/plain' -u gs-admin:${TOKEN} -d @template.json ${MANAGER_REST}/v2/pus)
  echo -e "$requestId Finished deployment of service $puName...\n"
}

function deploy_stateless {
  local puName="$1"
  local resource="$2"
  echo -e "Deploying stateless service $puName..\n"
  echo $KAFKA_HOST

  cat > template.json <<EOF
{
     "name": "${puName}",
     "resource": "${resource}",
     "topology": {
       "instances": 1
     },
     "contextProperties": {
       "license": "$GS_LICENSE",
       "KAFKA_HOST": "$KAFKA_HOST"
     }
   }
EOF
  #echo "curl -X POST --insecure --silent --header 'Content-Type: application/json' --header 'Accept: text/plain' -u gs-admin:${TOKEN} -d @template.json ${MANAGER_REST}/v2/pus"
  requestId=$(curl -X POST --insecure --silent --header 'Content-Type: application/json' --header 'Accept: text/plain' -u gs-admin:${TOKEN} -d @template.json ${MANAGER_REST}/v2/pus)
  echo -e "$requestId Finished deployment of stateless service $puName...\n"
}

function assertRequest {
  local requestId="$1"
  local requestStatus
   sleep 30s
  while [[ $requestStatus != \"successful\" ]]; do
   #echo "curl -X GET --insecure --silent --header 'Accept: text/plain' -u gs-admin:${TOKEN} ${MANAGER_REST}/v2/requests/$requestId"
    requestStatus=$(curl -X GET --insecure --silent --header 'Accept: text/plain' -u gs-admin:${TOKEN} ${MANAGER_REST}/v2/requests/$requestId |jq .)
    echo -n "."
    sleep 1
  done
  echo ""
}

function undeploy_pu {
    local puName="$1"
    echo -e "Undeploying service $puName...\n"
   # echo "curl -X DELETE --insecure --silent --header 'Accept: text/plain' -u gs-admin:${TOKEN} ${MANAGER_REST}/v2/pus/$puName"
    requestId=$(curl -X DELETE --insecure --silent --header 'Accept: text/plain' -u gs-admin:${TOKEN} ${MANAGER_REST}/v2/pus/$puName)
}

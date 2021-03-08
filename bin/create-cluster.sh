#!/usr/bin/env bash
#if cluster is created then detroying it.
if [[ -d ".gsctl/" ]]; then
  ./destroy-cluster.sh
  echo "destroyed old cluster"
fi

set -ex
source env.sh
export GS_CLI_VERBOSE=true

echo "Downloading gsctl.jar"
wget https://gigaspaces-releases-eu.s3.amazonaws.com/gsctl-ea/15.8.1-m1-sun-13/gsctl.jar
echo "Downloaded gsctl.jar"

echo "Changed default product type to insighedge - gsctl"
java -jar gsctl.jar init
sed -i 's/sslEnabled: "true"/sslEnabled: "false"/g' services.yaml
#For Mac  uncomment below and comment above
#sed -i "" 's/sslEnabled: "true"/sslEnabled: "false"/g' services.yaml

java -jar gsctl.jar config product-type use insightedge

echo "Setting up cluster"
java -jar gsctl.jar create

echo "List of services"
java -jar gsctl.jar list-services

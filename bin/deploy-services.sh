#!/usr/bin/env bash

set -e
source env.sh

echo "Deploying services"
declare -a requestIds
declare -a servicesNames
for space in demo; do
    deploy_dynamic_space "demo" "https://jay-dalal.s3-us-west-2.amazonaws.com/atnt/15.8.0/$space.jar"
    #deploy_dynamic_space "demo" "https://aa-nihar-test.s3.us-east-2.amazonaws.com/atnt/$space.jar"
    requestIds+=($requestId)
    servicesNames+=($space)
    service=$(java -jar gsctl.jar list-services | grep ops-manager-0 | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" )
    ssh -i .gsctl/*.pem -T -o StrictHostKeyChecking=no ec2-user@$service << EOF
    nohup wget https://aa-nihar-test.s3.us-east-2.amazonaws.com/atnt/springbootappModifier.jar > /dev/null 2>&1 &
EOF
done

#manager1=$(java -jar gsctl.jar list-services|grep manager-rest-0 | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b")

#ssh -i .gsctl/*.pem -T -o StrictHostKeyChecking=no ec2-user@$manager1 << EOF
#  nohup docker exec mydb2 bash -c 'su - db2inst1 sh -c "strmqm PUBSRC;cd db2mqrep/myQCap;asnqcap capture_server=PUBSRC"' | tee nohup.out &
#  sleep 20
#  nohup docker exec mydb2 bash -c 'su - db2inst1 sh -c "strmqm PUBSRC;cd db2mqrep;java -Djava.library.path=/opt/mqm/java/lib64 -jar db2-delta-server-1.0-jar-with-dependencies.jar"' | tee nohup.out &
#  sleep 5
#EOF


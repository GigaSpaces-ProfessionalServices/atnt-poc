#!/usr/bin/env bash

#set -ex

if [[ $1 = "--help" ]]; then
    cat help.txt
elif [[ $1 = "--create" ]]; then
    echo "creating cluster and listing services"
    ./create-cluster.sh
elif [[ $1 = "--deploy" ]]; then
    echo "deploying space"
    ./deploy-services.sh
     #nohup wget https://jay-dalal.s3-us-west-2.amazonaws.com/atnt/15.8.0/springbootappModifier.jar > /dev/null 2>&1 &
     echo "after running spring app"
     service=$(java -jar gsctl.jar list-services | grep ops-manager-0 | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" )
elif [[ $1 = "--undeploy" ]]; then
    echo "undeploying space"
    ./undeploy-services.sh
elif [[ $1 = "--destroy" ]]; then
    echo "destroying cluster"
    ./destroy-cluster.sh
elif [[ $1 = "--startspringapp" ]]; then
    service=$(java -jar gsctl.jar list-services | grep ops-manager-0 | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" )
    ssh -i .gsctl/*.pem -T -o StrictHostKeyChecking=no ec2-user@$service << EOF
    nohup wget https://jay-dalal.s3-us-west-2.amazonaws.com/atnt/15.8.0/springbootappModifier.jar > /dev/null 2>&1 &
    sleep 5
    nohup java -jar atnt-demo-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
EOF
    echo "Springboot Rest Url : http://${service}:12000/gigaspaces"
elif [[ $1 = "--stopspringapp" ]]; then
    service=$(java -jar gsctl.jar list-services | grep ops-manager-0 | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" )
    ssh -i .gsctl/*.pem -T -o StrictHostKeyChecking=no ec2-user@$service << EOF
    pkill -f atnt-demo-0.0.1-SNAPSHOT
    sleep 7
    nohup rm * &
EOF
    echo "Stopped spring app"
else
    echo "Please provide operation --help or --create or --deploy or --run or --destroy"
fi

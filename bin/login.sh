#!/usr/bin/env bash

#set -ex

service=$(java -jar gsctl.jar list-services | grep ops-manager-0 | grep -oE "\b([0-9]{1,3}\.){3}[0-9]{1,3}\b" )
ssh -i .gsctl/*.pem ec2-user@$service 

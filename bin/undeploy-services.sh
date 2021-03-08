#!/usr/bin/env bash

#set -ex
source env.sh

declare -a requestIds
declare -a servicesNames
for space in demo; do
	 undeploy_pu "$space"
	 requestIds+=($requestId)
	 servicesNames+=($space)
done


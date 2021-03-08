#!/usr/bin/env bash
GS_HOME=/home/nihar/work/gigaspaces/gigaspaces-insightedge-enterprise-15.8.0/bin
#GS_HOME=<SET PATH TO GIGASPACES HOME DIRECTORY>

#cd $GS_HOME
#pwd
#$GS_HOME/gs.sh host run-agent --auto --gsc=5
echo "Starting Gigaspaces ..."
nohup $GS_HOME/gs.sh host run-agent --auto --gsc=5 > /tmp/agent-console.log 2>&1 &
sleep 30
echo "Deploying space ..."
$GS_HOME/gs.sh pu deploy --partitions=2 --ha=true -p pu.dynamic-partitioning=true demo demo.jar $*

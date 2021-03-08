#!/usr/bin/env bash
GS_HOME=${GS_HOME=`(cd ../../; pwd )`}
#GS_HOME=<SET PATH TO GIGASPACES HOME DIRECTORY>

./gs.sh host run-agent --auto --gsc=5;
sleep 30
$GS_HOME/bin/gs.sh pu deploy --partitions=2 --ha=true -p pu.dynamic-partitioning=true demo target/demo.jar $*

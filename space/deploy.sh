#!/usr/bin/env bash
GS_HOME=${GS_HOME=`(cd ../../; pwd )`}
$GS_HOME/bin/gs.sh pu deploy --partitions=2 --ha=true -p pu.dynamic-partitioning=true demo target/demo.jar $*

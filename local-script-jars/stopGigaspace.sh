#!/usr/bin/env bash
GS_HOME=/home/nihar/work/gigaspaces/gigaspaces-insightedge-enterprise-15.8.0/bin
#GS_HOME=<SET PATH TO GIGASPACES HOME DIRECTORY>


echo "Stopping Gigaspaces ..."
$GS_HOME/gs.sh host kill-agent

#!/bin/bash

export AURAHOME=/datapool/aura
export POLICY=${AURAHOME}/dist/jsk-all.policy
export REGHOST=$1
export DIR=`dirname $0`

nohup java -DauraHome=${AURAHOME} -DregHost=${REGHOST} -jar $DIR/../dist/grid.jar \
     /com/sun/labs/aura/grid/ec2/resource/sitmCrawlerConfig.xml starter \
     "${AURAHOME}/logs/crawlers.%g.out" &> ${AURAHOME}/logs/crawlers.stdout &

export PID=$!
echo $PID > ${AURAHOME}/logs/crawlers.pid


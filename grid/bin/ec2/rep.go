#!/bin/bash

export AURAHOME=/datapool/aura
export POLICY=${AURAHOME}/dist/jsk-all.policy
export REGHOST=$1
export PREFIX=`ls ${AURAHOME} | grep '[01][01]'`

export DIR=`dirname $0`

nohup java -DauraHome=${AURAHOME} -DregHost=${REGHOST} -Dprefix=${PREFIX} -jar $DIR/../dist/grid.jar \
     /com/sun/labs/aura/grid/ec2/resource/repPCConfig.xml starter \
     "${AURAHOME}/logs/rep-${PREFIX}.%g.out" &> ${AURAHOME}/logs/rep-${PREFIX}.stdout &

export JPID=$!
echo ${JPID} > ${AURAHOME}/logs/rep.pid

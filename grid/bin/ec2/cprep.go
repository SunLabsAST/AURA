#!/bin/bash

export AURAHOME=/datapool/aura
export POLICY=${AURAHOME}/dist/jsk-all.policy
export REGHOST=$1
export PREFIX=`ls ${AURAHOME} | grep '[01][01]'`

export DIR=`dirname $0`

if [ -z "${REGHOST}" ] ; then
    echo Usage: $DIR/cprep.go registry_host
    exit
fi

nohup java -Xmx1200m -DauraHome=${AURAHOME} -DregHost=${REGHOST} -Dprefix=${PREFIX} -jar $DIR/../dist/grid.jar \
     /com/sun/labs/aura/grid/ec2/resource/repPCCopyConfig.xml starter \
     "${AURAHOME}/logs/rep-${PREFIX}.%g.out" &> ${AURAHOME}/logs/rep-${PREFIX}.stdout &

export JPID=$!
echo ${JPID} > ${AURAHOME}/logs/rep.pid

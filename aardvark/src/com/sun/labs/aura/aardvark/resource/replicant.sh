#!/bin/bash
HOST=`hostname`
if [ "${HOST}" = "bobism" ]; then
    PREFIX=00
    AURAHOME=/scratch/aura
elif [ "${HOST}" = "search" ]; then
    PREFIX=01
    AURAHOME=/scratch2/stgreen/aura
elif [ "$HOST" = "lur" ]; then
    PREFIX=10
    AURAHOME=/big/aura
elif [ "$HOST" = "faber" ]; then
    PREFIX=11
    AURAHOME=/export/aura
fi

nohup java -DauraHome=${AURAHOME} -Dprefix=${PREFIX} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/partitionClusterConfig.xml \
      partitionClusterStarter &> pc.out &

sleep 5

nohup java -Xmx2g -DauraHome=${AURAHOME} -Dprefix=${PREFIX} \
      -jar dist/aardvark.jar \
      /com/sun/labs/aura/resource/replicantConfig.xml \
      replicantStarter &> rep.out & 

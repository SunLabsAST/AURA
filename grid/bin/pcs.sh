#!/bin/bash

export DIR=`dirname $0`

. ${DIR}/setup.sh

#
# Data store heads.
mkdir -p ${LOGS}/pc
export PORT=4401
for PREFIX in 0000 0001 0010 0011 0100 0101 0110 0111 1000 1001 1010 1011 1100 1101 1110 1111; do
    echo Starting PC for prefix ${PREFIX} on port ${PORT}
    nohup java -XX:ParallelGCThreads=8 \
	-DauraHome=${AURA_HOME} -DauraGroup=${AURA_GROUP} -DcsPort=${PORT} -Dprefix=${PREFIX} \
	-jar ${JAR} ${CONF}/partitionClusterConfig.xml \
	partitionClusterStarter ${LOGS}/pc/pc-${PREFIX}.%g.out &> ${LOGS}/pc/pc-${PREFIX}.out &
    echo $! > ${PIDDIR}/pc-${PREFIX}.pid
    PORT=$(( $PORT + 1 ))
done


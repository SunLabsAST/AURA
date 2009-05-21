#!/bin/bash

export DIR=`dirname $0`
. ${DIR}/setup.sh

DATA_DIR=/aura/scratch/data
if [ $1 != "" ] ; then
    DATA_DIR=$1
fi

#
# Data store heads.
mkdir -p ${LOGS}/rep
export PORT=3301
for PREFIX in 0000 0001 0010 0011 0100 0101 0110 0111 1000 1001 1010 1011 1100 1101 1110 1111; do
    echo Starting replicant for prefix ${PREFIX} on port ${PORT}
    nohup java -Xmx2500m \
	-XX:ParallelGCThreads=4 \
	-DauraHome=${AURA_HOME} -DauraGroup=${AURA_GROUP} -DcsPort=${PORT} -Dprefix=${PREFIX} \
	-DdataFS=${DATA_DIR}/${PREFIX} \
	-jar ${JAR} ${CONF}/replicantSlowDumpConfig.xml \
	replicantStarter ${LOGS}/rep/rep-${PREFIX}.%g.out &> $LOGS/rep/rep-${PREFIX}.out &
    echo $! > ${PIDDIR}/rep-${PREFIX}.pid
    PORT=$(( $PORT + 1 ))
done


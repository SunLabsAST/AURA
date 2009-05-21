#!/usr/bin/bash

export DIR=`dirname $0`

. ${DIR}/setup.sh

#
# Data store heads.
mkdir -p ${LOGS}/dshead
export PORT=5500
#for i in 1 2 3 4; do
for i in 1 ; do
    echo Starting data store head ${i} on port ${PORT}
    nohup java -XX:ParallelGCThreads=8 \
	-DauraHome=${AURA_HOME} -DauraGroup=${AURA_GROUP} -DcsPort=${PORT} \
	-jar $JAR ${CONF}/dataStoreHeadConfig.xml \
	parallelDataStoreHeadStarter ${LOGS}/dshead/dshead-$i.%g.out &> ${LOGS}/dshead/dshead-$i.out &
    echo $! > ${PIDDIR}/dshead-$i.pid
    PORT=$(( $PORT + 1 ))
done


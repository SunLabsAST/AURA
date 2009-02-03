#!/bin/bash
#
# Get the instance metadata and use it to start the service
. /lib/svc/share/smf_include.sh
. /opt/aura/services/method/parseMeta.sh

GROUP=`getValue auraGroup`
REGISTRY_HOST=`getValue registryHost`

if [ -z "${GROUP}" ] ; then
    GROUP=live-aura
    echo Using default group: ${GROUP}
fi

JVM_OPTS=$1
NAME=$2
CONFIG=$3
STARTER=$4
LOG_TYPE=$5

mkdir -p /mnt/dist/logs/${LOG_TYPE}

#
# Start the service in a JVM.
java $JVM_OPTS \
    -DregistryHost=${REGISTRY_HOST} \
    -DauraGroup=${GROUP} \
    -jar /mnt/dist/dist/grid.jar \
    ${CONFIG} ${STARTER} "/mnt/dist/logs/${LOG_TYPE}/${NAME}.%g.out" \
    &> /mnt/dist/logs/${LOG_TYPE}/${NAME}.stdout &

exit $SMF_EXIT_OK

#!/bin/bash
#
# Get the instance metadata and use it to start reggie.
. /lib/svc/share/smf_include.sh
. /opt/aura/services/method/parseMeta.sh

GROUP=`getValue auraGroup`

if [ -z "${GROUP}" ] ; then
    GROUP=live-aura
    echo Using default group: ${GROUP}
fi

AURADIR=/distpool/aura
POLICY=${AURADIR}/dist/jsk-all.policy

java -DauraGroup=$GROUP \
    -DauraDir=${AURADIR} \
    -Djava.security.policy=${POLICY} \
    -DauraPolicy=${POLICY} \
    -jar ${AURADIR}/jini/lib/start.jar \
    ${AURADIR}/jini/nobrowse.config &> /distpool/aura/logs/reggie/reggie.out &

exit $SMF_EXIT_OK

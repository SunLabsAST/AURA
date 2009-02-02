#!/bin/bash

. /lib/svc/share/smf_include.sh
. /opt/aura/services/method/parseMeta.sh

DIST_HOST=`getValue registryHost`

if [ -z "${DIST_HOST}" ] ; then
    echo No dist host defined
    exit $SMF_EXIT_ERR_FATAL;
fi

echo Distribution host: $DIST_HOST

#
# Mount the dist file system.
mkdir -p /mnt/dist
mount ${DIST_HOST}:/distpool/aura /mnt/dist
if [ $? -ne 0 ] ; then
    echo Error mounting distribution from ${DIST_HOST}
    exit $SMF_EXIT_ERR_FATAL;
fi
    
exit $SMF_EXIT_OK


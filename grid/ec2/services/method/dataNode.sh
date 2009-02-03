#!/bin/bash

. /lib/svc/share/smf_include.sh
. /opt/aura/services/method/parseMeta.sh

DISTHOST=`getValue registryHost`

echo Distribution host: $DISTHOST

#
# Import the Aura data pool
/opt/aura/services/method/importPool.sh datapool 80

#
# Make sure that we got it.
if [ $? -eq 1 ] ; then
    echo Could not import data pool!
    exit 1
fi

#
# Make sure that we're sharing the pool, just in case.
zfs set sharenfs=on datapool/aura

if [ $? -ne 0 ] ; then
    exit $SMF_EXIT_ERR_FATAL
fi

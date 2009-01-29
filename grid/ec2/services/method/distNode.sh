#!/bin/bash

. /lib/svc/share/smf_include.sh

#
# Import the distpool
/opt/aura/services/method/importPool.sh distpool 80

#
# Make sure that we got it.
if [ $? -eq 1 ] ; then
    echo Could not import distribution pool!
    exit $SMF_EXIT_ERR_FATAL
fi

#
# Make sure that we're sharing the distribution pool
zfs set sharenfs=on distpool/aura

if [ $? -ne 0 ] ; then
    exit $SMF_EXIT_ERR_FATAL
fi

exit $SMF_EXIT_OK

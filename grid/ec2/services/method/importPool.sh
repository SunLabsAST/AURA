#!/bin/bash

poolName=$1

if [ -z "$poolName" ] ; then
    echo "Usage: $0 poolName"
    exit 1;
fi

#
# See if it's already imported.
zpool list | grep $poolName
if [ $? -eq 0 ] ; then
    exit 0
fi

declare -i maxTries=$2

if [ $maxTries -eq 0 ] ; then
    maxTries=60
fi

#
# OK, try to import the pool.
for (( tries=0 ; $tries < $maxTries ; tries++ )) ; do 
    zpool import $poolName
    if [ $? -eq 0 ] ; then
	exit 0
    fi
    sleep 5
done

exit 1


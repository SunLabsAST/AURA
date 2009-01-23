#!/bin/bash

DIR=`dirname $0`
. $DIR/basic.sh 

#
# Remove log files.
for addr in ${REPINSTL}; do
    echo Cleaning logs for $addr
    $SSH root@$addr "rm -f ${FS}/logs/*"
done



#!/bin/bash

DIR=`dirname $0`
. $DIR/basic.sh 

#
# Remove log files.
for addr in ${REPINSTL}; do
    echo Getting logs from $addr
    $SCP root@$addr:${FS}/logs/*.out .
done



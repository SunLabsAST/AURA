#!/bin/bash

DIR=`dirname $0`
. $DIR/basic.sh 

#
# Start up reggie.
echo Starting reggie on ${REGINSTANCE}
$SSH root@${REGINSTANCE} ${FS}/jini/startReggieNB.sh

sleep 5

#
# Start up data store head, stat server, and process manager.
echo Starting data store head on ${DSHINSTANCE}
$SSH root@${DSHINSTANCE} ${FS}/bin/dsh.go ${REGHOST}

sleep 5

#
# Start up replicants.
for addr in ${REPINSTL}; do
    echo Starting replicant on $addr
    $SSH root@$addr ${FS}/bin/cprep.go ${REGHOST}
done



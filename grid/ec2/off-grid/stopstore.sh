#!/bin/bash

DIR=`dirname $0`
. $DIR/basic.sh 

#
# Kill replicants
for addr in ${REPINSTL}; do
    echo Stopping replicant on $addr
    $SSH root@$addr 'kill `cat /datapool/aura/logs/rep.pid`'
done

#
# Kill data store head
echo Stopping data store head
$SSH root@${DSHINSTANCE} 'kill `cat /datapool/aura/logs/dsh.pid`'

#
# Kill reggie.
echo Stopping reggie
$SSH root@${REGINSTANCE} 'kill `cat /datapool/aura/logs/reggie.pid`'





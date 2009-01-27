#!/bin/bash

export EXTADDRS=`ec2-describe-instances | grep INSTANCE | cut -f 4`
export FIRSTADDR=`ec2-describe-instances | grep INSTANCE | head -1 | cut -f 4`
export REGHOST=`ec2-describe-instances | grep INSTANCE | head -1 | cut -f 5`
export FS=/datapool/aura

export ID=~/.ec2/id_rsa-aura
export SCP="scp -i $ID"
export SSH="ssh -i $ID"

echo Addresses $EXTADDRS
echo Reg host $REGHOST

#
# Start up reggie.
echo Starting reggie
$SSH root@${FIRSTADDR} ${FS}/jini/startReggieNB.sh

sleep 5

#
# Start up data store head, stat server, and process manager.
echo Starting data store head
$SSH root@${FIRSTADDR} ${FS}/bin/dsh.go ${REGHOST}

sleep 5

#
# Start up replicants.
for addr in ${EXTADDRS}; do
    echo Starting replicant on $addr
    $SSH root@$addr ${FS}/bin/rep.go ${REGHOST}
done



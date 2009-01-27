#!/bin/bash

export EXTADDRS=`ec2-describe-instances | grep INSTANCE | cut -f 4`
export LASTADDR=`ec2-describe-instances | grep INSTANCE | tail -1 | cut -f 4`
export REGHOST=`ec2-describe-instances | grep INSTANCE | head -1 | cut -f 5`
export FS=/datapool/aura

export ID=~/.ec2/id_rsa-aura
export SCP="scp -i $ID"
export SSH="ssh -i $ID"

echo Reg host $REGHOST

#
# Start up crawlers
echo Starting crawlers on ${LASTADDR}
$SSH root@${LASTADDR} mkdir ${FS}/cache
$SSH root@${LASTADDR} ${FS}/bin/crawlers.go ${REGHOST}




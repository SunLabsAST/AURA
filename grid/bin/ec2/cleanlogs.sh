#!/bin/bash

export EXTADDRS=`ec2-describe-instances | grep INSTANCE | cut -f 4`
export FS=/datapool/aura

export ID=~/.ec2/id_rsa-aura
export SCP="scp -i $ID"
export SSH="ssh -i $ID"

#
# Remove log files.
for addr in ${EXTADDRS}; do
    echo Cleaning logs for $addr
    $SSH root@$addr "rm -f ${FS}/logs/*"
done



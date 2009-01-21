#!/bin/bash

export EXTADDRS=`ec2-describe-instances | grep INSTANCE | cut -f 4`
export FS=/datapool/aura

export ID=~/.ec2/id_rsa-aura
export SCP="scp -i $ID"
export SSH="ssh -i $ID"

export DIR=`dirname $0`

#
# Copy the grid distribution and the binaries up to the instances.
for addr in ${EXTADDRS}; do
    echo Uploading to $addr
    $SCP $DIR/../../dist/grid-dist.jar root@$addr\:$FS && \
	$SSH root@$addr unzip -o -d $FS $FS/grid-dist.jar
    $SCP $DIR/*.go root@$addr\:$FS/bin
done



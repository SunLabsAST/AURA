#!/bin/bash

export DIR=`dirname $0`
. $DIR/basic.sh 

#
# Copy the grid distribution and the binaries up to the instances.
for addr in ${REPINSTL}; do
    echo Uploading to $addr
    $SCP $DIR/../../dist/grid-dist.jar root@$addr\:$FS && \
	$SSH root@$addr unzip -o -d $FS $FS/grid-dist.jar
    $SCP $DIR/*.go root@$addr\:$FS/bin
done



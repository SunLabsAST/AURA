#!/bin/bash

DIR=`dirname $0`
. $DIR/basic.sh 

#
# Start up crawlers
echo Starting crawlers on ${CRAWLINSTANCE}
$SSH root@${CRAWLINSTANCE} mkdir ${FS}/cache
$SSH root@${CRAWLINSTANCE} ${FS}/bin/crawlers.go ${REGHOST}




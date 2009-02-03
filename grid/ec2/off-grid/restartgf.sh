#!/bin/bash

DIR=`dirname $0`
. $DIR/basic.sh 

$SSH root@${GFHOST} svcadm restart aura




#!/bin/bash

INSTALLDIR=$(dirname $0)
echo "Starting JINI Lookup Service from $INSTALLDIR"
java -Djava.security.policy=$INSTALLDIR/jsk-all.policy -DjiniDir=$INSTALLDIR -jar $INSTALLDIR/lib/start.jar $INSTALLDIR/start.config

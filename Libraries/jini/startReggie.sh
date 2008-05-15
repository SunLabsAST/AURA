#!/bin/sh
INSTALLDIR=`dirname $0`
echo $INSTALLDIR
java -Djava.security.policy=$INSTALLDIR/jsk-all.policy -jar $INSTALLDIR/lib/start.jar $INSTALLDIR/start.config

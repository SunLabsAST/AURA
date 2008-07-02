#!/bin/bash

pushd "$(dirname $0)/../../" > /dev/null
BASEDIR="$(pwd)"
popd > /dev/null
AURAHOME=$BASEDIR/aura
JSKPOLICY=$BASEDIR/Libraries/jini/jsk-all.policy

AURALIBS=$BASEDIR/aura/dist/aura.jar:$BASEDIR/sitm/dist/sitm.jar
for file in $(find $BASEDIR/Libraries/ -name *jar); do AURALIBS=$AURALIBS:$file; done

DATA=$@
[ -z "$DATA" ] && DATA=$BASEDIR/sitm/data/lastfm.users

echo "Loading Data: $DATA"

java -DauraHome=${AURAHOME} -DauraPolicy=$JSKPOLICY -mx1g -classpath $AURALIBS \
    com.sun.labs.aura.music.sample.LastFMLoader $DATA

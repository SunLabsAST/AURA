#!/bin/bash

cd "$(dirname $0)/../../"
BASEDIR="$(pwd)"
AURAHOME=$BASEDIR/aura
JSKPOLICY=$BASEDIR/Libraries/jini/jsk-all.policy

AURALIBS=aura/dist/aura.jar:sitm/dist/sitm.jar
for file in $(find Libraries/ -name *jar); do AURALIBS=$AURALIBS:$file; done

DATA=$@
[ -z "$DATA" ] && DATA=sitm/data/lastfm.users

echo "Loading Data: $DATA"

java -DauraHome=${AURAHOME} -DauraPolicy=$JSKPOLICY -mx1g -classpath $AURALIBS \
    com.sun.labs.aura.music.sample.LastFMLoader $DATA

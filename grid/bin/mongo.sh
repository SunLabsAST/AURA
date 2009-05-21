#!/usr/bin/bash

export DIR=`dirname $0`
. ${DIR}/setup.sh

mkdir -p ${LOGS}/reggie

#
# Reggie
nohup ${DIR}/../dist/jini/startReggieNB.sh &> ${LOGS}/reggie/reggie.out &
echo $! > ${PIDDIR}/reggie.pid

#
# Data store heads.
${DIR}/dsheads.sh

#
# Other services.
${DIR}/other.sh

#
# Partition clusters.
${DIR}/pcs.sh

#
# Replicants.
${DIR}/reps.sh $*

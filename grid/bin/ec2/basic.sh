#!/bin/bash 
export REPAMI=ami-7db75014
export GFAMI=ami-958067fc
export TMPFILE=/tmp/$$.inst

ec2-describe-instances | grep INSTANCE | sort -k 4 > ${TMPFILE}

#
# Parse out the instances that will run various things.
export REPINSTL=`grep ${REPAMI} ${TMPFILE} | cut -f 4 | head -4`
export REPINSTANCES=(`grep ${REPAMI} ${TMPFILE} | cut -f 4 | head -4`)
export REGINSTANCE=${REPINSTANCES[0]}
export DSHINSTANCE=${REPINSTANCES[1]}
export CRAWLINSTANCE=${REPINSTANCES[3]}

export REGHOST=`grep ${REPAMI} ${TMPFILE} | head -1 | cut -f 5`
export REPLHOST=`grep ${REPAMI} ${TMPFILE} | cut -f 5`
export GFHOST=`grep ${GFAMI} ${TMPFILE} | head -1 | cut -f 4`

export FS=/datapool/aura

export ID=~/.ec2/id_rsa-aura
export SCP="scp -i $ID"
export SSH="ssh -i $ID"


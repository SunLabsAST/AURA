#!/bin/bash

#
# Parses the instance metadata for an EC2 instance into two bash array variables, one for the
# names and one for the values.

export MD_FILE=/tmp/$$.instmd

curl http://169.254.169.254/latest/user-data > ${MD_FILE}

declare -a keys
declare -a vals
keys=(`cut -d = -f 1 ${MD_FILE}`)
vals=(`cut -d = -f 2 ${MD_FILE}`)

export keys vals

#
# Gets the value associated with the given key and echoes it, suitable for capture by
# backticking the function call.
function getValue () {
    for (( p=0 ; p < ${#keys[*]} ; p++ )) ; do
	if [ "${keys[$p]}" == "$1" ] ; then
	    echo ${vals[$p]}
	    return
	fi
    done;
    echo ""
}

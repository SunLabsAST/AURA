#!/bin/bash
#
# A script that will use a manifest template to create a number of SMF services
# that will be used to manage a set of Aura services.

. /lib/svc/share/smf_include.sh
. /opt/aura/services/method/parseMeta.sh

#
# Our single argument indicates whether the generated services need to use
# a data pool, which will affect their dependencies (i.e., they won't be dependant on 
# aura/import-data.  The default is that services won't use data.
SERVICES_USE_DATA="$1"

if [ -z "${SERVICES_USE_DATA}" ] ; then
    SERVICES_USE_DATA="no";
fi

#
# Pass the instance metadata into a perl script, and have it do the hard work.  It will
# produce a list of service manifest names on the standard output.
GENDIR=/opt/aura/services/manifest/generated
mkdir -p ${GENDIR}
MANIFESTS=`/opt/aura/services/method/createManifest.pl ${GENDIR} ${SERVICES_USE_DATA} ${MD_FILE}`

#
# First, we'll validate the manifests
for m in $MANIFESTS ; do
    echo Validating: $m
    svccfg validate $m
    if [ $? -ne 0 ] ; then
	echo Invalid manifest: $m
	exit $SMF_EXIT_ERR_FATAL
    fi
done

for m in $MANIFESTS ; do
    echo Importing: $m
    svccfg import $m
    if [ $? -ne 0 ] ; then
	echo Error importing manifest: $m
	exit $SMF_EXIT_ERR_FATAL
    fi
done

exit $SMF_EXIT_OK


#!/bin/bash
export JAVA_HOME=/var/tmp/jdk1.6.0_18
export PATH=/var/tmp/sunstudio12.1/bin:$PATH

rm /files/auraLogs/rep/rep-collect.*.out

collect -d /var/tmp -p high -s on -j on \
    /var/tmp/jdk1.6.0_18/bin/java \
    -Xmx2g \
    -DauraHome=/files/auraDist \
    -DdataFS=/files/data \
    -DauraGroup=live-aura -Dprefix=0000 \
    -jar /files/auraDist/dist/grid.jar \
    /com/sun/labs/aura/resource/replicantSlowDumpConfig.xml replicantStarter \
    /files/auraLogs/rep/rep-collect.%g.out &> /files/auraLogs/other/rep-collect.out &



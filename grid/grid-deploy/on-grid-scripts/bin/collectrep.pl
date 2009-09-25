#!/usr/bin/perl
#
# Launch a collector-wrapped jvm for a replicant.


exec '/files/dev-tools/bin/collect', 
    '-p', 'high', '-s', 'on', '-j', 'on',
    'java', '-Xmx2g', 
    '-DauraHome=/files/auraDist',
    "-DauraGroup=$ARGV[0]",
    "-Dprefix=$ARGV[1]", "-jar", 
    "/files/auraDist/dist/grid.jar",
    "/com/sun/labs/aura/resource/replicantSlowDumpConfig.xml",
    "starter",
    "${AURAHOME}/logs/rep-${PREFIX}.%g.out";



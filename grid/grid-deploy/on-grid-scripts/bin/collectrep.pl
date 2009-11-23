#!/usr/bin/perl
#
# Launch a collector-wrapped jvm for a replicant.
$| = 1;
exec '/opt/SUNWspro/bin/collect', 
    '-p', 'high', '-s', 'on', '-j', 'on',
    '/usr/bin/java', '-Xmx2g', 
    '-DauraHome=/files/auraDist',
    "-DauraGroup=$ARGV[0]",
    "-Dprefix=$ARGV[1]", "-jar", 
    "/files/auraDist/dist/grid.jar",
    "/com/sun/labs/aura/resource/replicantSlowDumpConfig.xml",
    "starter",
    "/files/auraLogs/rep/rep-$ARGV[1].%g.out";



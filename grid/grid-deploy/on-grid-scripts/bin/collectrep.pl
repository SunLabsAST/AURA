#!/usr/bin/perl
#
# Launch a collector-wrapped jvm for a replicant.
$| = 1;
$libs = `/usr/bin/find /files/dev-tools/SunStudioExpress/lib -type d`;
$ENV{'LD_LIBRARY_PATH'} = join ':', split(/\s+/, $libs);
print `/usr/bin/echo $LD_LIBRARY_PATH`
exec '/files/dev-tools/SunStudioExpress/bin/collect', 
    '-p', 'high', '-s', 'on', '-j', 'on',
    'java', '-Xmx2g', 
    '-DauraHome=/files/auraDist',
    "-DauraGroup=$ARGV[0]",
    "-Dprefix=$ARGV[1]", "-jar", 
    "/files/auraDist/dist/grid.jar",
    "/com/sun/labs/aura/resource/replicantSlowDumpConfig.xml",
    "starter",
    "${AURAHOME}/logs/rep-${PREFIX}.%g.out";



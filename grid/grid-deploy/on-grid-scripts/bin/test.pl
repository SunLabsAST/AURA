#!/usr/bin/perl
#
# Launch a collector-wrapped jvm as a test.
$| = 1;
$libs = `/usr/bin/find /files/dev-tools/SunStudioExpress/lib -type d`;
print "libs: $libs\n";
@l = split(/\s+/, $libs);
print "l: $l[0]\n";
$ENV{'LD_LIBRARY_PATH'} = join ':', split(/\s+/, $libs);
$ENV{'foo'} = 'bar';
print `LD_LIBRARY_PATH=foo /usr/bin/echo $LD_LIBRARY_PATH`;





    


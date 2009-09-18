#!/usr/bin/perl
#
# Untar the dev tools
$| = 1;
print `ls -l /files/dev-tools/`;
exec '/usr/bin/tar', '-xf', '/files/dev-tools/sse.tar';

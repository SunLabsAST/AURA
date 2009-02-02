#!/usr/bin/perl
use FileHandle;

$outDir = shift;
$useData = shift;

%props = ();

#
# Parse the properties given on STDIN.
while(<>) {
  if(/^\s*\#/) {
    next;
  }
  if(/^\s*([^\=]*)\=(.*)\s*$/) {
    $props{$1} = $2;
  }
}

#
# OK, we'll build an array of starters, based on the numbers given in
# the provided keys and values.  Each element in the array will be a
# hash reference containing the values that we need to replace in the
# manifest template.  Perl is fun!
@services = ();

foreach $key (keys(%props)) {
  if($key =~ /(config|starter|name|opts|logType)\.(\d+)/) {
    $type = $1;
    $num = $2;
    $hr = $services[$num];
    if(!defined($hr)) {
      $hr = {};
      $services[$num] = $hr;
    }
    $hr->{$type} = $props{$key};
  }
}

for $service (@services) {

  if(!defined($service)) {
    next;
  }

  $name = $service->{"name"};
  $config = $service->{"config"};
  $starter = $service->{"starter"};
  $jvmOpts = $service->{"opts"};
  $logType = $service->{"logType"};

  if(!defined($name) || !defined($config) || !defined($service)) {
    print STDERR "Bad starter: ", {$hr};
    next;
  }

  if(!defined($jvmOpts)) {
    #
    # We need something, and I'm lazy.
    $jvmOpts = "-Xmx1g";
  }

  if(!defined($logType)) {
    $logType = "other";
  }

  $fname = "$outDir/aura-$name.xml";
  $fh = new FileHandle(">$fname");
  print $fh <<EOFH;
<?xml version="1.0"?>
<!DOCTYPE service_bundle SYSTEM "/usr/share/lib/xml/dtd/service_bundle.dtd.1">
<service_bundle type='manifest' name='aura:$name'>
<service name='aura/$name' type='service' version='1'>
	<create_default_instance enabled='true' />
	<single_instance/>
EOFH

  if($useData eq "no") {
    print $fh <<EONDD;
        <dependency name='mount-dist' grouping='require_all' restart_on='none' type='service'>
    	    <service_fmri value='svc:/aura/mount-dist' />
	</dependency>
EONDD
  } else {
    print $fh <<EODD;
	<dependency name='import-data' type='service' grouping='require_all' restart_on='none'>
		<service_fmri value='svc:/aura/import-data' />
	</dependency>
EODD
}
print $fh <<EOR;
	<exec_method type='method' name='start'
		exec='/opt/aura/services/method/startService.sh "$jvmOpts" $name $config $starter $logType'
		timeout_seconds='60' />
	<exec_method type='method' name='stop' exec=':kill' timeout_seconds='60' />
	<stability value='Unstable' />
	<template>
		<common_name>
			<loctext xml:lang='C'>$name Aura service</loctext>
		</common_name>
	</template>
</service>

</service_bundle>
EOR

  $fh->close();
  print $fname, "\n";
}


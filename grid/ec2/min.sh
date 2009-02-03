#!/bin/bash

#
# A one off to minimize and set up a base OpenSolaris image on EC2 into something a bit more reasonable.

#
# Turn off services that we don't need
svcadm disable gdm:default
svcadm disable svc:/network/smtp:sendmail
svcadm disable hal
svcadm disable dbus
svcadm disable svc:/system/filesystem/rmvolmgr:default
svcadm disable svc:/system/avahi-bridge-dsd:default
svcadm disable svc:/application/desktop-cache/gconf-cache:default
svcadm disable svc:/application/desktop-cache/pixbuf-loaders-installer:default
svcadm disable svc:/application/desktop-cache/mime-types-cache:default
svcadm disable svc:/application/desktop-cache/input-method-cache:default
svcadm disable svc:/application/desktop-cache/desktop-mime-cache:default
svcadm disable svc:/application/desktop-cache/icon-cache:default

#
# Change AllowTcpForwarding in sshd_config so that we can tunnel into ports on-grid.

#
# Install a full JDK.
pkg install pkg:/SUNWj6dev@0.5.11-0.101

#
# Install screen so that we can easily disconnect from an instance while we're rebundling, which we 
# always seem to want to do about 10 minutes before heading home.
pkg install pkg:/SUNWscreen@4.0.3-0.101

#
# Copy up our services directory.  We won't need this everywhere, but it's nice to have.


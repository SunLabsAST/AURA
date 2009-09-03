 ######
 # Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 # DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 #
 # This code is free software; you can redistribute it and/or modify
 # it under the terms of the GNU General Public License version 2
 # only, as published by the Free Software Foundation.
 #
 # This code is distributed in the hope that it will be useful, but
 # WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 # General Public License version 2 for more details (a copy is
 # included in the LICENSE file that accompanied this code).
 #
 # You should have received a copy of the GNU General Public License
 # version 2 along with this work; if not, write to the Free Software
 # Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 # 02110-1301 USA
 #
 # Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 # Park, CA 94025 or visit www.sun.com if you need additional
 # information or have any questions.
 #####


import jpype as J
from lib import *


# Auto start the jvm if it's not running
if J.isJVMStarted()==0:
    print "Starting the JVM with default params..."
    init_jvm()



def get_keyset(splitNbr, prefix=""):
    fnct = lambda x: x.readKeySplit(prefix, splitNbr)
    return _get_simdata(fnct)


def get_tag_sim(splitNbr, prefix="",):
    fnct = lambda x: x.readMapSplit(prefix, splitNbr)
    return _get_simdata(fnct)


def _get_simdata(fnct):
    tagClustClass = J.JClass("com.sun.labs.aura.grid.sitm.TagClusterer")
    print "Loading java object..."
    jobj = fnct(tagClustClass)
    print "Converting to python native..."
    return j2py( jobj )





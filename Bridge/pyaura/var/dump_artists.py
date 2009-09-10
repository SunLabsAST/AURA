#! /usr/bin/python

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
 

import os
import pyaura.bridge as B
import pyaura.timestats as TS
import cPickle as C
  
 
def do_dump(regHost, output_folder): 
    
    aB = B.AuraBridge(regHost=regHost)
    tS = TS.TimeStats(100000, 500)

    cnt = 0
    idx = 0
    tag_dump = []
    
    for artist in aB.get_all_iterator("ARTIST"):
        tag_map = {}
        for tag in aB.mdb.get_tags(artist):
            tag_map[tag.name] = tag.count
        
        tag_dump.append( (artist.getName(), artist.getKey(), tag_map) )
        cnt += 1
        tS.next()

        if cnt > 20000:
            C.dump(tag_dump, open(os.path.join(output_folder, "all-artist-dump-%d.dump" % idx), "w"))
            cnt = 0
            tag_dump = []
            idx += 1

    # do final dump when we're done
    C.dump(tag_dump, open(os.path.join(output_folder, "all-artist-dump-%d.dump" % idx), "w"))


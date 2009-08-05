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

#
#   This script allows the removal of all the raw tags items from
#   the store and also clears all the tag maps from the track
#   and artists items
#


import pyaura.lib as L
import pyaura.bridge as B

aB = B.AuraBridge()


def del_rawtags():
    """
    Deletes all the raw tags items from the store
    """
    cnt = aB.get_item_count("ARTIST_TAG_RAW")

    for i, tag in enumerate(aB.get_all_iterator("ARTIST_TAG_RAW")):
        aB.delete_item(tag.getKey())

        if i%100==0:
            print "%d/%d" % (i, cnt)




def clear_all_rawtags():
    for it in ["TRACK", "ARTIST"]:
        clear_rawtags(it)



def clear_rawtags(itemtype):
    """
    Clears all the raw tag maps from the store    
    """

    print "Clearing raw tags for '%s'" % itemtype

    rawTagType = L._assert_type_tagtype("SOCIAL_RAW")
    cnt = aB.get_item_count(itemtype)

    for i, item in enumerate(aB.get_all_iterator(itemtype)):
        item.clearTags(rawTagType)
        aB.flush_item(item)

        if i%100==0:
            print "  %d/%d" % (i, cnt)


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

import sys
import warnings
import bridge as B
import jpype as J
from lib import j2py
try:
    import cPickle as pickle
except ImportError:
    warnings.warn("Error importing cPickle. Using python version")
    import pickle # fall back on Python version


def get_tag_popularity(verbose=False):
    """
    Iterates through all the raw social tags and sums their tagged artists map.    
    """

    if J.isJVMStarted():
        warnings.warn("JVM has already been started. Might not be configured properly for connecting to the grid")


    print "Connection to Aura..."
    aB = B.AuraBridge()
    print "Done."

    nbr_tags = aB.get_item_count("ARTIST_TAG_RAW")

    tags = {}
    for i, tag in enumerate(aB.get_all_iterator("ARTIST_TAG_RAW")):
        tags[tag.getName()] = sum( [tA.getCount() for tA in tag.getTaggedArtist()] )

        if i%250==0:
            print "%d/%d" % (i, nbr_tags)
        if verbose:
            print " %s -> %d" % (tag.getName(), tags[tag.getName()])

    return tags



def crawl_tag_info(tagnames):

    lfm2 = J.JClass("com.sun.labs.aura.music.web.lastfm.LastFM2Impl")()

    crawldata = {"track":{}, "album":{}}
    for tag in tagnames:
        print "  Crawling '%s'" % tag

        toptracks = j2py( lfm2.getTagTopTracks(tag) , 1)
        topalbums = j2py( lfm2.getTagTopAlbums(tag) , 1)
        for name, toplist in [("track", toptracks), ("album", topalbums)]:
            crawldata[name][tag] = []
            for cT in toptracks:
                t = cT.getItem()
                crawldata[name][tag].append( (cT.getCount(), t.getMbid(), t.getName(), t.getArtistMbid(), t.getArtistName()) )

    return crawldata




def _show_help():
    print "pickles a dict of tag_name->popularity"
    print "Usage: python tagspopular.py outfile"
    print ""


#  If launched from console
if __name__ == '__main__':

    if len(sys.argv) < 2:
        _show_help()
        sys.exit(1)


    tags = get_tag_popularity()


    fOut = file(sys.argv[1], 'w')
    pickle.dump(tags, fOut)
    fOut.close()


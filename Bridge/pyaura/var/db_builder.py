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

###
#   This file contains functions to generate a mbid resolved
#   dataset of top tracks for last.fm tags to train autotagger
###

import sys
import time
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



def crawl_tag_info(tagname):
    """
    Crawl top tracks and albums for a given tag
    """

    lfm2 = J.JClass("com.sun.labs.aura.music.web.lastfm.LastFM2Impl")()

    crawldata = {"track":[], "album":[]}
    crawlhash = {"track":set(), "album":set()}

    print "  Crawling '%s'" % tagname

    toptracks = j2py( lfm2.getTagTopTracks(tagname) , 1)
    topalbums = j2py( lfm2.getTagTopAlbums(tagname) , 1)
    for name, toplist in [("track", toptracks), ("album", topalbums)]:
        for cT in toplist:
            t = cT.getItem()
            crawldata[name].append( [cT.getCount(), t.getMbid(), t.getName(), t.getArtistMbid(), t.getArtistName()] )
            crawlhash[name].add( t.getMbid() + t.getName() + t.getArtistMbid() + t.getArtistName() )

    print "    Got::\tTracks:%d\tAlbums:%d" % ( len(crawldata["track"]), len(crawldata["album"]) )
    return crawldata, crawlhash


def crawl_tags_from_file(tag_file_path):
    """
    Expects a file with one tag per line, supporting synonyms, which will be
    added to the results of the primary tag

    Example:
    rock
    female vocalists, female, female vocalist, female vocals
    """


    crawled_tags = {}

    with open(tag_file_path, "r") as f:
        for line in f:
            print "Got line: '%s'" % line.strip()
            cD = None
            cH = None
            mainTagName = None
            for tag in [t.strip() for t in line.split(",")]:
                tcD, tcH = crawl_tag_info(tag)
                if mainTagName is None:
                    mainTagName = tag
                    cD = tcD
                    cH = tcH
                else:
                    # Merge the subtag's tracks/albums with the ones we already have
                    for name in ["track", "album"]:
                        cnt = 0
                        for t, h in zip(tcD[name], tcH[name]):
                            if not h in cH[name]:
                                cD[name].append( t )
                                cH[name].add ( h )
                                cnt += 1
                        print "          Merged %d %s" % (cnt, name)

            crawled_tags[mainTagName] = cD

    return crawled_tags



def load_sitm_artists(path):
    sitm_artists = set()
    for line in open(path, "r"):
        mbid, name = line.strip().split(" <sep> ")
        sitm_artists.add(mbid)
    return sitm_artists


def load_sitm_albums(path):
    sitm_albums = set()
    for line in open(path, "r"):
        mbid, album_name, artist_name = line.strip().split(" <sep> ")
        sitm_albums.add(mbid)
    return sitm_albums


def load_sitm_tracks(path):
    sitm_tracks = set()
    for line in open(path, "r"):
        mbid, track_name, album_name, artist_name = line.strip().split(" <sep> ")
        sitm_tracks.add(mbid)
    return sitm_tracks


def isMbid(s):
    return len(s)==36

def noneIfEmpty(s):
    return s if len(s)>0 else None

def extractResults(results, dbSet):
    """
    If we can't find a match in the lab's mbids, store a list of
    all the possible matches from musizbrainz
    """

    if results is None:
        return ""

    found = []
    for r in results:
        if r.item.getMbid() in dbSet:
            found = r.item.getMbid()
            break
        else:
            found.append( (r.item.getMbid(), r.score) )
            
    return found


def resolve_with_sitm(tags_dump_path, check_in_sitm_for_preresolved=True):
    """
    Resolves songs and albums contained in a pickled map for a series of tags
    to mbids. It tries to match the songs/albums to mbids of items we have in
    the sitm music db. If we do not have any of the candidate items, we store
    a list of all the mbids returned by musicbrainz

    tags_dump_path: path to file generated with crawl_tags_from_file()
    check_in_sitm_for_preresolved : if an item is already resolved, set wether
                    to assert it is in sitm db. If check is made and mbid not
                    in sitmdb, a list of possible matches will be fetched
    """

    mb = J.JClass("com.sun.labs.aura.music.web.musicbrainz.MusicBrainz")()

    # These files can be obtained using udm's sitm webservices
    sitm_tracks = load_sitm_tracks("sitm_tracks.txt")
    sitm_albums = load_sitm_albums("sitm_albums.txt")
    #sitm_artists = load_sitm_artists("sitm_artists.txt")

    tags = pickle.load(open(tags_dump_path, "r"))

    for tag_i, tag_name in enumerate(tags.keys()):
        print "%d/%d  Tag:%s" % (tag_i, len(tags.keys()), tag_name)
        crawl_data = tags[tag_name]

        # Resolve albums
        print "   Resolving albums"
        albums = crawl_data["album"]

        cnt_new_resolved = 0
        cnt_multi_resolved = 0
        cnt_were_resolved = 0
        cnt_total = 0
        for i, data in enumerate(albums):
            sys.stdout.write(".")
            sys.stdout.flush()
            cnt_total += 1
            # Backward comptability. Eventualy remove this
            if isinstance(data, tuple):
                data = [x for x in data]
                albums[i] = data


            # If we don't already have this mbid resolved
            if isMbid(data[1]):
                if not check_in_sitm_for_preresolved or data[1] in sitm_albums:
                    cnt_were_resolved+=1
                    continue
            elif isinstance(data[1], list):
                cnt_multi_resolved+=1
                continue


            # Query musizbrainz
            results = None
            tries = 0
            while results==None:
                try:
                    results = j2py( mb.albumSearch(data[2], noneIfEmpty(data[4]), noneIfEmpty(data[3])) )
                except Exception as ex:
                    tries += 1
                    print ex
                    if tries < 5:
                        print ">>>>> Trying again (%d)" % tries
                        time.sleep(1)
                    else:
                        print ">>>>> Giving up (%d)" % tries
                        break

            # Replace with new mbid
            data[1] = extractResults(results, sitm_albums)

            if isinstance(data[1], list):
                cnt_multi_resolved+=1
            elif (isinstance(data[1], str) or isinstance(data[1], unicode)) and isMbid(data[1]):
                cnt_new_resolved+=1

            albums[i] = data

        crawl_data["album"] = albums
        print("     New:%d   Multi:%d   Were:%d    Total:%d" % (cnt_new_resolved, cnt_multi_resolved, cnt_were_resolved, cnt_total))

        # Resolve tracks
        print "   Resolving tracks"
        tracks = crawl_data["track"]

        cnt_new_resolved = 0
        cnt_multi_resolved = 0
        cnt_were_resolved = 0
        cnt_total = 0
        for i, data in enumerate(tracks):
            sys.stdout.write(".")
            sys.stdout.flush()
            cnt_total += 1
            # Backward comptability. To eventualy remove
            if isinstance(data, tuple):
                data = [x for x in data]
                tracks[i] = data

            # If we have a string that's the right length in the mbid field, pass
            if isMbid(data[1]):
                if not check_in_sitm_for_preresolved or data[1] in sitm_tracks:
                    cnt_were_resolved+=1
                    continue
            elif isinstance(data[1], list):
                cnt_multi_resolved+=1
                continue

            # String title, String artistName, String artistMbid, String albumName, String albumMbid
            results = None
            tries = 0
            while results==None:
                try:
                    results = j2py( mb.trackSearch( data[2], noneIfEmpty(data[4]), noneIfEmpty(data[3]), None, None) )
                except Exception as ex:
                    tries += 1
                    print ex
                    if tries < 5:
                        print ">>>>> Trying again (%d)" % tries
                        time.sleep(1)
                    else:
                        print ">>>>> Giving up (%d)" % tries
                        break

            data[1] = extractResults(results, sitm_tracks)

            if isinstance(data[1], list):
                cnt_multi_resolved+=1
            elif (isinstance(data[1], str) or isinstance(data[1], unicode)) and isMbid(data[1]):
                cnt_new_resolved+=1

            tracks[i] = data

        crawl_data["track"] = tracks
        print("     New:%d   Multi:%d   Were:%d    Total:%d" % (cnt_new_resolved, cnt_multi_resolved, cnt_were_resolved, cnt_total))



        tags[tag_name] = crawl_data

    return tags



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


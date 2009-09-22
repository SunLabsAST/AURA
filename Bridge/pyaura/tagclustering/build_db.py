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
import threading
import time
import re
import Queue
import jpype as J
import pyaura.bridge as B
import pyaura.timestats as TS
import cPickle as P


all_tags = {}
jobs = Queue.Queue()


def do_download(output_folder, save_tagdata=True, regHost="brannigan"):
    """
    Downloads tag data form the store. Splits up vectors with items that have 
    been tagged in multiple files and stores an index in alltags.dump with
    statistics
    """

    aB = B.AuraBridge(regHost=regHost)

    iT_artist = J.JClass("com.sun.labs.aura.datastore.Item$ItemType").valueOf("ARTIST")
    iT_track = J.JClass("com.sun.labs.aura.datastore.Item$ItemType").valueOf("TRACK")

    jobsProcessor = ProcessJobs(output_folder, aB.get_item_count("ARTIST_TAG_RAW"), save_tagdata=save_tagdata)
    jobsProcessor.start()

    # Iterate through all the raw tags
    for tag_nbr, tag in enumerate(aB.get_all_iterator("ARTIST_TAG_RAW")):
        #print "   Adding '%s' to queue" % tag.name

        vals = {}
        for iT, name in [ (iT_artist, "a"), (iT_track, "t") ]:
            vals[name] = {}
            for rel in tag.getTaggedItems(iT).iterator():
                vals[name][rel.name] = rel.count
        jobs.put( (tag.name, vals) )

    jobsProcessor.done = True

    # Wait for the job thread to finish
    print ">> Waiting for the jobs thread to finish..."
    jobsProcessor.join()

    print ">> Saving all_tags dict..."
    save_path = os.path.join(output_folder, "alltags.dump")
    P.dump(all_tags, open(save_path, "w"))



def load_partial_download(folder):
    """
    This should only be used if a partial download is stopped and you need to
    build the all_tags dict from the tagdata.dump files
    """

    for filename in os.listdir(folder):
        match = re.search(r"tagcut-([\d]+)\.tagdata\.dump", filename, re.IGNORECASE)
        if match:
            print "  Processing %s" % filename
            file_idx = match.group(1)

            fdata = P.load(open(os.path.join(folder, filename)))
            for name, val in fdata.iteritems():

                # Compute tag statistics and save tag data in dict
                all_tags[name] = TagInfo(name, file_idx, val)


    print ">> Saving all_tags dict..."
    save_path = os.path.join(folder, "alltags.dump")
    P.dump(all_tags, open(save_path, "w"))




class ProcessJobs(threading.Thread):
    """
    Thread that will be doing the local work that does not require access to
    the JVM when we're downloading tagdata from the store
    """

    def __init__(self, output_folder, nbr_tags, save_tagdata=True):
        threading.Thread.__init__(self)
        self.output_folder = output_folder
        self.done = False
        self.nbr_tags = nbr_tags
        self.save_tagdata = save_tagdata
        self.timeStats = TS.TimeStats(total=nbr_tags, echo_each=100)

    def run(self):

        current_idx = 0
        file_idx = 0
        output_dict = {}

        while not self.done or jobs.qsize()>0:

            if jobs.qsize()==0:
                time.sleep(1)
                continue

            current_idx += 1
            tag_name, tag = jobs.get()

            # Compute tag statistics
            tag_info = TagInfo(tag_name, file_idx, tag)


            # Save tag data in dict
            all_tags[tag_name] = tag_info

            if self.save_tagdata:
                output_dict[tag_name] = tag
                if current_idx >= 2000:
                    #print ">>>> Saving file cut %d" % file_idx
                    save_path = os.path.join(self.output_folder, "tagcut-%d.tagdata.dump" % file_idx)
                    P.dump(output_dict, open(save_path, "w"))

                    output_dict = {}
                    current_idx = 0
                    file_idx+=1
            
            self.timeStats.next()




class TagInfo():

    def __init__(self, name, file_location=None, tagData=None):
        self.name = name
        self.file_location = file_location
        
        self.item_count = {}
        self.totals = {}
        if not tagData is None:
            self.item_count = ( len(tagData["a"]), len(tagData["t"]))
            self.totals = ( sum(tagData["a"].values()), sum(tagData["t"].values()) )


    def get_key(self):
        return "artist-tag-raw:%s" % self.name


    def _get_fnct(self, fnct, itemtype):
        if itemtype in ["a", "artist"]:
            return fnct[0]
        elif itemtype in ["t", "track"]:
            return fnct[1]
        else:
            raise KeyError("Invalid itemtype")


    def get_itemcount(self, itemtype="a"):
        return self._get_fnct(self.item_count, itemtype)


    def get_totals(self, itemtype="a"):
        return self._get_fnct(self.totals, itemtype)


    def __repr__(self):
        return "TagInfo<'%s' item_cnt:%d total_app:%d>" % (self.name, self.get_itemcount("a"), self.get_totals("a"))

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
import pyaura.lib as L
import pyaura.bridge as B
import pyaura.timestats as TS
import cPickle as P


all_tags = {}
jobs = Queue.Queue()


def do_download(output_folder):
    """
    Downloads tag data form the store. Splits up vectors with items that have 
    been tagged in multiple files and stores an index in alltags.dump with
    statistics
    """

    L.init_jvm("/home/mailletf/Desktop/jdk1.6.0_14/jre/lib/amd64/server/libjvm.so")

    iT_artist = J.JClass("com.sun.labs.aura.datastore.Item$ItemType").valueOf("ARTIST")
    iT_track = J.JClass("com.sun.labs.aura.datastore.Item$ItemType").valueOf("TRACK")

    aB = B.AuraBridge("/home/mailletf/Desktop/jdk1.6.0_14/jre/lib/amd64/server/libjvm.so")

    jobsProcessor = ProcessJobs(output_folder, aB.get_item_count("ARTIST_TAG_RAW"))
    jobsProcessor.start()

    # Iterate through all the raw tags
    for tag_nbr, tag in enumerate(aB.get_all_iterator("ARTIST_TAG_RAW")):
        #print "   Adding '%s' to queue" % tag.name

        vals = {}
        for iT, name in [ (iT_artist, "artist"), (iT_track, "track") ]:
            vals[name] = {}
            for rel in tag.getTaggedItems(iT).iterator():
                vals[name][rel.name] = rel.count
        jobs.put( (tag.name, vals) )

        #if tag_nbr>=31:
        #    print "Early stop"
        #    break

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

            if save_tagdata:
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
            for name in ("artist", "track"):
                self.item_count[name] = len(tagData[name])
                self.totals[name] = sum(tagData[name].values())


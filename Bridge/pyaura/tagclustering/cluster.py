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
import re
import cPickle as C
import jpype as J
import pyaura.bridge as B
import pyaura.timestats as TS
from pyaura.lib import j2py

try:
    import Levenshtein as Leven
except ImportError:
    print "Unable to import pylevenshtein, which is required to compute \
            string similarity. Get it from http://code.google.com/p/pylevenshtein"



DATA_PREFIX= "out500k"


# Length of the actual tag name
MIN_LENGTH=2
MAX_LENGTH=35

MIN_APPLIED_ITEM_CNT = 20
MIN_APPLICATIONS = 500


class Cluster():

    def __init__(self, prefix=DATA_PREFIX, regHost="brannigan"):

        self.tags = C.load(open(os.path.join(prefix, "alltags.dump")))
        self.wsf = None

        if regHost!=None:
            self._aB = B.AuraBridge(regHost=regHost)
        else:
            self._aB = None


    def cluster(self):


        print "Init with %d tags." % len(self.tags)

        print "Cleaning tags..."
        self.tags = self.lexical_sim_remove_stopwords(self.tags)
        print "  %d remaining" % len(self.tags)

        print "Combining whitespaces..."
        self.wsf = self.lexical_sim_remove_ws(self.tags)
        self.tags = None
        print "  %d remaining" % len(self.wsf)

        print "Combining plural..."
        self.wsf = self.lexical_sim_remove_plural(self.wsf)
        print "  %d remaining" % len(self.wsf)

        print "Removing TES not used enough..."
        self.wsf = self.lexical_sim_remove_notused(self.wsf)
        print "  %d remaining" % len(self.wsf)

        print "Combining based on edit distance & artist space similarity..."
        self.wsf = self.lexical_sim_ratio(self.wsf)
        print "  %d remaining" % len(self.wsf)

        return wsf


    def lexical_sim_remove_stopwords(self, tagsDict):


        # Make sure tags were used enough and aren't too long and too short
        clean_tags = [(k, v) for k, v in tagsDict.iteritems() if len(k)>=MIN_LENGTH]

        # Make sure the tags aren't in the stop list
        # regexbuddy : (?:fuck|shit|favorite|seen[\w]*live)
        clean_tags = [(k, v) for k, v in clean_tags if not re.search(r"(?:f.ck|sh(?:i|1)t|wh(?:o|0)re|favorite|seen[\w]*live)", k, re.IGNORECASE)]

        clean_dict = {}
        clean_dict.update(clean_tags)
        return clean_dict


    def lexical_sim_remove_ws(self, tags):

        # Build a whitespace free dict
        wsf = {}
        for tagkey in tags.iterkeys():
            # music ?
            wsf_tag = tagkey.replace(" ", "").replace("_", "").replace("-", "").replace("'", "").replace("/", "").\
                             replace(":", "").replace(".", "").replace(",", "").replace("~", "").replace("!", "").\
                             replace("*", "").replace("?", "").lower()
            tlen = len(wsf_tag)
            if tlen<MIN_LENGTH or tlen>MAX_LENGTH:
                continue

            if wsf_tag not in wsf:
                wsf[wsf_tag] = TermEquivalenceSet(wsf_tag, tags[tagkey])
            else:
                wsf[wsf_tag].add_tag( tags[tagkey] )

        return wsf


    def lexical_sim_remove_plural(self, wsf):

        no_s = set()
        with_s = set()
        for tag in wsf.keys():
            if tag[-1]=="s":
                with_s.add(tag)
            else:
                no_s.add(tag)

        for tag in with_s:
            if tag[:-1] in no_s:
                wsf[tag[:-1]].merge_tes( wsf.pop(tag) )

        return wsf


    def lexical_sim_remove_notused(self, wsf):

        # Remove any tes for which the combined values of item counts
        # or applications isn't high enough
        for key,tes in wsf.items():
            if tes.get_itemcounts("a")<MIN_APPLIED_ITEM_CNT or tes.get_totals("a")>=MIN_APPLICATIONS:
                blackhole = wsf.pop(key)
        return wsf



    def lexical_sim_ratio(self, wsf):

        timeStats = TS.TimeStats(total=len(wsf), echo_each=25)

        outfile = open("tes_merge.txt", "w")

        if self._aB==None:
            raise RuntimeError("AuraBridge needs to be initialised for lexical_sim_ratio to run. "+ \
                                        "You need to specify a regHost to the constructor.")

        removed_keys = {}

        skeys = wsf.keys()
        for tag in skeys:

            needs2break = False
            timeStats.next()


            # If this tag has been merged with anoter tes
            if tag in removed_keys:
                continue

            for sndtag in wsf.keys():

                if tag==sndtag or sndtag in removed_keys:
                    continue

                # If they have a small enough edit distance
                ratio = Leven.ratio(tag, sndtag)
                if Leven.ratio(tag, sndtag)>0.75:
                    # If they have a small enough similarity in the space of tagged artists
                    sim = self._aB.get_tag_similarity("artist-tag-raw:"+wsf[tag].representative_tag.name,
                                                      "artist-tag-raw:"+wsf[sndtag].representative_tag.name)
                    if sim>0.75:
                        outfile.write("%s (%d) <-> %s (%d)   ed:%0.2f  sim:%0.2f\n" %
                                                   (wsf[tag].get_name().encode("utf8"), len(wsf[tag]),
                                                    wsf[sndtag].get_name().encode("utf8"), len(wsf[sndtag]),
                                                    ratio, sim))
                        outfile.write("   mult:%0.4f     avg:%0.4f\n" % ((ratio*sim), (ratio+sim)/2))

                    merge_ratio = (ratio+sim)/2.
                    if merge_ratio > 0.8:
                        # determine the most popular
                        if wsf[tag].get_totals()>wsf[sndtag].get_totals():
                            alpha, beta = (tag, sndtag)
                        else:
                            needs2break = True
                            alpha, beta = (sndtag, tag)
                        removed_keys[beta] = wsf[beta]
                        wsf[alpha].merge_tes( wsf.pop(beta) )

                        if needs2break:
                            break

        return wsf




class TermEquivalenceSet():

    def __init__(self, wsf_name, tag):
        self.wsf_name = wsf_name
        self._tags = [tag]
        self.representative_tag = tag

        # Parent tags are tags which are more general than the current tag
        # ex: "rock" is a parent of "piano rock"
        self._parent_tags = set()
        # Child tags are the inverse
        # ex: "piano rock" is a child of "rock"
        self._child_tags = set()


    def get_name(self):
        return self.representative_tag.name


    def add_tag(self, new_tag):
        self._tags.append(new_tag)

        if new_tag.get_itemcount("a")>self.representative_tag.get_itemcount("a"):
            self.representative_tag = new_tag


    def merge_tes(self, snd_tes):
        """
        Combine the tags from two TES
        """
        if snd_tes.representative_tag.get_itemcount("a")>self.representative_tag.get_itemcount("a"):
            self.representative_tag = snd_tes.representative_tag

        for t in snd_tes.get_tags():
            self._tags.append(t)

        self._parent_tags = self._parent_tags.union( snd_tes.get_parent_tags() )
        self._child_tags = self._child_tags.union( snd_tes.get_child_tags() )


    def add_parent_tag(self, tag):
        self._parent_tags.add(tag)


    def add_child_tag(self, tag):
        self._child_tags.add(tag)


    def get_parent_tags(self):
        return self._parent_tags


    def get_child_tags(self):
        return self._child_tags


    def get_itemcounts(self, itemtype="a"):
        """
        Get the item counts for the TES by combining the items counts of all
        the tags it contains.
        """
        return sum([t.get_itemcount(itemtype) for t in self._tags])


    def get_totals(self, itemtype="a"):
        """
        Get the total application count for the TES by combining the
        application counts of all the tags it contains.
        """
        return sum([t.get_totals(itemtype) for t in self._tags])


    def get_tags(self):
        return self._tags


    def __len__(self):
        return len(self._tags)


    def __repr__(self):
        return "TES<'%s' key:'%s' len:%d pt:%d ct:%d>" % (self.representative_tag.name, self.wsf_name,
                    len(self._tags), len(self._parent_tags), len(self._child_tags))



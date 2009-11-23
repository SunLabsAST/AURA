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
import cPickle as C
import pyaura.bridge as B
from pyaura.lib import j2py



DATA_PREFIX= "out500k"


class SimpleStats():
    """
    Generate simple statistics and plots for a set of tags.
    """
    def __init__(self, prefix=DATA_PREFIX, regHost="brannigan"):

        self.tags = C.load(open(os.path.join(prefix, "alltags.dump")))
        self._dataFileCache = DataFileCache(prefix, self.tags)

        self._is_plot_init = False

        if regHost!=None:
            self._aB = B.AuraBridge(regHost=regHost)
        else:
            self._aB = None
        



    def _init_plotting(self, backend="MacOSX"):
        if not self._is_plot_init:
            self._is_plot_init = True
            import matplotlib as M
            M.use(backend)


    def plot_taglen_hist(self):
        self._init_plotting()
        import pylab
        tag_len = [len(x) for x in self.tags.keys() ]
        pylab.hist(tag_len)


    def plot_tagpop_itemcnt(self, itemtype="a"):
        """
        Tag the histogram of the number of tagged items for each tag
        """
        self._init_plotting()
        import pylab
        itemcnt = [x.get_itemcount(itemtype) for x in self.tags.values()]
        pylab.hist(itemcnt, bins=1000)


    def plot_tagpop_appcnt(self, itemtype="a"):
        """
        Tag the histogram of application counts for each tag
        """
        self._init_plotting()
        import pylab
        appcnt = [x.get_totals(itemtype) for x in self.tags.values()]
        pylab.hist(appcnt, bins=1000)


    def get_average_len(self):

        w_running_tot=0
        running_tot=0
        tot_cnt=0
        nbr_cnt=0
        for t in self.tags.values():
            w_running_tot+=len(t.name)*t.totals['artist']
            running_tot+=len(t.name)
            tot_cnt+=t.totals['artist']
            nbr_cnt+=1
        print "Weighted avg:\t%0.4f" % (float(w_running_tot)/tot_cnt)
        print "Avg:\t\t%0.4f" % (float(running_tot)/nbr_cnt)


    def find_most_co_ocurr(self, tagname, n=10):

        vals = {}
        for t in self.tags:
            vals[t] = self.co_ocurr(tagname, t)

        return sorted(vals.items(), key=lambda (k,v): (v,k), reverse=True)


    def find_similar(self, tagname, n=10):

        return j2py( self._aB.mdb.find_similar_rawtags(tagname, n) )




    def co_ocurr(self, tagname1, tagname2):
        """
        Get relative co-occurence (Jaccard coefficient)
        """

        tagdata1 = self._dataFileCache.get(tagname1)
        tagdata2 = self._dataFileCache.get(tagname2)

        kt1 = frozenset(tagdata1['artist'].keys())
        kt2 = frozenset(tagdata2['artist'].keys())

        return float(len(kt1.intersection(kt2))) / len(kt1.union(kt2))





class DataFileCache():

    def __init__(self, prefix, taginfo):
        self._prefix = prefix
        self._taginfo = taginfo
        self._datafiles = {}

    def get(self, tagname):

        if tagname not in self._taginfo:
            raise KeyError("Tag '%s' is not in the db" % tagname)

        file_id = self._taginfo[tagname].file_location

        if not file_id in self._datafiles:
            print "  > Loading datafile %s" % file_id
            path = os.path.join(self._prefix, "tagcut-%s.tagdata.dump" % file_id)
            self._datafiles[file_id] = C.load(open(path))
        return self._datafiles[file_id][tagname]


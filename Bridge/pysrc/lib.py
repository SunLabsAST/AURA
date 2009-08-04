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
import os
import warnings


DEFAULT_GRID_REGHOST="172.16.136.2"


def init_jvm(jvm_path=J.getDefaultJVMPath(), classpath_prefix=os.path.join("..", "dist"), 
                max_heap="2G", regHost=DEFAULT_GRID_REGHOST):

    cP = os.path.join(classpath_prefix, "Bridge.jar")
    J.startJVM(jvm_path, "-Xmx"+max_heap, "-DauraGroup=live-aura", "-DauraHome=/aura/sitm/db/", \
                        "-DauraPolicy=/aura/sitm/dist/jsk-all.policy", \
                        "-DregHost="+regHost, "-Djava.class.path="+cP)




####################################
#        Utility functions         #
####################################

def _lst_to_jarraylist(l):
    aL = J.java.util.ArrayList()
    for k in l:
        aL.add(k)
    return aL


def j2py(elem):
    """
    Tries to convert java data types and data structures to native python. Also
    tries to convert ItemImpl objects to their specific item types.
    """
    if isinstance(elem, J.JClass("java.util.HashMap")):
        r = {}
        for eS in elem.entrySet().iterator():
            r[ j2py(eS.getKey()) ] = j2py(eS.getValue())
        return r

    elif isinstance(elem, J.JClass("java.util.HashSet")):
        r = set()
        for e in elem.iterator():
            r.add( j2py(e) )
        return r

    elif isinstance(elem, J.JClass("java.util.ArrayList")):
        return [ j2py(elem.get(x)) for x in xrange(elem.size())]

    elif isinstance(elem, J.JClass("java.lang.Long")):
        return elem.longValue()

    elif isinstance(elem, J.JClass("java.lang.Integer")):
        return elem.intValue()

    elif isinstance(elem, J.JClass("com.sun.labs.aura.util.Scored")):
        return PyScored( j2py(elem.getScore()), j2py(elem.getItem()) )

    elif isinstance(elem, J.JClass("com.sun.labs.aura.datastore.impl.store.persist.ItemImpl")):

        item_type = J.JClass("com.sun.labs.aura.datastore.Item$ItemType")

        if elem.getType()==item_type.ALBUM:
            return J.JClass("com.sun.labs.aura.music.Album")(elem)
        elif elem.getType()==item_type.ARTIST:
            return J.JClass("com.sun.labs.aura.music.Artist")(elem)
        elif elem.getType()==item_type.TRACK:
            return J.JClass("com.sun.labs.aura.music.Track")(elem)
        elif elem.getType()==item_type.USER:
            return J.JClass("com.sun.labs.aura.music.Listener")(elem)
        elif elem.getType()==item_type.ARTIST_TAG:
            return J.JClass("com.sun.labs.aura.music.ArtistTag")(elem)
        elif elem.getType()==item_type.ARTIST_TAG_RAW:
            return J.JClass("com.sun.labs.aura.music.ArtistTagRaw")(elem)
        else:
            warnings.warn("Conversion of ItemType "+elem.getType().toString()+" to it's native type is not yet implemented")
            return elem


    else:
        return elem




####################################
#     Type assertions functions    #
####################################
def _assert_type_attn_config(attn_config):
    if not isinstance(attn_config, J.JClass("com.sun.labs.aura.datastore.AttentionConfig")):
        raise WrongTypeException("Must use a valid attention config object. Use ClassFactory.new_attention_config()")

def _assert_type_itemtype(itemType):
    iT_class = J.JClass("com.sun.labs.aura.datastore.Item$ItemType")
    if isinstance(itemType, str):
        itemType = iT_class.valueOf(itemType)
    elif not isinstance(itemType, iT_class):
        raise WrongTypeException("Must use valid item type.")
    return itemType





class WrongTypeException(Exception):
    def __init__(self, value):
        self.value = value

    def __str__(self):
        return repr(self.value)





class ClassFactory():

    @staticmethod
    def new_attention_config():
        """Returns a new AttentionConfig object"""
        return J.JClass("com.sun.labs.aura.datastore.AttentionConfig")()



class PyScored():

    def _init_(self, score, item):
        self.score = score
        self.item = item

    def __repr__(self) :

        name = None
        if isinstance(self.item, str):
            name = self.item
        elif type(self.item).startswith("com.sun.labs.aura.music") and \
                type(self.item)[24:] in ["Album", "Artist", "Track", "Listener"]:
            for f in [ lambda x: x.getName(), lambda x: x.getTitle() ]:
                try:
                    name = type(self.item)[24:] + ":" + f(self.item)
                except AttributeError:
                    continue
        else:
            name = type(self.item)

        return "<Scored[%0.3f](%s)>" % (self.score, name)



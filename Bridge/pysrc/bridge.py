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

class AuraBridge():

    def __init__(self, classpath_prefix=os.path.join("..", "dist")):
    
	cP = os.path.join(classpath_prefix, "Bridge.jar")
        J.startJVM(J.getDefaultJVMPath(), "-DauraGroup=live-aura", "-DauraHome=/aura/sitm/db/", \
                            "-DauraPolicy=/aura/sitm/dist/jsk-all.policy", "-Djava.class.path="+cP)

        AuraBridge = J.JClass("com.sun.labs.aura.bridge.AuraBridge")
        self._bridge = AuraBridge()


    def getItem(self, key):
        """
        Gets an item from the store
        """
	return self._bridge.pyGetItem(key)


    def getItems(self, keys):
        """
        Gets a list of items from the store
        """
        if not isinstance(keys, list):
            raise WrongTypeException("keys argument should be a list")

        aL = self._bridge.getItems(_lst_to_jArrayList(keys))
        return _jArrayList_to_lst(aL)


    def getAll(self, itemType):
        """
        Gets all items of a particular type from the store.
        This can be **VERY** long  
        """
        jItemName = J.JClass("com.sun.labs.aura.datastore.Item").ItemType.valueOf(itemType)
        jL = self._bridge.getAll(jItemName)
        return _jArrayList_to_lst(jL)





####################################
#        Utility functions         #
####################################
def _jArrayList_to_lst(aL):
    return [aL.get(x) for x in xrange(aL.size())]            

def _lst_to_jArrayList(l):
    aL = J.java.util.ArrayList()
    for k in l:
        aL.add(k)
    return aL





class WrongTypeException(Exception):
    def __init__(self, value):
        self.value = value
    
    def __str__(self):
        return repr(self.value)

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
import lib as L
from lib import j2py


class AuraBridge():
    """
    This class serves as a bridge between python code and the Aura datastore by 
    implementing the datastore and musicdb APIs.
    """

    def __init__(self, jvm_path=J.getDefaultJVMPath(),
                    classpath_prefix=L._get_default_prefix(),
                    regHost=L.DEFAULT_GRID_REGHOST):

        if J.isJVMStarted()==0:
            L.init_jvm(jvm_path=jvm_path, classpath_prefix=classpath_prefix, regHost=regHost)
        
        AuraBridge = J.JClass("com.sun.labs.aura.bridge.AuraBridge")
        self._bridge = AuraBridge()
        self.mdb = MusicDatabase(self._bridge)


    #############################################
    #   Implementation of ItemStore interface   #
    #############################################
    def flush_item(self, item):
        """Save the item in the store"""
        self._bridge.flushItem(item)


    def get_item(self, key):
        """Gets an item from the store"""
	return j2py(self._bridge.getItem(key))


    def get_user(self, key):
        """Gets a user from the store"""
	return j2py(self._bridge.getUser(key))


    def get_items(self, keys):
        """Gets a list of items from the store"""
        if not isinstance(keys, list):
            raise WrongTypeException("keys argument should be a list")

        aL = self._bridge.getItems(_lst_to_jArrayList(keys))
        return j2py(aL)


    def get_all(self, itemType):
        """
        Gets all items of a particular type from the store.
        This can be **VERY** long. Consider using get_all_iterator()
        """
        itemType = L._assert_type_itemtype(itemType)
        jL = self._bridge.getAll(itemType)
        return j2py(jL)


    def get_attention_count(self, attn_config):
        """Gets the attention count for a particular attention configuration"""
        L._assert_type_attn_config(attn_config)
        return self._bridge.getAttentionCount(attn_config).longValue()


    def get_attention_since(self, attn_config, timestamp):
        """Gets attentions created since timestamp, given an attention configuration"""
        L._assert_type_attn_config(attn_config)
        aL = self._bridge.getAttentionSince(attn_config, J.java.util.Date(timestamp))
        return j2py(aL)


    def get_attention_since_count(self, attn_config, timestamp):
        """Gets the attention count created since timestamp for a particular attention configuration"""
        L._assert_type_attn_config(attn_config)
        return self._bridge.getAttentionSinceCount(attn_config, J.java.util.Date(timestamp)).longValue()


    def get_last_attention(self, attn_config, count=10):
        """Get the count last attentions given an attention configuration object"""
        L._assert_type_attn_config(attn_config)
        return self._bridge.getLastAttention(attn_config, count)


    def get_item_count(self, itemType):
        """Gets the count for an item type"""
        if isinstance(itemType, str):
            itemType = J.JClass("com.sun.labs.aura.datastore.Item$ItemType").valueOf(itemType)
        return self._bridge.getItemCount(itemType)


    def delete_item(self, key):
        """Deletes an item from the store"""
        self._bridge.deleteItem(key)


    def get_top_values(self, field, n=100, ignore_case=False):
        """Gets the most frequent values for the named field"""
        return j2py(self._bridge.getTopValues(field, n, ignore_case))



    ####################################
    #       Iterator functions         #
    ####################################
    def get_all_iterator(self, itemType):
        """Gets an iterator over all items of specified type"""
        itemType = L._assert_type_itemtype(itemType)
        it_id = self._bridge.allItemsIteratorInit(itemType)
        return self._iterator_loop(it_id)


    def get_items_added_since_iterator(self, itemType, timestamp):
        """Gets an iterator over all items of specified type added since the specified timestamp"""
        itemType = L._assert_type_itemtype(itemType)
        timestamp = J.java.util.Date(timestamp)
        it_id = self._bridge.initGetItemsAddedSinceIterator(itemType, timestamp)
        return self._iterator_loop(it_id)


    def _iterator_loop(self, it_id):
        """Actual iterator loop used by all our iterators"""
        try:
            while True:
                try:
                    nextVal = self._bridge.iteratorNext(it_id)
                    if not nextVal is None:
                        yield j2py(nextVal)
                    else:
                        break
                except:
                    #todo fix this to catch only the right exception
                    print "Iterator end"
                    break

        finally:
            self._bridge.iteratorClose(it_id)




#####################################
#     Implementation of MusicDb     #
#####################################
class MusicDatabase():

    def __init__(self, bridge):
        self._bridge = bridge


    def get_favorite_artist_keys(self, listenerId, max=10):
        """Gets the favorite artists' keys for a listener"""
        keys = self._bridge.getMdb().getFavoriteArtistKeys(listenerId, max)
        return j2py(keys)


    def get_favorite_artists(self, listenerID, max=10):
        """Gets the Favorite artists IDs for a listener"""
        a = self._bridge.getMdb().getFavoriteArtists(listenerID, max)
        return j2py(a)


    def artist_search(self, artistName, return_count=10):
        """Searches for artists that match the given name"""
        sA = self._bridge.getMdb().artistSearch(artistName, return_count)
        return j2py(sA)


    def get_tags(self, taggable_item, tag_type="SOCIAL"):
        """
        Given a taggable item and a tag_type, returns its tags

        Possible tag_type values : SOCIAL, SOCIAL_RAW, BIO, BLURB, AUTO, REVIEW_EN, BLOG_EN
        """
        tt = J.JClass("com.sun.labs.aura.music.TaggableItem$TagType").valueOf(tag_type)
        return j2py( taggable_item.getTags(tt) )


    def find_similar_rawtags(self, tagname, count=25):
        """
        
        """
        return j2py( self._bridge.getMdb().findSimilar("artist-tag-raw:"+tagname, count) )

package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.util.AuraException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A binary tree whose leaf nodes point to particular partition clusters that
 * are associated with items that have a common prefix in their key's hashcode.
 * The path through the tree is described by the prefix.
 */
public class BinaryTrie<E> implements Serializable {
    
    /**
     * The root of the Trie
     */
    private TrieNode root;
    
    /**
     * Contents of the trie, in no particular order
     */
    private Set<E> contents;
    
    private transient ReadWriteLock lock;
    
    public BinaryTrie() {
        root = new TrieNode();
        contents = new HashSet<E>();
        lock = new ReentrantReadWriteLock();
    }
    
    /**
     * Adds a new element to the tree with the specified prefix.  If the
     * length of the prefix exceeds the length of the tree, new nodes will
     * be created to accomodate the prefix.  Note that this could leave
     * null leaf nodes if elements are not added to fill in the blanks.
     * 
     * @param newElem the element to add
     * @param prefix the prefix describing where the element should be added
     */
    public void add(E newElem, DSBitSet prefix) {
        lock.writeLock().lock();
        try {
            contents.add(newElem);
            add(newElem, prefix, root, 0);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    protected void add(E newElem, DSBitSet prefix, TrieNode curr, int offset) {
        //
        // The base case:
        if (offset == (prefix.prefixLength() - 1)) {
            //
            // We've traversed to where we want to insert.  Insert either to
            // the left or right depending on the value.  If this is a leaf
            // node, we lose the leaf object (?)
            if (prefix.get(offset)) {
                if (curr.getOne() != null) {
                    throw new IllegalStateException("A node already exists " +
                            " at the given prefix");
                }
                curr.setOne(new TrieNode(newElem));
            } else {
                if (curr.getZero() != null) {
                    throw new IllegalStateException("A node already exists " +
                            " at the given prefix");
                }
                curr.setZero(new TrieNode(newElem));
            }
            curr.setLeafObject(null);
        } else {
            //
            // We need to keep drilling down.  We'll lose a leaf if it was
            // in the way
            curr.setLeafObject(null);
            if (prefix.get(offset)) {
                if (curr.getOne() == null) {
                    curr.setOne(new TrieNode());
                }
                add(newElem, prefix, curr.getOne(), offset + 1);
            } else {
                if (curr.getZero() == null) {
                    curr.setZero(new TrieNode());
                }
                add(newElem, prefix, curr.getZero(), offset + 1);
            }
        }
        
    }
    
    /**
     * Get an element from the trie matching some bits of the provided prefix
     * 
     * @param prefix the prefix to match
     * @return the element at the leaf matching the initial bits of the prefix
     */
    public E get(DSBitSet prefix) {
        lock.readLock().lock();
        try {
            TrieNode curr = root;
            for (int i = 0; i < prefix.prefixLength(); i++) {
                //
                // If this is a leaf, then return it
                if (curr.getLeafObject() != null) {
                    return curr.getLeafObject();
                }
                if (prefix.get(i)) {
                    curr = curr.getOne();
                    if (curr == null) {
                        throw new IllegalStateException("Encountered null child " +
                                "at prefix " + prefix + " offset " + i);
                    }
                } else {
                    curr = curr.getZero();
                    if (curr == null) {
                        throw new IllegalStateException("Encountered null child " +
                                "at prefix " + prefix + " offset " + i);
                    }
                }
            }
        } finally {
            lock.readLock().unlock();
        }        
        //
        // If we made it all the way through and didn't
        // find anything, we're probably in trouble.
        throw new IllegalStateException("Trie too deep!");
    }
    
    /**
     * Get a new set containing all the elements of the trie
     * 
     * @return a new set containing all the elements of the trie
     */
    public Set<E> getAll() {
        lock.readLock().lock();
        try {
            return new HashSet<E>(contents);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Returns the number of elements in the trie
     * 
     * @return the number of elements in the trie
     */
    public int size() {
        lock.readLock().lock();
        try {
            return contents.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Determines if every leaf node of this trie has an element at it.
     * 
     * @return true if the tree is completed
     */
    public boolean isComplete() {
        lock.readLock().lock();
        try {
            return isComplete(root);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    protected boolean isComplete(TrieNode node) {
        if (node.getLeafObject() != null) {
            return true;
        } else {
            if (isComplete(node.getZero()) && isComplete(node.getOne())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * A node in the Trie
     */
    protected class TrieNode implements Serializable {
        /** If this is a leaf node, the object hanging at the leaf */
        private E leafObject = null;
        
        /** If this isn't a leaf, the left child */
        private TrieNode childZero = null;
        
        /** If this isn't a leaf, the right child */
        private TrieNode childOne = null;
        
        /**
         * Default constructor for no children or leaf objects
         */
        public TrieNode() {
        }
        
        /**
         * Construct a leaf node with the given leaf object
         * 
         * @param leaf the leaf
         */
        public TrieNode(E leaf) {
            this.leafObject = leaf;
        }
                
        /**
         * Split a leaf node, creating two sub nodes
         * 
         * @param zero the left child
         * @param one the right child
         * @throws com.sun.labs.aura.aardvark.util.AuraException
         */
        public void split(TrieNode zero, TrieNode one) throws AuraException {
            if (leafObject != null) {
                childZero = zero;
                childOne = one;
                leafObject = null;
            } else {
                throw new AuraException("Attempted to split a non-leaf node");
            }
        }

        public void setZero(TrieNode node) {
            this.childZero = node;
        }
        
        public void setOne(TrieNode node) {
           this.childOne = node;
        }
        
        public TrieNode getZero() {
            return childZero;
        }
        
        public TrieNode getOne() {
            return childOne;
        }
        
        public E getLeafObject() {
            return leafObject;
        }
        
        public void setLeafObject (E element) {
            leafObject = element;
        }
    }
}

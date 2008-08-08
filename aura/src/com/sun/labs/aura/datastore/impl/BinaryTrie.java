package com.sun.labs.aura.datastore.impl;

import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A binary tree whose leaf nodes point to particular partition clusters that
 * are associated with items that have a common prefix in their key's hashcode.
 * The path through the tree is described by the prefix.
 */
public class BinaryTrie<E> implements Serializable {
    private static final long serialVersionUID = 1L;
    Logger log;
    
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
        log = Logger.getLogger(getClass().getName());
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
        log.info("Adding element to tree for prefix: " + prefix);
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
        if(offset == (prefix.prefixLength() - 1)) {
            //
            // We've traversed to where we want to insert.  Insert either to
            // the left or right depending on the value.  If this is a leaf
            // node, we lose the leaf object (?)
            if(curr.getChild(prefix.getBit(offset)) != null) {
                throw new IllegalStateException("A node already exists" +
                            " at prefix " + prefix.get(offset));
            }
            curr.setChild(prefix.getBit(offset), new TrieNode(newElem));
            curr.setLeafObject(null);
            log.info("Set child at: " + prefix.getBit(offset) +
                     ": Offset: " + offset + ": Complete: " + isComplete());
        } else {
            //
            // We need to keep drilling down.  We'll lose a leaf if it was
            // in the way
            log.fine("Descending to add tree node for prefix '" + prefix + "' at offset " + offset);
            curr.setLeafObject(null);
            if(curr.getChild(prefix.getBit(offset)) == null) {
                curr.setChild(prefix.getBit(offset), new TrieNode());
            }
            add(newElem, prefix, curr.getChild(prefix.getBit(offset)), offset + 1);
        }
        
    }

    /**
     * Add a pair of leaf nodes together in one operation.  This is used when
     * the lock should be obtained a single time to add two items, keeping
     * the tree "complete" after the lock is released.
     * 
     * @param zeroChild the zero/left child to add
     * @param zeroPrefix the prefix for the zero/left child
     * @param oneChild the one/right child to add
     * @param onePrefix the prefix for the one/right child
     */
    public void addPair(E zeroChild, DSBitSet zeroPrefix,
                        E oneChild, DSBitSet onePrefix) {
        lock.writeLock().lock();
        try {
            contents.add(zeroChild);
            contents.add(oneChild);
            add(zeroChild, zeroPrefix, root, 0);
            add(oneChild, onePrefix, root, 0);
        } finally {
            lock.writeLock().unlock();
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
            for(int i = 0; i < prefix.prefixLength(); i++) {
                curr = curr.getChild(prefix.getBit(i));
                if(curr == null) {
                    String message = "Encountered null child for prefix " +
                                     prefix + " at offset " + i;
                    log.severe(message);
                    throw new IllegalStateException(message);
                }
                //
                // If this is a leaf, then return it
                if(curr.getLeafObject() != null) {
                    return curr.getLeafObject();
                }
            }
        } finally {
            lock.readLock().unlock();
        }
        //
        // If we made it all the way through and didn't
        // find anything, we're probably in trouble.
        String message = "No leaf object found for prefix: " + prefix;
        log.severe(message);
        throw new IllegalStateException(message);
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
        if(node == null) {
            return false;
        }
        return node.leafObject != null ||
               (isComplete(node.getChild(0)) && isComplete(node.getChild(1)));
    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        //
        // Get a read lock, write myself, release the read lock
        lock.readLock().lock();
        try {
            oos.defaultWriteObject();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        lock = new ReentrantReadWriteLock();
    }
    
    /**
     * A node in the Trie
     */
    protected class TrieNode {
        /** If this is a leaf node, the object hanging at the leaf */
        private E leafObject = null;
        
        /** If this isn't a leaf, the left child */
        private TrieNode[] children = (TrieNode[])Array.newInstance(TrieNode.class, 2);
        
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
            log.info("Splitting node");
            if(leafObject != null) {
                throw new AuraException("Attempted to split a non-leaf node");
            }
            setChild(0, zero);
            setChild(1, one);
            leafObject = null;
        }
        
        public void setChild(int index, TrieNode node) {
            this.children[index] = node;
        }

        public TrieNode getChild(int index) {
            return children[index];
        }
        
        public E getLeafObject() {
            return leafObject;
        }
        
        public void setLeafObject (E element) {
            if(leafObject != null) {
                log.severe("Replacing leaf object");
            }
            leafObject = element;
        }
    }
}
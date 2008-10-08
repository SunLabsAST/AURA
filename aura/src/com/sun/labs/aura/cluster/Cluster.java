/*
 * ClusterResultImpl.java
 *
 * Created on November 27, 2006, 7:48 AM
 *
 */

package com.sun.labs.aura.cluster;

import com.sun.labs.aura.datastore.Item;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A cluster of items.
 */
public class Cluster implements Comparable<Cluster>, Serializable {

    protected List<ClusterElement> elements;
    
    protected String name;

    /**
     * The most central result in this set.
     */
    protected Item mc;

    /**
     * The distance of the most central result from the centroid.
     */
    protected double mcDist;

    /**
     * The map from feature names to index in the point, which we
     * can use later to choose a representative set of words for the
     * cluster.
     */
    protected Map<String, Integer> features;

    /**
     * The centroid of the cluster.
     */
    protected double[] centroid;

    /**
     * Creates a cluster.
     */
    public Cluster(Map<String, Integer> features, double[] centroid) {
        this.features = features;
        if (centroid != null) {
            this.centroid = Arrays.copyOf(centroid, centroid.length);
        }
        mcDist = Double.MAX_VALUE;
        elements = new ArrayList<ClusterElement>();
    }

    /**
     * Adds an element to this cluster.
     */
    protected void add(ClusterElement el) {
        elements.add(el);
        el.dist(centroid);
        if(el.dist < mcDist) {
            mc = el.item;
            mcDist = el.dist;
        }
    }
    
    protected void finish() {
        for(ClusterElement el : elements) {
            el.dist(centroid);
        }
        Collections.sort(elements);
    }

    /**
     * Gets a description of this cluster as a list of at most the top n terms.
     */
    public List<String> getDescription(int n) {
        Map<Integer, HE> top = new HashMap<Integer, HE>();
        for(Map.Entry<String, Integer> e : features.entrySet()) {
            HE he = top.get(e.getValue());
            if(he == null) {
                he = new HE(e.getKey(), centroid[e.getValue()]);
                top.put(e.getValue(), he);
            } else {
                he.name.add(e.getKey());
                he.weight += centroid[e.getValue()];
            }
        }
        HE[] x = top.values().toArray(new HE[0]);
        com.sun.labs.minion.util.Util.sort(x);
        List<String> ret = new ArrayList<String>();
        for(int i = 0; i < n && i < x.length;
                i++) {
            ret.add(String.format("<%s, %.3f>", x[i].name.get(0), x[i].weight));
        }
        return ret;
    }

    /**
     * Gets the items that make up this cluster, in order of their distance to 
     * the cluster centroid.
     * @return
     */
    public List<ClusterElement> getMembers() {
        return elements;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int compareTo(Cluster o) {
        return o.elements.size() - elements.size();
    }
    
    public int hashCode() {
        return elements.hashCode();
    }
    
    /**
     * Returns true if all the elements are the same, the features are the same,
     * and the centroid is the same
     * @param o
     * @return
     */
    public boolean equals(Object o) {
        if (o instanceof Cluster) {
            Cluster oc = (Cluster)o;
            //
            // Same centroid?
            if (Arrays.equals(centroid, oc.centroid)) {            
                //
                // Same number of elements?
                if (elements.size() == oc.elements.size()) {
                    List elmns = new ArrayList(elements);
                    //
                    // Same exact elements?
                    elmns.removeAll(oc.elements);
                    if (elmns.isEmpty()) {
                        //
                        // Same features?
                        if (features.equals(oc.features)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public double distance(Item item) {
        return 0;
    }

    public Item getMostCentralResult() {
        return mc;
    }

    public int size() {
        return elements.size();
    }

    static class HE implements Comparable<HE> {

        public HE(String name, double weight) {
            this.name = new ArrayList<String>();
            this.name.add(name);
            this.weight = weight;
        }

        public boolean equals(Object o) {
            if (o instanceof HE) {
                HE ohe = (HE)o;
                //
                // Comparing floating point is inexact - they're the same if
                // they're very close.
                if (Math.abs(weight - ohe.weight) < .0000001) {
                    if (name.size() == ohe.name.size()) {
                        List nm = new ArrayList(name);
                        nm.removeAll(ohe.name);
                        if (nm.isEmpty()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
        
        public int hashCode() {
            return name.hashCode();
        }
        
        public int compareTo(Cluster.HE o) {
            if(weight < o.weight) {
                return 1;
            }

            if(weight > o.weight) {
                return -1;
            }

            return 0;
        }
        List<String> name;
        double weight;
    }
}

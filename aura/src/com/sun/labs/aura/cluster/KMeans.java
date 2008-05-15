/*
 *   Copyright 2007 Sun Microsystems, Inc. All rights reserved
 * 
 *   Use is subject to license terms.
 */
/*
 * KMeans.java
 *
 * Created on November 27, 2006, 7:17 AM
 *
 */

package com.sun.labs.aura.cluster;

import com.sun.labs.aura.datastore.impl.DataStoreHead;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.minion.classification.Feature;
import com.sun.labs.minion.classification.FeatureCluster;
import com.sun.labs.minion.classification.FeatureClusterSet;
import com.sun.labs.minion.classification.WeightedFeature;
import com.sun.labs.minion.classification.WeightedFeatureClusterer;
import com.sun.labs.minion.classification.WeightedFeatureSelector;
import com.sun.labs.minion.clustering.ClusterUtil;
import com.sun.labs.minion.retrieval.DocumentVectorImpl;
import com.sun.labs.minion.util.StopWatch;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A K means clustering algorithm for items from a data store.
 *
 * @author Stephen Green <stephen.green@sun.com>
 */
public class KMeans {
    
    public static String logTag = "KM";
    
    protected Map<String,Integer> features;
    
    protected Map<Integer, List<String>> ptof;

    protected String field;

    protected int k;

    protected int N;

    protected ClusterElement[] els;
    
    protected List<ClusterElement> te;
    
    protected double[][] clusters;

    protected String[] names;

    protected int nFeat;

    public KMeans(List<String> keys, 
            DataStoreHead store,
            String field,
            int k, 
            int nFeat) throws AuraException, RemoteException {
        this.k = k;
        this.field = field;
        this.nFeat = nFeat;
        
        //
        // Get the vectors associated with the items, make cluster elements out of them,
        // and add them to our feature clusterer.
        te = new ArrayList<ClusterElement>();
        for(String key : keys) {
            te.add(new ClusterElement(store.getItem(key), store.getDocumentVector(key, field)));
        }
        
        initFeatures(store);
    }
    
    private void initFeatures(DataStoreHead store) {
        
        features = new HashMap<String,Integer>();
        ptof = new HashMap<Integer,List<String>>();
        
        WeightedFeatureClusterer fc = new WeightedFeatureClusterer();
        for(ClusterElement el : te) {
            fc.add(((DocumentVectorImpl) el.dv).getFeatures());
        }
        FeatureClusterSet fcs = fc.cluster(null);
        WeightedFeatureSelector fs = new WeightedFeatureSelector();
        fs.setStopWords(store.getStopWords());
        
        //
        // Select the top n feature clusters.
        fcs = fs.select(fcs, null, 0, nFeat, null);
        
        //
        // Make the map from features to points in our feature space.
        N = 0;
        for(FeatureCluster clust : fcs) {
            List<String> fnames = new ArrayList();
            for(Feature f : clust) {
                features.put(f.getName(), N);
                fnames.add(f.getName());
            }
            ptof.put(N, fnames);
            N++;
        }
        
        //
        // Now make the cluster elements.
        for(Iterator<ClusterElement> i = te.iterator(); i.hasNext(); ) {
            
            ClusterElement el = i.next();
            WeightedFeature[] v = ((DocumentVectorImpl) el.dv).getFeatures();
            if(v.length == 0) {
                i.remove();
                continue;
            }
            double[] point = new double[N];
            for(int j = 0; j < v.length; j++) {
                Integer x = features.get(v[j].getName());
                if(x != null) {
                    point[x] += v[j].getWeight();
                }
            }
            
            //
            // Normalize to unit length.
            double sum = 0;
            for(int j = 0; j < point.length; j++) {
                sum += point[j] * point[j];
            }
            sum = Math.sqrt(sum);
            for(int j = 0 ; j < point.length; j++) {
                point[j] /= sum;
            }
            
            el.setPoint(point);
        }
        els = te.toArray(new ClusterElement[0]);
    }
    
    public List<Cluster> getClusters() {
        Cluster[] cl = new Cluster[k];
        for(int i = 0; i < cl.length; i++) {
            cl[i] = new Cluster(features, clusters[i]);
        }
        
        //
        // Put the elements in their assigned clusters.      
        for(ClusterElement el : te) {
            if(el.member >= 0) {
                cl[el.member].add(el);
            }
        }
        
        List<Cluster> ret = new ArrayList<Cluster>();
        for(int i = 0; i < cl.length; i++) {
            cl[i].finish();
            ret.add(cl[i]);
        }
        
        Collections.sort(ret);
        Collections.reverse(ret);
        return ret;
    }
    
    /**
     * Cluster the results.
     */
    public void cluster() {
        StopWatch sw = new StopWatch();
        sw.start();
        clusters = new double[k][];
        //
        // Generate k random points in N space to start.
        for(int i = 0; i < clusters.length; i++) {
            clusters[i] = ClusterUtil.normalizedRandomPoint(N);
        }
        
        //
        // We'll foolishly keep going forever-ish.
        int nIter = 0;
        while(true) {
            
            int nChanges = assignToClusters();
            
            //
            // We'll quit if we didn't reassign any of the docs and we've not
            // been running for too long.
            if(nChanges == 0 && nIter < 1000) {
                break;
            }
            
            recomputeClusters();
            nIter++;
        }
        sw.stop();
    }
    
    private int assignToClusters() {
        int nChanges = 0;
        for(int i = 0; i < els.length; i++) {
            double min = Double.MAX_VALUE;
            int mp = 0;
            for(int j = 0; j < clusters.length; j++) {
                double dist = ClusterUtil.euclideanDistance(els[i].point, clusters[j]);
                if(dist < min) {
                    min = dist;
                    mp = j;
                }
            }
            if(els[i].member != mp) {
                els[i].member = mp;
                nChanges++;
            }
        }
        return nChanges;
    }
    
    /**
     * Recomputes the cluster centroids.
     */
    private void recomputeClusters() {
        
        //
        // Compute the cluster centroids.
        clusters = new double[k][N];
        int[] s = new int[k];
        for(int i = 0; i < els.length; i++) {
            ClusterUtil.add(clusters[els[i].member],
                    els[i].point);
            s[els[i].member]++;
        }
        for(int i = 0; i < clusters.length; i++) {
            ClusterUtil.div(clusters[i], s[i]);
        }
        
        //
        // Find the point closest to the centroid and use that.
        int[] closest = new int[k];
        double[] cd = new double[k];
        for(int i = 0; i < cd.length; i++) {
            cd[i] = Double.MAX_VALUE;
        }
        for(int i = 0; i < els.length; i++) {
            int c = els[i].member;
            double dist = ClusterUtil.euclideanDistance(els[i].point, clusters[c]);
            if(dist < cd[c]) {
                cd[c] = dist;
                closest[c] = i;
            }
        }
        for(int i = 0; i < clusters.length; i++) {
            clusters[i] = els[closest[i]].point;
        }
    }       
 }

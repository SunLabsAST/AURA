/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.web.sxsw;

import com.sun.labs.aura.music.web.Utilities;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author plamere
 */
public class Tree {
    private SimilarityEngine sim = null;
    private List<Scored<String>> ids;
    private Node[] allNodes;
    private boolean multi;

    public Tree(SimilarityEngine sim, List<Scored<String>> ids, boolean multi) {
        this.sim = sim;
        this.ids = ids;
        this.allNodes = new Node[ids.size()];
        this.multi = multi;

        buildTree();
    }

    public void buildTree() {
        for (int i = 0; i < ids.size(); i++) {
            allNodes[i] = new Node(ids.get(i).getItem(), ids.get(i).getScore());
        }
        System.out.println("Attaching  " + allNodes.length + " nodes");
        for (int i = 0; i < allNodes.length; i++) {
            System.out.println("Attaching node " + i + " : " + allNodes[i].getName());
            if (multi) {
                attachMultiNode(i);
            } else {
                attachSingleNode(i);
            }
        }
    }

    private void attachSingleNode(int which) {
        double minDistance = Double.MAX_VALUE;
        int bestParentIndex = -1;
        for (int i = which - 1; i >= 0; i--) {
            double distance = getDistance(which, i);
            if (distance < minDistance) {
                minDistance = distance;
                bestParentIndex = i;
            }
        }
        if (bestParentIndex >= 0) {
            Node thisNode = allNodes[which];
            thisNode.addParent(allNodes[bestParentIndex]);
        }
    }

    private void attachMultiNode(int which) {
        double minDistance = Double.MAX_VALUE;
        double minAttachmentThreshold = 0.748;
        double watermark = .05;
        for (int i = 0; i < which; i++) {
            double distance = getDistance(which, i);
            if (distance < minDistance) {
                minDistance = distance;
            }
        }
        if (minDistance < minAttachmentThreshold) {
            double threshold = minDistance + (minDistance * watermark);
            Node curNode = allNodes[which];
            List<Scored<Node>> bestNodes = new ArrayList<Scored<Node>>();
            for (int i = 0; i < which; i++) {
                Node thisNode = allNodes[i];
                double distance = getDistance(which, i);
                if (distance < threshold) {
                    bestNodes.add(new Scored<Node>(thisNode, distance));
                }
            }
            Collections.sort(bestNodes, new ScoredComparator<Node>());
            for (Scored<Node> snode : bestNodes) {
                boolean goodNode = true;
                Node candidate = snode.getItem();

                // see if we have any of the current parents as ancestors to 
                // the candidate

                // make sure that we don't already have the candidate
                // as an ancestor
                if (!curNode.hasAncestor(candidate)) {
                    for (Node pnode : curNode.getParents()) {
                        if (candidate.hasAncestor(pnode)) {
                            goodNode = false;
                            break;
                        }
                    }
                } else {
                    goodNode = false;
                }

                if (goodNode) {
                    curNode.addParent(snode.getItem());
                }
            }
        }
    }

    void dumpGraphviz(String path, boolean showSizes) throws IOException {
        PrintWriter out = new PrintWriter(path);
        out.printf("digraph MusicTags {\n   graph [rankdir=\"LR\"]\n");
        double maxSize = getMax();

        for (Node node : allNodes) {
            if (showSizes) {
                out.printf("    \"%s\" [label=\"%s\" %s];\n", node.getGraphNodeName(), node.getName(), getScale(node.getSize() / maxSize));
            } else {
                out.printf("    \"%s\" [label=\"%s\"];\n", node.getGraphNodeName(), node.getName());
            }
        }

        for (Node node : allNodes) {
            for (Node parent : node.getParents()) {
                out.printf("    \"%s\" -> \"%s\" [weight=%.3f];\n",
                        parent.getGraphNodeName(), node.getGraphNodeName(), 2 - node.getDistance(parent));
            }
        }
        out.printf("}\n");
        out.close();
    }

    private double getMax() {
        double max = 0;
        for (Node node : allNodes) {
            if (node.getSize() > max) {
                max = node.getSize();
            }
        }
        return max;
    }
    private final static double MIN_WIDTH = .8;
    private final static double MAX_WIDTH = 5;

    String getScale(double normSize) {
        double width = normSize * MAX_WIDTH + MIN_WIDTH;
        double height = width / 3;
        int fontSize = (int) (height * 72) / 3;

        if (fontSize < 4) {
            fontSize = 4;
        }
        return "fontsize=" + fontSize + " fixedsize=true width=" + width + " height=" + height;
    }

    private double getDistance(int i, int j) {
        return sim.getDistance(allNodes[i].getKey(), allNodes[j].getKey());
    }

    private double getDistance(String key1, String key2) {
        return sim.getDistance(key1, key2);
    }

    class Node {

        private String key;
        private double size;
        private List<Node> parents = new ArrayList<Node>();

        Node(String key, double size) {
            this.key = key;
            this.size = size;
        }

        void addParent(Node node) {
            parents.add(node);
        }

        List<Node> getParents() {
            return parents;
        }

        double getDistance(Node node) {
            return sim.getDistance(key, node.getKey());
        }

        String getKey() {
            return key;
        }

        String getGraphNodeName() {
            return Utilities.normalize(key);
        }

        String getName() {
            return key;
        }

        double getSize() {
            return size;
        }

        boolean hasAncestor(Node node) {
            for (Node pnode : getParents()) {
                if (pnode == node) {
                    return true;
                } else if (pnode.hasAncestor(node)) {
                    return true;
                }
            }
            return false;
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.grid.util;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.Grid;
import com.sun.caroline.util.GridFinder;

/**
 *
 */
public class DU {

    public static void main(String args[]) throws Exception {
        GridFinder gf = new GridFinder(null);
        Grid grid = gf.findGrid(0);
        long total = 0;
        for(FileSystem fs : grid.findAllFileSystems()) {
            total += fs.getMetrics().getSpaceUsed();
        }

        System.out.println(String.format("Space: %.3fMB", total / 1024.0 / 1024.0));
    }
}

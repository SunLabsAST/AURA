/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Directory {
    private File dirFile;
    private Map<String, String> map = new HashMap<String,String>();
    private String SEP = "\t<sep>\t";
    private boolean loaded = false;
    
    
    Directory(File dirFile) throws IOException {
        File dir = dirFile.getParentFile();
        if (dir != null && !dir.exists()) {
            if (!dir.mkdirs()) {
                throw new IOException("Can't create dir " + dir);
            }
        }
        this.dirFile = dirFile;
        load();
    }
    
    
    void add(String name, String value) {
        if (value != null) {
            map.put(name, value);
        }
        
    }
    
    void setFilename(File newName) {
        dirFile = newName;
    }
    
    File getFilename() {
        return dirFile;
    }
    
    String lookup(String name) {
        String id = map.get(name);
        return id;
    }
    
    void flush() throws IOException {
        PrintWriter out = new PrintWriter(dirFile);
        List<String> names = new ArrayList<String>(map.keySet());
        Collections.sort(names);
        for (String name : names) {
            out.printf("%s%s%s\n", name, SEP, map.get(name));
        }
        out.close();
    }
    
    void dump() throws IOException {
        List<String> names = new ArrayList<String>(map.keySet());
        Collections.sort(names);
        System.out.println("==========" + dirFile.getName() + "============");
        for (String name : names) {
            System.out.printf("%14.14s: %s\n", name, map.get(name));
        }
    }
    
    void load() throws IOException {
        if (dirFile.exists()) {
            BufferedReader in = new BufferedReader(new FileReader(dirFile));
            String line = null;
            
            while ((line = in.readLine()) != null) {
                String[] fields = line.split(SEP, 2);
                if (fields.length == 2) {
                    map.put(fields[0], fields[1]);
                }
            }
            in.close();
            loaded = true;
        }
    }
    
    public boolean isLoaded() {
        return loaded;
    }
}


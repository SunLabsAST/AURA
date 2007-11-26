/*
 * Directory.java
 *
 * Created on February 13, 2007, 8:12 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.search.music.web;

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
import javax.swing.text.Utilities;

public class Directory {
    private File dirFile;
    private Map<String, String> map = new HashMap<String,String>();
    private String SEP = "\t<sep>\t";
    private boolean loaded = false;
    
    
    Directory(File dirFile) throws IOException {
        File dir = dirFile.getParentFile();
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
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


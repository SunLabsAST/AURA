/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.search.music.web;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.BaseException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class Cache<T> {

    private int maxMemoryObjects;
    private int maxAgeInDays;
    private File cacheDir;
    private XStream xstream;
    private Map<String, T> objectCache = new ObjectMap<String, T>();

    public Cache(int maxMemoryObjects, int maxAgeInDays, File cacheDir) {
        this.maxMemoryObjects = maxMemoryObjects;
        this.maxAgeInDays = maxAgeInDays;
        this.cacheDir = cacheDir;
        this.xstream = new XStream();
    }

    public synchronized T get(String name) {
        T object = null;
        object = objectCache.get(name);
        if (object == null && cacheDir != null) {
            object = readObjectFromFile(name);
        }
        return object;
    }

    public synchronized void put(String name, T object) {
        if (object != null) {
            objectCache.put(name, object);
            if (cacheDir != null) {
                writeObjectToFile(name, object);
            }
        }
    }

    private T readObjectFromFile(String id) {
        T object = null;
        File file = getXmlFile(id);
        if (file.exists() && !expired(file)) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
                object =  (T) xstream.fromXML(reader);
                reader.close();
            } catch (IOException ioe) {
                System.err.println("trouble reading " + file);
            } catch (BaseException e) {
                System.err.println("trouble reading xml" + file);
            }
        }
        return object;
    }
   
    private File getXmlFile(String id) {
        // there will likely be tens of thousands of these
        // xml files, we don't want to overwhelm a diretory, so
        // lets spread them out over 256 directories.'
        id = id.replaceAll("/", "_");
        id = id + ".obj.xml";
        String dir = id.substring(0, 2).toLowerCase();
        File fullPath = new File(cacheDir, dir);
        if (!fullPath.exists()) {
            fullPath.mkdirs();
        }
        return new File(fullPath, id);
    }
        /**
     * Checks to see if a file is older than
     * the expired time
     */
    private boolean expired(File file) {
        if (maxAgeInDays == 0) {
            return false;
        } else {
            long staleTime = System.currentTimeMillis() -
                    maxAgeInDays * 24 * 60 * 60 * 1000L;
            return (file.lastModified() < staleTime);
        }
    }

    private void writeObjectToFile(String name, T object) {
        File file = getXmlFile(name);
        // System.out.println("Saving to " + file);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
            xstream.toXML(object, writer);
            writer.close();
        } catch (IOException ioe) {
            System.err.println("can't save details to " + file);
        }
    }

    class ObjectMap<V, O> extends LinkedHashMap<V, O> {

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxMemoryObjects;
        }
    }
}

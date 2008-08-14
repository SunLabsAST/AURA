/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public class OutFileGenerator {
    public String dataDir;
    String filenameBase;
    String filenameExt;
    public String separator = ".";
    
    public OutFileGenerator(String filenameBase, String filenameExt, String dataDir) {
        if(!(new File(dataDir)).exists()) {
            throw new IllegalArgumentException("Data directory does not exist: " + dataDir);
        }
        
        this.dataDir = dataDir;
         if(!dataDir.endsWith(File.separator)) {
            this.dataDir += File.separator;
        }
        this.filenameBase = filenameBase;
        this.filenameExt = filenameExt;
    }

    public Writer getOutput(List<RecordSet> recordSets) throws IOException {
        StringBuffer fileId = new StringBuffer();
        for(RecordSet records : recordSets) {
            fileId.append(records.getId());
            fileId.append(separator);
        }
        fileId.setLength(fileId.length() - separator.length());
        String outFilename =
                dataDir + filenameBase + fileId.toString() + filenameExt;
        try {
            return new FileWriter(outFilename);
        } catch(FileNotFoundException fnfe) {
            throw new IllegalArgumentException("Could not create: " + outFilename);
        }
    }
    
    public String getFileId(String filename) {
        if(!filename.startsWith(filenameBase) && !filename.endsWith(filenameExt)) {
            return null;
        } else {
            try {
                return filename.substring(filenameBase.length(), filename.length() - filenameExt.length());
            } catch(StringIndexOutOfBoundsException e) {
                return null;
            }
        }
    }
}

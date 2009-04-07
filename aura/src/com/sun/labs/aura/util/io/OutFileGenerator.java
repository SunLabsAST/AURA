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

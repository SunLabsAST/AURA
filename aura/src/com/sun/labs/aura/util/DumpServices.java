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

package com.sun.labs.aura.util;

import com.sun.labs.util.props.ComponentRegistry;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.io.IOException;

/**
 *
 * @author stgreen
 */
public class DumpServices {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        URL cu = DumpServices.class.getResource(args[0]);
        if(cu == null) {
            cu = (new File(args[0])).toURI().toURL();
        }
        listServices(cu);
    }
    
    public static void listServices(URL configURL) throws InterruptedException, PropertyException, IOException {
        ConfigurationManager cm = new ConfigurationManager(configURL);
        ComponentRegistry cr = cm.getComponentRegistry();
        
        if(cr == null) {
            System.out.println("No component registry");
            return;
        }
        Thread.sleep(10000);
        Map<String,List<String>> dump = cr.dumpJiniServices();
        for(Map.Entry<String,List<String>> e : dump.entrySet()) {
            System.out.println("Registrar: " + e.getKey());
            for(String s : e.getValue()) {
                System.out.println("  Service: " + s);
            }
        }
    }
}

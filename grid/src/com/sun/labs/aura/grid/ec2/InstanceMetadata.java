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

package com.sun.labs.aura.grid.ec2;

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class to manage instance metadata on the EC2 grid.  This will probably
 * only do something useful on the EC2 grid.
 */
public class InstanceMetadata {

    private String mdURI = "http://169.254.169.254/latest/user-data";
    private Map<String,String> md;
    private Logger logger = Logger.getLogger(InstanceMetadata.class.getName());

    public InstanceMetadata() {
        try {
            URI u = new URI(mdURI);
            InputStream is = u.toURL().openStream();
            Properties props = new Properties();
            props.load(is);
            is.close();
            for(Map.Entry<Object,Object> e : props.entrySet()) {
                md.put((String) e.getKey(), (String) e.getValue());
            }
        } catch (java.net.URISyntaxException urie) {
            logger.severe("Bad metadata URI: " + mdURI);
        } catch (java.net.MalformedURLException male) {
            logger.severe("Malformed metadata URI: " + mdURI);
        } catch (java.io.IOException ioe) {
            logger.log(Level.SEVERE, "Error reading instance metadata", ioe);
        }
    }

    public String get(String key) {
        return md.get(key);
    }

    public Set<String> keySet() {
        return new HashSet<String>(md.keySet());
    }
}

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

import com.sun.labs.aura.util.AuraException;
import java.io.IOException;
import java.util.Properties;

/**
 *
 * @author mailletf
 */
public abstract class WebServiceAccessor {

    protected String API_KEY;

    private Properties properties;

    public WebServiceAccessor(String serviceName, String propertiesKey) throws AuraException {

        try {
            properties = new Properties();
            properties.load(this.getClass().getClassLoader().getResourceAsStream("com/sun/labs/aura/music/resource/api.properties"));
            API_KEY = properties.getProperty(propertiesKey);
        } catch (IOException ex) {
            throw new AuraException("No " + serviceName + " API key available. " +
                    "Please set properties file (com/sun/labs/aura/music/resource/api.properties) to use.");
        } catch (NullPointerException ex2) {
            throw new AuraException("No " + serviceName + " API key available. " +
                    "Please set properties file (com/sun/labs/aura/music/resource/api.properties) to use.");
        }
    }

    protected String getProperty(String key) {
        return properties.getProperty(key);
    }
    
}

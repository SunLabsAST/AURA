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

package com.sun.labs.aura.grid.aura;

import com.sun.caroline.platform.ProcessRegistration;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.util.logging.Level;

/**
 *
 */
public class StartDataStore extends Aura {
    
    @ConfigInteger(defaultValue=2)
    public static final String PROP_INSTANCE_NUMBER = "instanceNumber";
    private int instanceNumber;

    @Override
    public String serviceName() {
        return "StartDataStore";
    }

    public void start() {
        try {
            ProcessRegistration dsHeadReg =
                    gu.createProcess(
                    getDataStoreHeadName(instanceNumber),
                    getDataStoreHeadConfig(instanceNumber));
            gu.startRegistration(dsHeadReg);
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "Error starting data store", ex);
        }

    }

    public void stop() {
    }

    @Override
    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        instanceNumber = ps.getInt(PROP_INSTANCE_NUMBER);
    }

}

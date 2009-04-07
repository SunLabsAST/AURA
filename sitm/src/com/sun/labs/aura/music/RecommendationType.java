/*
 * Copyright 2008-2009 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.labs.aura.music;

import com.sun.labs.aura.datastore.Item.ItemType;
import com.sun.labs.aura.util.AuraException;
import java.rmi.RemoteException;

/**
 *
 * @author plamere
 */
public interface RecommendationType {
    /**
     * Gets the name of the recommendation type
     * @return the name of the recommendation type
     */
    String getName();

    /**
     * Gets a detailed description of the recommendation type
     * @return the description of the recommendation type
     */
    String getDescription();

    /**
     * Gets recommendations
     * @param listenerID the id of the listener of interest
     * @param count the number of recommendations to generate
     * @param the recommendation profile
     * @return  the ordered list of recommendations
     */
    RecommendationSummary getRecommendations(String listenerID, int count, RecommendationProfile rp) 
                throws AuraException, RemoteException;


    /**
     *  Returns the type of item that is recommended
     * @return the type of item that is recommendec
     */
    ItemType getType();
}

/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author plamere
 */
public class WiEntrySummary implements IsSerializable {
    private String title;
    private String link;

    public WiEntrySummary(String title, String link) {
        this.title = title;
        this.link = link;
    }

    public WiEntrySummary() {
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }
}

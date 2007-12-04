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
    private String date;
    //private String sourceTitle;
    //private String sourceLink;

    public WiEntrySummary(String title, String link, String date) {
        this.title = title;
        this.link = link;
        this.date = date;
    }



    public WiEntrySummary() {
    }

    public String getLink() {
        return link;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }
}

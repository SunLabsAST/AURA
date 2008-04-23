/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.impl.crawler;

import java.io.Serializable;

/**
 *
 * @author plamere
 */
public class Anchor implements Serializable {
    private String destURL;
    private String anchorText;

    public Anchor(String destURL, String anchorText) {
        this.destURL = destURL;
        this.anchorText = anchorText;
    }

    public String getAnchorText() {
        return anchorText;
    }

    public String getDestURL() {
        return destURL;
    }


    @Override
    public String toString() {
        return  destURL + "  (" + anchorText + ")";
    }

}

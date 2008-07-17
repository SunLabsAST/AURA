/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.Image;

/**
 *
 * @author mailletf
 */
public class SwapableImage extends Image {

    private String img1;
    private String img2;

    public SwapableImage(String img1, String img2) {
        super(img1);
        this.img1=img1;
        this.img2=img2;
    }

    public void setImg1() {
        setUrl(img1);
    }

    public void setImg2() {
        setUrl(img2);
    }

}

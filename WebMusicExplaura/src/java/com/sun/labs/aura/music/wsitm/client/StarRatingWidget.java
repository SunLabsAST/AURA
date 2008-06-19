/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;


import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public class StarRatingWidget extends Widget {

    private int nbrStars = 5;
    private int nbrSelectedStars = 0;

    private static final String STAR_LID = "star-lid.png";
    private static final String STAR_NOTLID = "star-notlid.png";
    private static final String STAR_WHITE = "star-white.png";

    private Image[] images;

    public StarRatingWidget(int initialSelection) {
        this.nbrSelectedStars = initialSelection;

        images = new Image[nbrStars];
        for (int i=0; i<nbrStars; i++) {
            if (i<=nbrSelectedStars-1) {
                images[i] = new Image(STAR_NOTLID);
            } else {
                images[i] = new Image(STAR_WHITE);
            }
            images[i].addClickListener(new IndexClickListener(i));
            images[i].addMouseListener(new IndexMouseListener(i));
        }

    }

    private void triggerAction(int index) {
        Window.alert("you clicked on star "+index);
    }

    private class IndexMouseListener implements MouseListener {

        private int index;

        public IndexMouseListener(int index) {
            this.index=index;
        }

        public void onMouseDown(Widget arg0, int arg1, int arg2) {
        }

        public void onMouseEnter(Widget arg0) {
            for (int i=0; i<=index; i++) {
                images[i].setUrl(STAR_LID);
            }
        }

        public void onMouseLeave(Widget arg0) {
            for (int i=0; i<=index; i++) {
                if (i<=nbrSelectedStars-1) {
                    images[i] = new Image(STAR_NOTLID);
                } else {
                    images[i] = new Image(STAR_WHITE);
                }
            }
        }

        public void onMouseMove(Widget arg0, int arg1, int arg2) {
        }

        public void onMouseUp(Widget arg0, int arg1, int arg2) {
        }

    }

    private class IndexClickListener implements ClickListener {

        private int index;

        public IndexClickListener(int index) {
            super();
            this.index = index;
        }

        public void onClick(Widget arg0) {
            triggerAction(index);
        }
    }

}

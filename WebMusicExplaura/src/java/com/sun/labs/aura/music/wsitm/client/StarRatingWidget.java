/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;


import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public class StarRatingWidget extends Widget {

    private int nbrStars = 5;
    private int nbrSelectedStars = 0;


    public StarRatingWidget(int initialSelection) {

    }

    private void triggerAction(int index) {
        Window.alert("you clicked on star "+index);
    }



    protected class IndexClickListener implements ClickListener {

        protected int index;

        public IndexClickListener(int index) {
            super();
            this.index = index;
        }

        public void onClick(Widget arg0) {
            triggerAction(index);
        }
    }

}

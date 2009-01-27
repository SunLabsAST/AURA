/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.event;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;

/**
 *
 * @author mailletf
 */
    public abstract class HoverListener<T> implements EventListener {

        protected T data;

        public HoverListener(T data) {
            this.data = data;
        }

        /**
         * Fired when mouse starts hovering over the widget
         */
        public abstract void onMouseHover();
        /**
         * Fired as soon as the mouse stops hovering over the widget
         */
        public abstract void onMouseOut();
        /**
         * Fired when the mouse has stopped hovering for a small delay
         */
        public abstract void onOutTimer();

        @Override
        public void onBrowserEvent(Event event) {};
    }

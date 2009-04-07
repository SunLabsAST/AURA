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

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

package com.sun.labs.aura.music.wsitm.client.ui.bundles;

import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.ImageBundle;

/**
 *
 * @author mailletf
 */
public interface ArtistRelatedBundle extends ImageBundle {

    /**
     * Generic play icons
     */

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-icon40.png")
    public AbstractImagePrototype playIcon40();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-icon30.png")
    public AbstractImagePrototype playIcon30();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-icon20.png")
    public AbstractImagePrototype playIcon20();


    /**
     * LastFm play icons
     */

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-lastfm-40.png")
    public AbstractImagePrototype playLastfm40();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-lastfm-30.png")
    public AbstractImagePrototype playLastfm30();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-lastfm-20.png")
    public AbstractImagePrototype playLastfm20();

    /**
     * Spotify play icons
     */

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-spotify-40.png")
    public AbstractImagePrototype playSpotify40();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-spotify-30.png")
    public AbstractImagePrototype playSpotify30();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/play-spotify-20.png")
    public AbstractImagePrototype playSpotify20();


    /**
     * Tag icon
     */
    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/tag25.png")
    public AbstractImagePrototype Tag25();


   /**
     * Tag icon
     */
    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/Prev_Button.png")
    public AbstractImagePrototype scrollWidgetPrev();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/Next_Button.png")
    public AbstractImagePrototype scrollWidgetNext();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/Top_Button.png")
    public AbstractImagePrototype topArrow();


}

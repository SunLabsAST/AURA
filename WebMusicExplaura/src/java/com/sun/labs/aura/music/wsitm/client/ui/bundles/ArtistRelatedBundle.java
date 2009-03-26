/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

}

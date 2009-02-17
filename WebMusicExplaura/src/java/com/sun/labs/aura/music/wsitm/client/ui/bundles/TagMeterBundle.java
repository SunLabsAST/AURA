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
public interface TagMeterBundle extends ImageBundle {

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-off.jpg")
    public AbstractImagePrototype meterOff();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-on.jpg")
    public AbstractImagePrototype meterOn();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-hover.jpg")
    public AbstractImagePrototype meterHover();


    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-stick-off.jpg")
    public AbstractImagePrototype meterStickOff();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-stick-on.jpg")
    public AbstractImagePrototype meterStickOn();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/meter-stick-hover.jpg")
    public AbstractImagePrototype meterStickHover();
    
}

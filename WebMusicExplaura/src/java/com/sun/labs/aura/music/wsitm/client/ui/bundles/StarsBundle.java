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
public interface StarsBundle extends ImageBundle {

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/star-lid.png")
    public AbstractImagePrototype starLid();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/star-notlid.png")
    public AbstractImagePrototype starNotLid();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/star-white.png")
    public AbstractImagePrototype starWhite();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/star-lid-s.png")
    public AbstractImagePrototype starLidS();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/star-notlid-s.png")
    public AbstractImagePrototype starNotLidS();

    @Resource("com/sun/labs/aura/music/wsitm/client/ui/bundles/img/star-white-s.png")
    public AbstractImagePrototype starWhiteS();

}

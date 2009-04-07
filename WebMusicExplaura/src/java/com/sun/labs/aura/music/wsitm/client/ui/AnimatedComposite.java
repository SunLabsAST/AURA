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

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.adamtacy.client.ui.NEffectPanel;
import org.adamtacy.client.ui.effects.NEffect;
import org.adamtacy.client.ui.effects.NEffectHandler;
import org.adamtacy.client.ui.effects.impl.Fade;
import org.adamtacy.client.ui.effects.impl.SlideBase;
import org.adamtacy.client.ui.effects.impl.SlideDown;
import org.adamtacy.client.ui.effects.impl.SlideLeft;
import org.adamtacy.client.ui.effects.impl.SlideRight;
import org.adamtacy.client.ui.effects.impl.SlideUp;

/**
 *
 * @author mailletf
 */
public abstract class AnimatedComposite extends Composite {

    public enum SlideDirection {
        UP, DOWN, LEFT, RIGHT
    }

    private NEffectPanel mainEffectPanel;

    public AnimatedComposite() {
        super();
        mainEffectPanel = new NEffectPanel();
    }

    @Override
    public void onBrowserEvent(Event event) {
        // Delegate events to the widget.
        mainEffectPanel.getWidget().onBrowserEvent(event);
    }

    @Override
    protected void initWidget(Widget widget) {
        mainEffectPanel.add(widget);
        super.initWidget(mainEffectPanel);
    }
/*
    //
    //  Slide in
    //
    public void slideIn(SlideDirection d) {
        slideIn(d, FxConfig.NONE);
    }

    public void slideIn(SlideDirection d, Command cmd) {
        slideIn(d, getFxConfigFromCmd(cmd));
    }
*/
    //
    // Slide out
    //
    public void slideOut(SlideDirection d) {
        slideOut(d, null);
    }

    public void slideOut(SlideDirection d, Command cmd) {

        cmd.execute();
        
/*
        SlideBase sB;
        if (d == SlideDirection.DOWN) {
            sB = new SlideDown();
        } else if (d == SlideDirection.LEFT) {
            sB = new SlideLeft();
        } else if (d == SlideDirection.RIGHT) {
            sB = new SlideRight();
        } else {
            sB = new SlideUp();
        }

        if (cmd != null) {
            sB.addEffectHandler(new CommandEmbededHandler(cmd));
        }

        sB.play();
 * */
    }


    //
    // Fade in
    //
    public void fade(Command cmd, int startOpacity, int endOpacity) {
        Fade f = new Fade();
        f.getProperties().setStartOpacity(startOpacity);
        f.getProperties().setEndOpacity(endOpacity);

        if (cmd != null) {
            f.addEffectHandler(new CommandEmbededHandler(cmd));
        }

        mainEffectPanel.addEffect(f);
        mainEffectPanel.playEffects();
    }

    public void fadeIn() {
        fadeIn(null);
    }

    public void fadeIn(Command cmd) {
        fade(cmd, 0, 100);
    }

    //
    // Fade out
    //
    public void fadeOut() {
        fadeOut(null);
    }

    public void fadeOut(Command cmd) {
        fade(cmd, 100, 0);
    }
    

    public class CommandEmbededHandler implements NEffectHandler {

        private Command cmd;

        public CommandEmbededHandler(Command cmd) {
            this.cmd = cmd;
        }

        public void interruptedEvent(NEffect theEffect) {}

        public void postEvent(NEffect theEffect) {
            cmd.execute();
        }

        public void preEvent(NEffect theEffect) {}
    }
    
}

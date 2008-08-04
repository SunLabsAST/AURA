/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.event.FxEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;

/**
 *
 * @author mailletf
 */
public abstract class AnimatedComposite extends Composite {

    //
    //  Slide in
    //
    public void slideIn(Direction d) {
        slideIn(d, FxConfig.NONE);
    }

    public void slideIn(Direction d, Command cmd) {
        slideIn(d, getFxConfigFromCmd(cmd));
    }

    private void slideIn(Direction d, FxConfig fx) {
        new El(this.getElement()).slideIn(d, fx);
    }

    //
    // Slide out
    //
    public void slideOut(Direction d) {
        slideOut(d, FxConfig.NONE);
    }

    public void slideOut(Direction d, Command cmd) {
        slideOut(d, getFxConfigFromCmd(cmd));
    }

    private void slideOut(Direction d, FxConfig fx) {
        new El(this.getElement()).slideOut(d, fx);
    }

    //
    // Fade in
    //
    public void fadeIn() {
        fadeIn(FxConfig.NONE);
    }

    public void fadeIn(Command cmd) {
        fadeIn(getFxConfigFromCmd(cmd));
    }

    private void fadeIn(FxConfig fx) {
        new El(this.getElement()).fadeIn(fx);
    }

    //
    // Fade out
    //
    public void fadeOut() {
        fadeOut(FxConfig.NONE);
    }

    public void fadeOut(Command cmd) {
        fadeOut(getFxConfigFromCmd(cmd));
    }

    private void fadeOut(FxConfig fx) {
        new El(this.getElement()).fadeOut(fx);
    }




    private FxConfig getFxConfigFromCmd(Command cmd) {
        FxConfig fx = new FxConfig();
        fx.setEffectCompleteListener(new CommandEmbededListener(cmd));
        return fx;
    }

    public class CommandEmbededListener implements Listener<FxEvent> {

        private Command cmd;

        public CommandEmbededListener(Command cmd) {
            this.cmd = cmd;
        }

        public void handleEvent(FxEvent arg0) {
            cmd.execute();
        }
    }

}

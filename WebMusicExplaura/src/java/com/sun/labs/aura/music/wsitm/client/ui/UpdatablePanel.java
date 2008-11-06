/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public class UpdatablePanel extends Composite {

    private Grid main;
    
    private FlowPanel refreshingPanel;

    public UpdatablePanel(Widget title, Widget widget, ClientDataManager cdm) {

        Grid g = new Grid (1,2);
        g.setStyleName("h2");
        g.setWidth("300px");
        g.setWidget(0, 0, title);

        refreshingPanel = new FlowPanel();
        refreshingPanel.add(new Image("ajax-loader-small.gif"));
        refreshingPanel.setVisible(false);

        g.getColumnFormatter().setWidth(1, "17px");
        g.setWidget(0, 1, refreshingPanel);

        main = new Grid(2, 1);
        main.setWidget(0, 0, g);
        main.setWidget(1, 0, new AnimatedUpdatableSection(widget));
        this.initWidget(main);
    }

    public void setWaitIconVisible(boolean visible) {
        refreshingPanel.setVisible(visible);
    }

    /**
     * Put the given widget as the new content.
     * @param w
     */
    public void setNewContent(Widget bottom) {
        /*((AnimatedUpdatableSection)main.getWidget(1, 0)).slideOut(Direction.UP, new DualDataEmbededCommand<Grid, Widget>(main, bottom) {
            public void execute() {
                data.setWidget(1, 0, sndData);
            }
        });*/
        main.setWidget(1, 0, bottom);
        
    }
    
    private class AnimatedUpdatableSection extends AnimatedComposite {

        public AnimatedUpdatableSection(Widget w) {
            initWidget(w);
            /*
             this.slideIn(Direction.DOWN, new Command() {
                public void execute() {}
            });
             * */
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.extjs.gxt.ui.client.Style.Direction;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Command;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

        HorizontalPanel hP = new HorizontalPanel();
        hP.setStyleName("h2");
        hP.setWidth("300px");
        //hP.add(new SpannedLabel("Recommendations"));
        hP.add(title);

        refreshingPanel = new FlowPanel();
        refreshingPanel.add(new Image("ajax-loader-small.gif"));
        refreshingPanel.setVisible(false);

        hP.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        hP.add(refreshingPanel);

        main = new Grid(2, 1);
        main.setWidget(0, 0, title);
        main.setWidget(1, 0, new AnimatedUpdatableSection(widget));
        this.initWidget(main);
    }

    public void setWaitIconVicible(boolean visible) {
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

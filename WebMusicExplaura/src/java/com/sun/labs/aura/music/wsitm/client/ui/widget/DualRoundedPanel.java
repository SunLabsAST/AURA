/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.agentspecific.impl.CssDefsImpl;
import org.cobogw.gwt.user.client.ui.RoundedPanel;

/**
 *
 * @author mailletf
 */
public class DualRoundedPanel extends Composite {

    private Grid mainPanel;

    public DualRoundedPanel() {
        mainPanel = new Grid(2,1);
        mainPanel.setCellPadding(0);
        mainPanel.setCellSpacing(0);
        initWidget(mainPanel);
    }

    public DualRoundedPanel(Widget title, Widget content) {
        this();
        setHeader(title);
        setContent(content, true);
    }

    public DualRoundedPanel(String title, Widget content) {
        this();
        Label titleLbl = new Label(title);
        titleLbl.setStyleName("h2");
        setHeader(titleLbl);
        setContent(content);
    }

    public void setHeader(Widget w) {
        w.getElement().getStyle().setPropertyPx("marginBottom", 0);
        mainPanel.setWidget(0, 0, w);
    }

    public void setContent(Widget w) {
        setContent(w, true);
    }

    public void setContent(Widget w, boolean addRoundedPanel) {
        if (addRoundedPanel) {
            RoundedPanel rp = CssDefsImpl.impl.createRoundedPanel(w, RoundedPanel.BOTTOM, 4);
            w.addStyleName("roundedPageBack");
            rp.setCornerStyleName("roundedPageBack");
            mainPanel.setWidget(1, 0, rp);
        } else {
            mainPanel.setWidget(1, 0, w);
        }
    }

}

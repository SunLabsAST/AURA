/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class TagInputWidget extends Composite {

    private Map<String, Tag> userTags;
    private FlowPanel tagPanel;
    private TextBox txtBox;

    public TagInputWidget(String itemName) {

        userTags = new HashMap<String, Tag>();

        HorizontalPanel mainPanel = new HorizontalPanel();
        mainPanel.add(new Label("Tag this "+itemName+": "));

        tagPanel = new FlowPanel();
        mainPanel.add(tagPanel);

        txtBox = new TextBox();
        txtBox.addKeyboardListener(new TxtboxKeyboardListener(this));

        mainPanel.add(txtBox);

        initWidget(mainPanel);

    }

    public String getTextBoxTxt() {
        return txtBox.getText();
    }

    public void clearTextBoxTxt() {
        txtBox.setText("");
    }

    private void redrawTags() {
        tagPanel.clear();
        for (Tag t : userTags.values()) {
            tagPanel.add(t);
        }
    }

    public void addTag(String tag) {
        userTags.put(tag, new Tag(tag));
        clearTextBoxTxt();
        redrawTags();
    }

    public void removeTag(String tag) {
        userTags.remove(tag);
        redrawTags();
    }

    public class TxtboxKeyboardListener implements KeyboardListener {

        private TagInputWidget tiw;

        public TxtboxKeyboardListener(TagInputWidget tiw) {
            this.tiw = tiw;
        }

        public void onKeyDown(Widget arg0, char arg1, int arg2) {
        }

        public void onKeyPress(Widget arg0, char arg1, int arg2) {
            if (arg1 == KeyboardListener.KEY_ENTER) {
                for (String newTag : tiw.getTextBoxTxt().split(",")) {
                    newTag = newTag.toLowerCase().trim();
                    tiw.addTag(newTag);
                }
                tiw.clearTextBoxTxt();
            }
        }

        public void onKeyUp(Widget arg0, char arg1, int arg2) {
        }
    }

    public class Tag extends Label {

            private boolean hasClicked = false;

            public Tag(String txt) {
                setElement(DOM.createSpan());
                setStyleName("gwt-Label");
                addStyleName("marginRight");
                setText(txt);

                addMouseListener(new MouseListener() {

                    public void onMouseDown(Widget arg0, int arg1, int arg2) {
                    }

                    public void onMouseEnter(Widget arg0) {
                    }

                    public void onMouseLeave(Widget arg0) {
                    }

                    public void onMouseMove(Widget arg0, int arg1, int arg2) {
                    }

                    public void onMouseUp(Widget arg0, int arg1, int arg2) {
                    }
                });

            }

        }
}


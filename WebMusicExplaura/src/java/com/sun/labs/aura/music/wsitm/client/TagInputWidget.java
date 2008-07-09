/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class TagInputWidget extends Composite {

    private Map<String, DeletableWidget> userTags;
    private FlowPanel tagPanel;
    private TextBox txtBox;

    private final static String DEFAULT_TBOX_TXT = "Add comma separated tags";

    public TagInputWidget(String itemName) {

        userTags = new HashMap<String, DeletableWidget>();

        HorizontalPanel mainPanel = new HorizontalPanel();
        Label titleLbl = new Label("Tag this "+itemName+": ");
        titleLbl.getElement().setAttribute("style", "margin-right:5px");
        mainPanel.add(titleLbl);

        tagPanel = new FlowPanel();
        mainPanel.add(tagPanel);

        txtBox = new TextBox();
        resetTextBoxIfEmpty();
        txtBox.addKeyboardListener(new TxtboxKeyboardListener(this));
        txtBox.addFocusListener(new FocusListener() {

            public void onFocus(Widget arg0) {
                clearTextBoxIfDefault();
            }

            public void onLostFocus(Widget arg0) {
                resetTextBoxIfEmpty();
            }
        });
        txtBox.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {

            }});

        mainPanel.add(txtBox);

        initWidget(mainPanel);

    }

    private void resetTextBoxIfEmpty() {
        if (txtBox.getText().equals("")) {
            txtBox.setText(DEFAULT_TBOX_TXT);
            txtBox.getElement().setAttribute("style", "font-style: italic; color: gray; margin-left: 5px;");
        }
    }

    private void clearTextBoxIfDefault() {
        if (txtBox.getText().equals(DEFAULT_TBOX_TXT)) {
            txtBox.setText("");
            txtBox.getElement().setAttribute("style", "margin-left: 5px;");
        }
    }

    public String getTextBoxTxt() {
        return txtBox.getText();
    }

    public void clearTextBoxTxt() {
        txtBox.setText("");
    }

    private void redrawTags() {
        tagPanel.clear();
        boolean first = true;
        for (DeletableWidget t : userTags.values()) {
            if (first) {
                first = false;
            } else {
                tagPanel.add(new SpannedLabel(", "));
            }
            tagPanel.add(t);
        }
    }

    public void addTag(String tag) {
        userTags.put(tag, new DeletableTag(new SpannedLabel(tag)));
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

    public class DeletableTag extends DeletableWidget<SpannedLabel> {

        private String tag;

        public DeletableTag(SpannedLabel w) {
            super(w);
            this.tag = w.getText();
        }

        public void onDelete() {
            removeTag(tag);
        }
    }

}


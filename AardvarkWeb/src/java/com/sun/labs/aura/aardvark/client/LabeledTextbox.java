/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */

package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A text box with a label
 */
public class LabeledTextbox extends HorizontalPanel {
    private TextBox textBox;

    LabeledTextbox(String label, String defaultText, int inputSize)  {
        add(new Label(label));
        textBox = new TextBox();
        textBox.setText(defaultText);
        textBox.setVisibleLength(inputSize);
        add(textBox);
    }

    public String getText() {
        return textBox.getText();
    }
    
    public void addChangeListener(ChangeListener l) {
        textBox.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        textBox.removeChangeListener(l);
    }
}

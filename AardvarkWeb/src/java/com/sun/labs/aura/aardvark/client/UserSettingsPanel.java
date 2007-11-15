/*
 *  Copyright 2007 Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.client;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormSubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormSubmitEvent;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author plamere
 */
public class UserSettingsPanel extends DockPanel {

    private AppStateListener appState;
    private Widget results = null;

    public UserSettingsPanel(AppStateListener appStateListener, WiUser user) {

        this.appState = appStateListener;
        // Create a FormPanel and point it at a service.
        final FormPanel form = new FormPanel();
        form.setAction("/AardvarkWeb/opml/" + user.getName());

        // Because we're going to add a FileUpload widget, we'll need to set the
    // form to use the POST method, and multipart MIME encoding.
        form.setEncoding(FormPanel.ENCODING_MULTIPART);
        form.setMethod(FormPanel.METHOD_POST);

        // Create a panel to hold all of the form widgets.
        VerticalPanel panel = new VerticalPanel();
        form.setWidget(panel);

        // Create a FileUpload widget.
        FileUpload upload = new FileUpload();
        upload.setName("uploadFormElement");
        panel.add(upload);

        // Add a 'submit' button.
        panel.add(new Button("Submit", new ClickListener() {
                    public void onClick(Widget sender) {
                        form.submit();
                    }
                }));

        // Add an event handler to the form.
        form.addFormHandler(new FormHandler() {

                    public void onSubmit(FormSubmitEvent event) {
                        appState.info("Uploading form");
                    }

                    public void onSubmitComplete(FormSubmitCompleteEvent event) {
                        appState.clearInfo();
                        if (results != null) {
                            remove(results);
                            results = null;
                        }
                        results = new HTML(event.getResults());
                        add(results, SOUTH);
                    }
                });

        Label title = new Label("Settings for " + user.getName());
        title.setStyleName("title");
        setStyleName("userSettingsPanel");

        add(title, NORTH);
        add(form, CENTER);
    }
}

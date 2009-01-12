/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusListener;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import java.util.HashMap;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
public class TagInputWidget extends Composite implements LoginListener {

    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private HashMap<String, SpannedLabel> userTags;
    private FlowPanel tagPanel;
    private TextBox txtBox;
    private Image progressImg;

    private String itemId;

    private final static String DEFAULT_TBOX_TXT = "Add comma separated tags";
    private final static String PROCESSING_TBOX_MSG = "Processing...";

    public TagInputWidget(MusicSearchInterfaceAsync musicServer, ClientDataManager cdm, String itemType, String itemId) {

        this.musicServer = musicServer;
        this.cdm = cdm;
        this.itemId = itemId;

        userTags = new HashMap<String, SpannedLabel>();

        HorizontalPanel mainPanel = new HorizontalPanel();
        Label titleLbl = new Label("Tag this "+itemType+": ");
        titleLbl.getElement().setAttribute("style", "margin-right:5px");
        mainPanel.add(titleLbl);

        tagPanel = new FlowPanel();
        mainPanel.add(tagPanel);

        txtBox = new TextBox();
        resetTextBoxIfEmpty();
        txtBox.addKeyboardListener(new KeyboardListener() {

            public void onKeyPress(Widget arg0, char arg1, int arg2) {
                if (arg1 == KeyboardListener.KEY_ENTER) {
                    onTagSubmit();
                }
            }

            public void onKeyDown(Widget arg0, char arg1, int arg2) {}
            public void onKeyUp(Widget arg0, char arg1, int arg2) {}
        });
        txtBox.addFocusListener(new FocusListener() {

            public void onFocus(Widget arg0) {
                clearTextBoxIfDefault();
            }

            public void onLostFocus(Widget arg0) {
                resetTextBoxIfEmpty();
            }
        });
        mainPanel.add(txtBox);

        progressImg = new Image("ajax-loader-small.gif");
        progressImg.setVisible(false);
        mainPanel.add(progressImg);

        if (cdm.isLoggedIn()) {
            invokeFetchUserTags();
        }

        initWidget(mainPanel);

    }

    private void onTagSubmit() {
        if (cdm.isLoggedIn()) {
            HashSet<String> tags = new HashSet<String>();
            for (String newTag : getTextBoxTxt().split(",")) {
                newTag = newTag.toLowerCase().trim();
                tags.add(newTag);
            }
            clearTextBoxTxt();
            invokeAddTags(tags);
        } else {
            //Window.alert("Message from the happy tag : you must be logged in to access this feature. I should redirect you to another page so you can create an account, but I'd rather keep you here and give you a big happy tag hug!");
            Popup.showLoginPopup();
        }
    }

    private void resetTextBoxIfEmpty() {
        if (txtBox.getText().equals("")) {
            showSystemTextBoxMessage(DEFAULT_TBOX_TXT);
        }
    }
    
    private void showSystemTextBoxMessage(String msg) {
        txtBox.setText(msg);
        txtBox.getElement().setAttribute("style", "font-style: italic; color: gray; margin-left: 5px;");
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
        for (SpannedLabel t : userTags.values()) {
            if (first) {
                first = false;
            } else {
                tagPanel.add(new SpannedLabel(", "));
            }
            tagPanel.add(t);
        }
    }

    public void addTag(String tag) {
        //userTags.put(tag, new DeletableTag(new SpannedLabel(tag)));
        userTags.put(tag, new SpannedLabel(tag));
        clearTextBoxTxt();
        redrawTags();
    }

    public void removeTag(String tag) {
        userTags.remove(tag);
        redrawTags();
    }

    public void removeAllTags() {
        userTags.clear();
        redrawTags();
    }

     private void invokeFetchUserTags() {
        
         AsyncCallback<HashSet<String>> callback = new AsyncCallback<HashSet<String>>() {

             public void onFailure(Throwable arg0) {
                 Window.alert(arg0.toString());
                 resetTextBox();
             }

             public void onSuccess(HashSet<String> tags) {
                 for (String s : tags) {
                     addTag(s);
                 }
                 resetTextBox();
             }

             public void resetTextBox() {
                 txtBox.setEnabled(true);
                 showSystemTextBoxMessage(DEFAULT_TBOX_TXT);
                 progressImg.setVisible(false);
             }
         };

         showSystemTextBoxMessage(PROCESSING_TBOX_MSG);
         txtBox.setEnabled(false);
         progressImg.setVisible(true);

         try {
             musicServer.fetchUserTagsForItem(itemId, callback);
         } catch (Exception ex) {
             Window.alert(ex.getMessage());
         }
    }

     private void invokeAddTags(HashSet<String> tags) {

         DEAsyncCallback<HashSet<String>, HashSet<String>> callback =
                 new DEAsyncCallback<HashSet<String>, HashSet<String>>(tags) {

             public void onFailure(Throwable arg0) {
                 Window.alert(arg0.toString());
                 resetTextBox();
             }

             public void onSuccess(HashSet<String> tags) {
                 for (String s : data) {
                     addTag(s);
                 }

                 cdm.getTaggingListenerManager().triggerOnTag(itemId, tags);

                 txtBox.setEnabled(true);
                 progressImg.setVisible(false);
                 txtBox.getElement().setAttribute("style", "margin-left: 5px;");
             }

             public void resetTextBox() {
                 txtBox.setEnabled(true);
                 showSystemTextBoxMessage(DEFAULT_TBOX_TXT);
                 progressImg.setVisible(false);
             }
         };

         showSystemTextBoxMessage(PROCESSING_TBOX_MSG);
         txtBox.setEnabled(false);
         progressImg.setVisible(true);

         try {
            musicServer.addUserTagsForItem(itemId, tags, callback);
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    public void onLogin(ListenerDetails lD) {
        invokeFetchUserTags();
    }

    public void onLogout() {
        removeAllTags();
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

    public void onDelete() {
        cdm.getLoginListenerManager().removeListener(this);
    }

}


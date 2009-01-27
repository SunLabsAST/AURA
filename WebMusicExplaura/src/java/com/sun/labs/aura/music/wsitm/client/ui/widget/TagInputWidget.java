/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedLabel;
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.event.LoginListener;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sun.labs.aura.music.wsitm.client.event.DEClickHandler;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
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
    private String tagStyleName = null;
    
    private final static String DEFAULT_TBOX_TXT = "Add comma separated tags";
    private final static String PROCESSING_TBOX_MSG = "Processing...";

    public TagInputWidget(MusicSearchInterfaceAsync musicServer,
                ClientDataManager cdm, String itemType, String itemId) {
        this(musicServer, cdm, itemType, itemId, null, null);
    }

    public TagInputWidget(MusicSearchInterfaceAsync musicServer, 
            ClientDataManager cdm, String itemType, String itemId, 
            String titleStyle, String tagStyle) {

        this.musicServer = musicServer;
        this.cdm = cdm;
        this.itemId = itemId;
        this.tagStyleName = tagStyle;

        userTags = new HashMap<String, SpannedLabel>();

        HorizontalPanel mainPanel = new HorizontalPanel();
        Label titleLbl = new Label("Tag this "+itemType+": ");
        titleLbl.getElement().getStyle().setPropertyPx("marginRight", 5);
        if (titleStyle!=null) {
            titleLbl.setStyleName(titleStyle);
        }
        mainPanel.add(titleLbl);

        tagPanel = new FlowPanel();
        mainPanel.add(tagPanel);

        txtBox = new TextBox();
        resetTextBoxIfEmpty();
        txtBox.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                if (event.getNativeEvent().getKeyCode() == 13) {
                    onTagSubmit();
                }
            }
        });
        txtBox.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                clearTextBoxIfDefault();
            }
        });
        txtBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
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

    /**
     * Add a tag to the textbox
     * @param tag to add
     */
    public void addTagToBox(String tag) {
        if (tag!=null && tag.length()>0) {
            clearTextBoxIfDefault();
            if (!txtBox.getText().equals("")) {
                tag=", "+tag;
            }
            txtBox.setText(txtBox.getText()+tag);
        }
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
                SpannedLabel cLabel = new SpannedLabel(", ");
                if (tagStyleName!=null) {
                    cLabel.setStyleName(tagStyleName);
                }
                tagPanel.add(cLabel);
            }
            tagPanel.add(t);
        }
    }

    public void addTag(String tag) {
        //userTags.put(tag, new DeletableTag(new SpannedLabel(tag)));
        SpannedLabel newTag = new SpannedLabel(tag);
        if (tagStyleName!=null) {
            newTag.setStyleName(tagStyleName);
        }
        userTags.put(tag, newTag);
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

    @Override
    public void onLogin(ListenerDetails lD) {
        invokeFetchUserTags();
    }

    @Override
    public void onLogout() {
        removeAllTags();
    }

    public class DeletableTag extends DeletableWidget<SpannedLabel> {

        private String tag;

        public DeletableTag(SpannedLabel w) {
            super(w);
            this.tag = w.getText();
        }

        @Override
        public void onDelete() {
            removeTag(tag);
        }
    }

    @Override
    public void onDelete() {
        cdm.getLoginListenerManager().removeListener(this);
    }


    public static void showTagInputPopup(ArtistCompact aC,
            MusicSearchInterfaceAsync musicServer, ClientDataManager cdm) {

        VerticalPanel vP = new VerticalPanel();
        vP.setStyleName("popupColors");
        vP.setWidth("600px");

        final TagInputWidget tiw = new TagInputWidget(musicServer, cdm, 
                "artist", aC.getId(), "tagPop2", "tagPop1");

        vP.add(tiw);

        FlowPanel sysTagPanel = new FlowPanel();
        sysTagPanel.getElement().getStyle().setPropertyPx("borderTop", 5);
        SpannedLabel sysTagLabel = new SpannedLabel("Suggested tags: ");
        sysTagLabel.addStyleName("tagPop2");
        sysTagPanel.add(sysTagLabel);

        boolean first = true;
        for (ItemInfo iI : aC.getDistinctiveTags()) {
            if (!first) {
                SpannedLabel cLabel = new SpannedLabel(", ");
                cLabel.addStyleName("tagPop1");
                sysTagPanel.add(cLabel);
            } else {
                first = false;
            }
            SpannedLabel l = new SpannedLabel(iI.getItemName());
            l.addStyleName("pointer");
            l.addStyleName("tagPop1");
            l.addClickHandler(new DEClickHandler<String>(iI.getItemName()) {
                @Override
                public void onClick(ClickEvent event) {
                    tiw.addTagToBox(data);
                }
            });
            sysTagPanel.add(l);
        }

        vP.add(tiw);
        vP.add(sysTagPanel);
        
        Popup.showRoundedPopup(vP, "Add tags to "+aC.getName(), Popup.getPopupPanel(), -1, -1);
    }

}


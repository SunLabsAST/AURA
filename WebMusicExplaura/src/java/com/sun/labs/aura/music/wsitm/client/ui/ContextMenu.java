/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.DataEmbededMouseListener;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.steerable.CloudItem;

/**
 *
 * @author mailletf
 */
public class ContextMenu {

    private PopupPanel pp;
    protected VerticalPanel vP;

    private ClickListener hideOnClickListener;

    private boolean newPopup = true;
  
    public ContextMenu() {
        pp = Popup.getPopupPanel();
        vP = new VerticalPanel();

        hideOnClickListener = new ClickListener() {

            public void onClick(Widget sender) {
                hideMenu();
            }
        };
    }

    public boolean isVisible() {
        return pp.isVisible();
    }

    public void hideMenu() {
        if (pp != null) {
            pp.hide();
        }
    }

    public void addElement(Label l, ClickListener cL) {
        l.addClickListener(cL);
        l.addClickListener(hideOnClickListener);
        l.addStyleName("contextMenuItem");
        l.addMouseListener(new DataEmbededMouseListener<Label>(l) {

            public void onMouseEnter(Widget sender) {
                data.addStyleName("contextMenuItemHover");
            }

            public void onMouseLeave(Widget sender) {
                data.removeStyleName("contextMenuItemHover");
            }
            public void onMouseDown(Widget sender, int x, int y) {}
            public void onMouseMove(Widget sender, int x, int y) {}
            public void onMouseUp(Widget sender, int x, int y) {}
        });
        vP.add(l);
    }

    public void addElement(String label, ClickListener cL) {
        Label l = new Label(label);
        addElement(l, cL);
    }

    public void addSeperator() {
        vP.add(new HTML("<hr>"));
    }
    
    public void showAt(Event e) {
        if (vP != null) {
            int x = e.getClientX() + Window.getScrollLeft();
            int y = e.getClientY() + Window.getScrollTop();
            if (newPopup) {
                Popup.showRoundedPopup(vP, "", pp, x, y);
                newPopup = false;
            } else {
                pp.setPopupPosition(x, y);
                pp.show();
            }
        } else {
            Popup.showInformationPopup("Error. Contextmenu is empty.");
        }
    }
    
    public interface HasContextMenu {

        public ContextMenu getContextMenu();
    }
    
    public interface TagDependentSharedMenu {
        
        public void showAt(Event e, ItemInfo currTag);
    }
    
    public interface ArtistDependentSharedMenu {
        
        public void showAt(Event e, ArtistCompact currTag);
    }

    public interface CloudItemDependentSharedMenu {

        public void showAt(Event e, CloudItem cI);
    }
}

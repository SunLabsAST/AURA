/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.Widget;
import java.util.List;

/**
 *
 * @author mailletf
 */
public abstract class AbstractSearchWidget extends Composite {

    public static enum Oracles {
        ARTIST,
        TAG
    }

    public enum searchTypes {
        SEARCH_FOR_ARTIST_BY_ARTIST,
        SEARCH_FOR_ARTIST_BY_TAG,
        SEARCH_FOR_TAG_BY_TAG
    }

    private Oracles currLoadedOracle;
    private Oracles fetchOracle;    // Oracle we are currently fetching

    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private Panel searchBoxContainerPanel; // panel that will contain the searchbox

    protected SuggestBox textBox;
    protected SearchTypeRadioButton[] searchButtons;

    protected String searchBoxStyleName = "searchText";
    protected int searchBoxWidth = 0;

    public AbstractSearchWidget(MusicSearchInterfaceAsync musicServer, 
            ClientDataManager cdm, Panel searchBoxContainerPanel) {
        this.musicServer = musicServer;
        this.cdm = cdm;
        this.searchBoxContainerPanel = searchBoxContainerPanel;
    }

    public abstract void search();

    /**
     * Does the actual swapping of the suggest box with the provided oracle
     * @param newOracle
     */
    private void swapSuggestBox(MultiWordSuggestOracle newOracle, Oracles newOracleType) {

        String oldTxt;
        if (getSearchBox()!=null) {
            oldTxt = getSearchBox().getText();
        } else {
            oldTxt="";
        }

        searchBoxContainerPanel.clear();
        textBox = createSuggestBox(newOracle);
        textBox.setText(oldTxt);
        searchBoxContainerPanel.add(getSearchBox());

        if (newOracleType==Oracles.ARTIST) {
            cdm.setArtistOracle(newOracle);
            currLoadedOracle = Oracles.ARTIST;
        } else {
            cdm.setTagOracle(newOracle);
            currLoadedOracle = Oracles.TAG;
        }
    }

    public void setSearchBox(SuggestBox box) {
        this.textBox = box;
    }

    public SuggestBox getSearchBox() {
        return textBox;
    }

    protected searchTypes getSearchType() {
        for (SearchTypeRadioButton rB : searchButtons) {
            if (rB.isChecked()) {
                return rB.getSearchType();
            }
        }
        return null;
    }

    protected void setText(String text, searchTypes searchType) {
        textBox.setText(text);
        for (SearchTypeRadioButton rB : searchButtons) {
            rB.setChecked(rB.getSearchType() == searchType);
        }
    }

    public Oracles getCurrLoadedOracle() {
        return currLoadedOracle;
    }
    
    /**
     * Update suggest box with new oracle if necessary. Will fetch oracle if it
     * is currently null
     * @param type artist or tag
     */
    public void updateSuggestBox(Oracles type) {
        if (currLoadedOracle!=null && currLoadedOracle == type) {
            return;
        } else {
            if (type == Oracles.ARTIST) {
                if (cdm.getArtistOracle() == null) {
                    invokeOracleFetchService(Oracles.ARTIST);
                } else {
                    swapSuggestBox(cdm.getArtistOracle(), Oracles.ARTIST);
                    fetchOracle = null;
                }
            } else {
                if (cdm.getTagOracle() == null) {
                    invokeOracleFetchService(Oracles.TAG);
                } else {
                    swapSuggestBox(cdm.getTagOracle(), Oracles.TAG);
                    fetchOracle = null;
                }
            }
        }
    }

    private SuggestBox createSuggestBox(MultiWordSuggestOracle oracle) {
        SuggestBox sbox = new SuggestBox(oracle);

        if (searchBoxStyleName != null && searchBoxStyleName.length()>0) {
            sbox.setStyleName(searchBoxStyleName);
        }
        if (searchBoxWidth>0) {
            this.textBox.getElement().setAttribute("style", "width: "+searchBoxWidth+"px;");
        }
        sbox.ensureDebugId ("cwSuggestBox");
        sbox.setLimit(20);

        sbox.addKeyboardListener(new KeyboardListenerAdapter() {
            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                if (keyCode == KEY_ENTER) {

                    /* Hack to go around the bug of the suggestbox which wasn't
                     * using the highlighted element of the suggetions popup
                     * when submitting the form
                     * */
                    DeferredCommand.addCommand(new Command(){ public void execute(){
                        search();
                    }});
                } else if (keyCode == KEY_ESCAPE) {
                    //Window.alert("escape!!");
                    //MouseListenerCollection a = new MouseListenerCollection();
                    //DOM.
                    //a.fireMouseEvent(sender, new Event(Event.ONCLICK));
                    //a.fireMouseDown(sender, sender.getAbsoluteLeft(), sender.getAbsoluteTop());
                    //a.fireMouseUp(sender, sender.getAbsoluteLeft(), sender.getAbsoluteTop());
                }
            }
        });

        return sbox;
    }

    public void setSuggestBoxWidth(int width) {
        this.searchBoxWidth = width;
        if (searchBoxWidth>0) {
            this.textBox.setWidth(searchBoxWidth+"px");
        }
    }

    private void invokeOracleFetchService(Oracles type) {

        AsyncCallbackWithType callback = new AsyncCallbackWithType(type) {

            public void onSuccess(Object result) {
                List<String> callBackList = (List<String>) result;
                MultiWordSuggestOracle newOracle = new MultiWordSuggestOracle();
                newOracle.addAll(callBackList);
                swapSuggestBox(newOracle, this.type);
            }

            public void onFailure(Throwable caught) {
                Window.alert(caught.toString());
            }
        };

        searchBoxContainerPanel.clear();
        searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

        try {
            if (type == Oracles.ARTIST) {
                musicServer.getArtistOracle(callback);
            } else {
                musicServer.getTagOracle(callback);
            }
        } catch (Exception ex) {
            Window.alert(ex.getMessage());
        }
    }

    abstract class AsyncCallbackWithType implements AsyncCallback {

        public Oracles type;

        public AsyncCallbackWithType(Oracles type) {
            super();
            this.type=type;
        }

        public abstract void onFailure(Throwable arg0);
        public abstract void onSuccess(Object arg0);

    }

    public class SearchTypeRadioButton extends RadioButton {

        private searchTypes searchType;

        public SearchTypeRadioButton(String name, String label, searchTypes searchType) {
            super(name, label);
            this.searchType = searchType;
        }

        public searchTypes getSearchType() {
            return searchType;
        }

    }
}

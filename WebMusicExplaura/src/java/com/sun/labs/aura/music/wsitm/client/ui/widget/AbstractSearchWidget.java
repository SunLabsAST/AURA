/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import java.util.ArrayList;

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

    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private Panel searchBoxContainerPanel; // panel that will contain the searchbox

    private SuggestBox sB;
    protected SearchTypeRadioButton[] searchButtons;

    protected String searchBoxStyleName = ""; //searchText";
    protected int searchBoxWidth = 0;

    public AbstractSearchWidget(MusicSearchInterfaceAsync musicServer, 
            ClientDataManager cdm, Panel searchBoxContainerPanel, Oracles type) {
        this.musicServer = musicServer;
        this.cdm = cdm;
        this.searchBoxContainerPanel = searchBoxContainerPanel;
        
        sB = getNewSuggestBox(new PopSortedMultiWordSuggestOracle());
        updateSuggestBox(type);
    }

    public abstract void search();

    private SuggestBox getNewSuggestBox(PopSortedMultiWordSuggestOracle oracle) {
        SuggestBox box = new SuggestBox(oracle);
        box.setLimit(15);
        box.addKeyboardListener(new KeyboardListener() {

            public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                // If enter key pressed, submit the form
                if (keyCode == 13) {
                    DeferredCommand.addCommand(new Command() {

                        public void execute() {
                            search();
                        }
                    });
                }
            }

            public void onKeyDown(Widget sender, char keyCode, int modifiers) {}
            public void onKeyUp(Widget sender, char keyCode, int modifiers) {}
        });
        return box;
    }

    /**
     * Does the actual swapping of the suggest box with the provided oracle
     * @param newOracle
     */
    private void swapSuggestBox(PopSortedMultiWordSuggestOracle newOracle, Oracles newOracleType) {

        String oldTxt;
        if (getSearchBox() != null) {
            oldTxt = getSearchBox().getText();
        } else {
            oldTxt = "";
        }

        searchBoxContainerPanel.clear();

        sB = getNewSuggestBox(newOracle);
        sB.setText(oldTxt);
        searchBoxContainerPanel.add(getSearchBox());

        if (newOracleType == Oracles.ARTIST) {
            cdm.setArtistOracle(newOracle);
            currLoadedOracle = Oracles.ARTIST;
        } else {
            cdm.setTagOracle(newOracle);
            currLoadedOracle = Oracles.TAG;
        }
    }

    public void setSearchBox(SuggestBox box) {
        this.sB = box;
    }

    public SuggestBox getSearchBox() {
        return sB;
    }

    protected searchTypes getSearchType() {
        for (SearchTypeRadioButton rB : searchButtons) {
            if (rB.isChecked()) {
                return rB.getSearchType();
            }
        }
        return null;
    }
    
    public void setText(String text) {
        if (sB != null) {
            sB.setText(text);
        }
    }

    public void setText(String text, searchTypes searchType) {
        if (sB != null) {
            sB.setText(text);
            for (SearchTypeRadioButton rB : searchButtons) {
                rB.setChecked(rB.getSearchType() == searchType);
            }
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
        if (currLoadedOracle != null && currLoadedOracle == type) {
            return;
        } else {
            if (type == Oracles.ARTIST) {
                if (cdm.getArtistOracle() == null) {
                    invokeOracleFetchService(Oracles.ARTIST);
                } else {
                    swapSuggestBox(cdm.getArtistOracle(), Oracles.ARTIST);
                }
            } else {
                if (cdm.getTagOracle() == null) {
                    invokeOracleFetchService(Oracles.TAG);
                } else {
                    swapSuggestBox(cdm.getTagOracle(), Oracles.TAG);
                }
            }
        }
    }

    public void setSuggestBoxWidth(int width) {
        this.searchBoxWidth = width;
        if (searchBoxWidth>0) {
            this.sB.setWidth(searchBoxWidth+"px");
        }
    }

    private void invokeOracleFetchService(Oracles type) {

        AsyncCallbackWithType callback = new AsyncCallbackWithType(type) {

            public void onSuccess(ArrayList<ScoredC<String>> callBackList) {

                PopSortedMultiWordSuggestOracle newOracle = new PopSortedMultiWordSuggestOracle();
                for (ScoredC<String> item : callBackList) {
                    newOracle.add(item.getItem(), item.getScore());
                }
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

    protected abstract class AsyncCallbackWithType implements AsyncCallback<ArrayList<ScoredC<String>>> {

        public Oracles type;

        public AsyncCallbackWithType(Oracles type) {
            super();
            this.type=type;
        }
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

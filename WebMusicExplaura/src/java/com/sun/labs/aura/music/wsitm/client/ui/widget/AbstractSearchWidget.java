/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.sun.labs.aura.music.wsitm.client.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SuggestBox;
import com.sun.labs.aura.music.wsitm.client.event.DEAsyncCallback;
import com.sun.labs.aura.music.wsitm.client.items.ScoredC;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
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

    private static MultiRpcManager tagOracleRpcManager;
    private static MultiRpcManager artistOracleRpcManager;

    private Oracles currLoadedOracle;

    private MusicSearchInterfaceAsync musicServer;
    private ClientDataManager cdm;

    private Panel searchBoxContainerPanel; // panel that will contain the searchbox

    private SuggestBox sB;

    protected String searchBoxStyleName = null; //searchText";
    protected int searchBoxWidth = 0;

    public AbstractSearchWidget(MusicSearchInterfaceAsync musicServer, 
            ClientDataManager cdm, Panel searchBoxContainerPanel, Oracles type) {
        init(musicServer, cdm, searchBoxContainerPanel, type);
    }

    public AbstractSearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, Panel searchBoxContainerPanel, Oracles type,
            String searchBoxStyle) {
        this.searchBoxStyleName = searchBoxStyle;
        init(musicServer, cdm, searchBoxContainerPanel, type);
    }

    private void init(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, Panel searchBoxContainerPanel, Oracles type) {
        this.musicServer = musicServer;
        this.cdm = cdm;
        this.searchBoxContainerPanel = searchBoxContainerPanel;
        
        sB = getNewSuggestBox(new PopSortedMultiWordSuggestOracle());
        updateSuggestBox(type);
    }

    protected abstract searchTypes getSearchType();
    public abstract void setSearchType(searchTypes searchType);
    public abstract void search();

    private SuggestBox getNewSuggestBox(PopSortedMultiWordSuggestOracle oracle) {
        SuggestBox box = new SuggestBox(oracle);
        box.setLimit(15);
        box.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                // If enter key pressed, submit the form
                if (event.getNativeEvent().getKeyCode() == 13) {
                    DeferredCommand.addCommand(new Command() {
                        @Override
                        public void execute() {
                            search();
                        }
                    });
                }
                
            }
        });
        if (searchBoxStyleName != null && searchBoxStyleName.length()>0) {
            box.addStyleName(searchBoxStyleName);
        }
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
    
    public void setText(String text) {
        if (sB != null) {
            sB.setText(text);
        }
    }

    public void setText(String text, searchTypes searchType) {
        if (sB != null) {
            sB.setText(text);
            setSearchType(searchType);
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

    private void invokeOracleFetchService(final Oracles type) {

        DEAsyncCallback<Oracles, ArrayList<ScoredC<String>>> callback =
                new DEAsyncCallback<Oracles, ArrayList<ScoredC<String>>>(type) {

            @Override
            public void onSuccess(ArrayList<ScoredC<String>> callBackList) {

                PopSortedMultiWordSuggestOracle newOracle = new PopSortedMultiWordSuggestOracle();
                for (ScoredC<String> item : callBackList) {
                    newOracle.add(item.getItem(), item.getScore());
                }
                swapSuggestBox(newOracle, this.data);
            }

            @Override
            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                        "fetch the search oracle.", Popup.ERROR_LVL.NORMAL,
                        new DECommand<Oracles>(type) {
                    @Override
                    public void execute() {
                        invokeOracleFetchService(type);
                    }
                });
            }
        };

        searchBoxContainerPanel.clear();
        searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

        try {
            if (type == Oracles.ARTIST) {

                if (artistOracleRpcManager != null && artistOracleRpcManager.isInRpc() ) {
                    artistOracleRpcManager.addCallback(callback);
                } else {
                    artistOracleRpcManager = new MultiRpcManager(callback);
                    musicServer.getArtistOracle(artistOracleRpcManager);
                }
            } else {
                if (tagOracleRpcManager != null && tagOracleRpcManager.isInRpc()) {
                    tagOracleRpcManager.addCallback(callback);
                } else {
                    tagOracleRpcManager = new MultiRpcManager(callback);
                    musicServer.getTagOracle(tagOracleRpcManager);
                }
            }
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "fetch the search oracle.", Popup.ERROR_LVL.NORMAL,
                    new DECommand<Oracles>(type) {
                        @Override
                        public void execute() {
                            invokeOracleFetchService(type);
                        }
                    });
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

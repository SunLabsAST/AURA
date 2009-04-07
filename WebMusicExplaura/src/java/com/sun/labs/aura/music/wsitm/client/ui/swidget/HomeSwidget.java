/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterfaceAsync;
import com.sun.labs.aura.music.wsitm.client.WebLib;
import com.sun.labs.aura.music.wsitm.client.items.ArtistCompact;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.ui.Popup;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.SearchTypeRadioButton;
import com.sun.labs.aura.music.wsitm.client.ui.widget.CompactArtistWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.DualRoundedPanel;
import com.sun.labs.aura.music.wsitm.client.ui.widget.StarRatingWidget.InitialRating;
import java.util.ArrayList;

/**
 *
 * @author mailletf
 */
public class HomeSwidget extends Swidget {

    private final boolean DEBUG_MODE = false;
    private final int POP_ART_HEIGHT = 2;
    private final int POP_ART_WIDTH = 3;

    private Grid mainPanel;
    private DualRoundedPanel popArtists;

    public HomeSwidget(ClientDataManager cdm) {
        super("Home", cdm);

        HorizontalPanel titleHp = new HorizontalPanel();
        titleHp.setWidth("100%");
        titleHp.setStyleName("h2");
        titleHp.add(new Label("Popular artists"));
        titleHp.setHorizontalAlignment(HorizontalPanel.ALIGN_RIGHT);
        Label featMore = new Label("More");
        featMore.addStyleName("headerMenuMedItem");
        featMore.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                invokeFetchRandomArtists();
            }
        });

        titleHp.add(featMore);
        popArtists = new DualRoundedPanel();
        popArtists.setVisible(false);
        popArtists.setHeader(titleHp);
        popArtists.setContent(WebLib.getSunLoaderWidget(), false);

        mainPanel = new Grid(3,1);
        mainPanel.getCellFormatter().setHorizontalAlignment(0, 0, HorizontalPanel.ALIGN_CENTER);
        //mainPanel.setWidget(0, 0, search);
        mainPanel.getCellFormatter().setHorizontalAlignment(1, 0, HorizontalPanel.ALIGN_CENTER);
        mainPanel.setWidget(1, 0, popArtists);

        if (DEBUG_MODE) {
            Label exLabel = new Label("Trigger exception");
            exLabel.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    invokeTriggerDebugException();
                }
            });
            mainPanel.setWidget(2, 0, exLabel);
        }

        initWidget(mainPanel, true);
        invokeFetchRandomArtists();
    }

    @Override
    public ArrayList<String> getTokenHeaders() {
        ArrayList<String> l = new ArrayList<String>();
        l.add("searchHome:");
        return l;
    }

    @Override
    protected void initMenuItem() {
        // no menu
        menuItem = new MenuItem();
    }

    @Override
    public void doRemoveListeners() {
        // no listeners
    }

    public class SearchWidget extends AbstractSearchWidget {

        private SearchTypeRadioButton[] searchButtons;

        public SearchWidget(MusicSearchInterfaceAsync musicServer,
            ClientDataManager cdm, FlowPanel searchBoxContainerPanel) {

            super(musicServer, cdm, searchBoxContainerPanel, Oracles.ARTIST);

            searchBoxContainerPanel.add(WebLib.getSunLoaderWidget());

            Panel searchType = new VerticalPanel();
            searchButtons = new SearchTypeRadioButton[3];
            searchButtons[0] = new SearchTypeRadioButton("searchType", "For Artist", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);
            searchButtons[1] = new SearchTypeRadioButton("searchType", "By Tag", searchTypes.SEARCH_FOR_ARTIST_BY_TAG);
            searchButtons[2] = new SearchTypeRadioButton("searchType", "For Tag", searchTypes.SEARCH_FOR_TAG_BY_TAG);

            searchButtons[0].addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateSuggestBox(Oracles.ARTIST);
                }
            });
            searchButtons[1].addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateSuggestBox(Oracles.TAG);
                }
            });
            searchButtons[2].addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    updateSuggestBox(Oracles.TAG);
                }
            });

            //updateSuggestBox(Oracles.ARTIST);  -- done in constructor
            setText("", searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST);

            for (int i = 0; i < searchButtons.length; i++) {
                searchType.add(searchButtons[i]);
                searchButtons[i].setStyleName("searchTypeButton");
            }
            searchType.setWidth("100%");
            searchType.setStyleName("searchPanel");

            HorizontalPanel searchPanel = new HorizontalPanel();
            searchPanel.setStyleName("searchPanel");
            searchPanel.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            searchPanel.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);

            Button searchButton = new Button("Search", new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    search();
                }
            });
            searchButton.addStyleName("main");
            searchButton.setTabIndex(2);

            VerticalPanel leftP = new VerticalPanel();
            leftP.setHeight("100%");
            leftP.setWidth("100%");
            leftP.setHorizontalAlignment(HorizontalPanel.ALIGN_CENTER);
            leftP.setVerticalAlignment(VerticalPanel.ALIGN_MIDDLE);
            leftP.add(searchBoxContainerPanel);
            leftP.add(searchButton);
            searchPanel.add(leftP);
            searchPanel.add(searchType);
            this.initWidget(searchPanel);
        }

        @Override
        public void search() {
            if (cdm.getCurrSimTypeName() == null || cdm.getCurrSimTypeName().equals("")) {
                Popup.showErrorPopup("Similarity types have not been loaded.", Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "perform your search.", Popup.ERROR_LVL.NORMAL, new Command() {
                    @Override
                    public void execute() {
                        // Retry will resubmit RPC to get sim types and then redo search
                        cdm.getPageHeaderWidget().requestRefreshSimTypes();
                        new Timer() {
                            @Override
                            public void run() {
                                search();
                            }
                        }.schedule(1500);
                    }

                });

            } else {
                String query = getSearchBox().getText().toLowerCase();
                searchTypes currST = getSearchType();
                if (currST == searchTypes.SEARCH_FOR_TAG_BY_TAG) {
                    History.newItem("tagSearch:"+query);
                } else if (currST == searchTypes.SEARCH_FOR_ARTIST_BY_ARTIST) {
                    History.newItem("artistSearch:"+query);
                } else {
                    History.newItem("artistSearchByTag::"+query);
                }
            }
        }

        @Override
        protected searchTypes getSearchType() {
            for (SearchTypeRadioButton rB : searchButtons) {
                if (rB.isChecked()) {
                    return rB.getSearchType();
                }
            }
            return null;
        }

        @Override
        public void setSearchType(searchTypes searchType) {
            for (SearchTypeRadioButton rB : searchButtons) {
                rB.setChecked(rB.getSearchType() == searchType);
            }
        }
    }

    /**
     * Debug method used to trigger an RPC exception
     */
    public void invokeTriggerDebugException() {

        AsyncCallback callback = new AsyncCallback() {

            @Override
            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.NONE,
                    "onFailure in callback.", Popup.ERROR_LVL.NORMAL, null);
            }

            @Override
            public void onSuccess(Object result) {
                Popup.showInformationPopup("callback success!");
            }
        };

        try {
            musicServer.triggerException(callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.NONE,
                    "in catch of trigger exception", Popup.ERROR_LVL.NORMAL, null);
        }

    }

    public void invokeFetchRandomArtists() {

        AsyncCallback<ArtistCompact[]> callback = new AsyncCallback<ArtistCompact[]>() {

            public void onSuccess(ArtistCompact[] aCList) {
                Grid g = new Grid(POP_ART_HEIGHT, POP_ART_WIDTH);
                int idx = 0;
                for (int h = 0; h < POP_ART_HEIGHT; h++) {
                    for (int w = 0; w < POP_ART_WIDTH; w++) {
                        g.setWidget(h, w,
                                new CompactArtistWidget(aCList[idx++], cdm,
                                musicServer, null, null, InitialRating.FETCH, null));
                    }
                }
                
                popArtists.setVisible(true);
                popArtists.setContent(g);
                hideLoader();
            }

            public void onFailure(Throwable caught) {
                Popup.showErrorPopup(caught, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve the random artists.", Popup.ERROR_LVL.NORMAL, null);
                hideLoader();
            }
        };

        showLoader();
        try {
            musicServer.getRandomPopularArtists(POP_ART_WIDTH * POP_ART_HEIGHT, callback);
        } catch (Exception ex) {
            Popup.showErrorPopup(ex, Popup.ERROR_MSG_PREFIX.ERROR_OCC_WHILE,
                    "retrieve the random artists.", Popup.ERROR_LVL.NORMAL, null);
            hideLoader();
        }
    }

}

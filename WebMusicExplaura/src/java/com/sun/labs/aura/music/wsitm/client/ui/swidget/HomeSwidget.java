/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.swidget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.sun.labs.aura.music.wsitm.client.ClientDataManager;
import com.sun.labs.aura.music.wsitm.client.MusicSearchInterfaceAsync;
import com.sun.labs.aura.music.wsitm.client.WebLib;
import com.sun.labs.aura.music.wsitm.client.ui.MenuItem;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.Oracles;
import com.sun.labs.aura.music.wsitm.client.ui.widget.AbstractSearchWidget.SearchTypeRadioButton;
import java.util.ArrayList;

/**
 *
 * @author mailletf
 */
public class HomeSwidget extends Swidget {

    private FlowPanel searchBoxContainerPanel;
    private SearchWidget search;

    public HomeSwidget(ClientDataManager cdm) {
        super("Home", cdm);

        searchBoxContainerPanel = new FlowPanel();

        search = new SearchWidget(musicServer, cdm, searchBoxContainerPanel);
        search.updateSuggestBox(Oracles.ARTIST);

        initWidget(search);

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

            searchBoxContainerPanel.add(WebLib.getLoadingBarWidget());

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
                Window.alert("Error. Cannot search without the similarity types.");
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

}

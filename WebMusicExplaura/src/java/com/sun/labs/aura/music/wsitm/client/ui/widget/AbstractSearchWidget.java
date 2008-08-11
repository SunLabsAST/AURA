/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.sun.labs.aura.music.wsitm.client.*;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RadioButton;
import com.gwtext.client.core.EventObject;
import com.gwtext.client.data.Record;
import com.gwtext.client.widgets.BoxComponent;
import com.gwtext.client.widgets.Component;
import com.gwtext.client.widgets.form.ComboBox;
import com.gwtext.client.widgets.form.Field;
import com.gwtext.client.widgets.form.event.ComboBoxCallback;
import com.gwtext.client.widgets.form.event.ComboBoxListener;
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

    protected ComboBox textBox;
    protected SearchTypeRadioButton[] searchButtons;

    protected String searchBoxStyleName = ""; //searchText";
    protected int searchBoxWidth = 0;

    public AbstractSearchWidget(MusicSearchInterfaceAsync musicServer, 
            ClientDataManager cdm, Panel searchBoxContainerPanel) {
        this.musicServer = musicServer;
        this.cdm = cdm;
        this.searchBoxContainerPanel = searchBoxContainerPanel;

        textBox = createSuggestBox();
    }

    public abstract void search();

    /**
     * Does the actual swapping of the suggest box with the provided oracle
     * @param newOracle
     */
    private void swapSuggestBox(UniqueStore newOracle, Oracles newOracleType) {

        String oldTxt;
        if (getSearchBox()!=null) {
            oldTxt = getSearchBox().getText();
        } else {
            oldTxt="";
        }

        searchBoxContainerPanel.clear();

        if (textBox == null) {
            textBox = createSuggestBox(newOracle);
        } else {
            textBox.setStore(newOracle);
        }
        textBox.setValue(oldTxt);
        searchBoxContainerPanel.add(getSearchBox());

        if (newOracleType==Oracles.ARTIST) {
            cdm.setArtistOracle(newOracle);
            currLoadedOracle = Oracles.ARTIST;
        } else {
            cdm.setTagOracle(newOracle);
            currLoadedOracle = Oracles.TAG;
        }
    }

    public void setSearchBox(ComboBox box) {
        this.textBox = box;
    }

    public ComboBox getSearchBox() {
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

    public void setText(String text, searchTypes searchType) {
        if (textBox != null) {
            textBox.setValue(text);
            //textBox.setText(text);
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

    protected ComboBox createSuggestBox(UniqueStore oracle) {
        ComboBox cB = createSuggestBox();
        cB.setStore(oracle);
        return cB;
    }

    protected ComboBox createSuggestBox() {
        
        ComboBox cb = new ComboBox();  
        cb.setMinChars(1);  
        cb.setFieldLabel("");
        cb.setDisplayField("name");  
        cb.setMode(ComboBox.LOCAL);
        cb.setEmptyText("Search here");  
        cb.setLoadingText("Searching...");  
        cb.setTypeAhead(true);  
        cb.setSelectOnFocus(true);  
        cb.setWidth(200);  
        //do not show drop fown icon  
        cb.setHideTrigger(true);  
        
        if (searchBoxStyleName != null && searchBoxStyleName.length()>0) {
            cb.setStyleName(searchBoxStyleName);
        }
        if (searchBoxWidth>0) {
            this.textBox.getElement().setAttribute("style", "width: "+searchBoxWidth+"px;");
        }

        cb.addListener(new ComboBoxListener() {

            public void onSpecialKey(Field field, EventObject e) {
                // If user pressed enter key
                if (e.getKey() == 13) {
                    DeferredCommand.addCommand(new Command(){ public void execute(){
                            search();
                        }});
                }
            }
            public boolean doBeforeQuery(ComboBox arg0, ComboBoxCallback arg1) { return true; }
            public boolean doBeforeSelect(ComboBox arg0, Record arg1, int arg2) { return true; }
            public void onCollapse(ComboBox arg0) {}
            public void onExpand(ComboBox arg0) {}
            public void onSelect(ComboBox arg0, Record arg1, int arg2) {}
            public void onBlur(Field arg0) {}
            public void onChange(Field arg0, Object arg1, Object arg2) {}
            public void onFocus(Field arg0) {}
            public void onInvalid(Field arg0, String arg1) {}
            public void onValid(Field arg0) {}
            public void onMove(BoxComponent arg0, int arg1, int arg2) {}
            public void onResize(BoxComponent arg0, int arg1, int arg2, int arg3, int arg4) {}
            public boolean doBeforeDestroy(Component arg0) { return true; }
            public boolean doBeforeHide(Component arg0) { return true;}
            public boolean doBeforeRender(Component arg0) { return true;}
            public boolean doBeforeShow(Component arg0) { return true;}
            public boolean doBeforeStateRestore(Component arg0, JavaScriptObject arg1) { return true;}
            public boolean doBeforeStateSave(Component arg0, JavaScriptObject arg1) { return true; }
            public void onDestroy(Component arg0) {}
            public void onDisable(Component arg0) {}
            public void onEnable(Component arg0) {}
            public void onHide(Component arg0) {}
            public void onRender(Component arg0) {}
            public void onShow(Component arg0) {}
            public void onStateRestore(Component arg0, JavaScriptObject arg1) {}
            public void onStateSave(Component arg0, JavaScriptObject arg1) {}
        });
        
        return cb;
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
                
                UniqueStore newStore = new UniqueStore("name", callBackList.toArray(new String[0]));
                newStore.load();
                
                swapSuggestBox(newStore, this.type);
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

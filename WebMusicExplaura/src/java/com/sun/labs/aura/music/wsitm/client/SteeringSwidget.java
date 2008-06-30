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
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.items.ItemInfo;
import com.sun.labs.aura.music.wsitm.client.items.ListenerDetails;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mailletf
 */
public class SteeringSwidget extends Swidget {

    private MainPanel mP;

    public SteeringSwidget(ClientDataManager cdm) {
        super("Steering", cdm);
        mP = new MainPanel();
        registerLoginListener(mP);
        initWidget(mP);
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("steering:");
        return l;
    }

    private class MainPanel extends LoginListener {

        private DockPanel dP;
        private Grid mainTagPanel;
        private Grid mainArtistListPanel;
        private ResizableTagWidget tagLand;
        private ArtistListWidget artistList;

        private SuggestBox sbox;

        public MainPanel() {
            dP = new DockPanel();

            // Right
            mainTagPanel = new Grid(2,1);
            mainTagPanel.setWidth("200px");
            updateTagSuggestBox();
            mainTagPanel.setWidget(1, 0, new Label("Search for tags to add using the above search box"));
            dP.add(WebLib.createSection("Add tag", mainTagPanel), DockPanel.EAST);

            // Left
            mainArtistListPanel = new Grid(1,1);
            mainArtistListPanel.setWidget(0, 0, new Label("Add tags to your tag cloud to get recommendations"));
            dP.add(WebLib.createSection("Recommendations", mainArtistListPanel), DockPanel.WEST);

            // North
            HorizontalPanel hP = new HorizontalPanel();
            hP.add(new Label("Name :"));
            hP.add(new TextBox());
            dP.add(hP, DockPanel.NORTH);

            tagLand = new ResizableTagWidget();
            dP.add(tagLand, DockPanel.NORTH);

            initWidget(dP);
        }

        private void updateTagSuggestBox() {
            if (cdm.getTagOracle() == null) {
                invokeOracleFetchService();
                return;
            }

            sbox = new SuggestBox(cdm.getTagOracle());
            sbox.setStyleName("searchText");
            sbox.ensureDebugId("cwSuggestBox");
            sbox.setLimit(20);

            sbox.addKeyboardListener(new KeyboardListenerAdapter() {

                public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                    if (keyCode == KEY_ENTER) {

                        /* Hack to go around the bug of the suggestbox which wasn't
                         * using the highlighted element of the suggetions popup
                         * when submitting the form
                         * */
                        DeferredCommand.addCommand(new Command() {

                            public void execute() {
                                invokeTagSearchService(sbox.getText().toLowerCase(), 0);
                            }
                        });
                    }
                }
            });

            mainTagPanel.setWidget(0, 0, sbox);
        }

        public void onLogin(ListenerDetails lD) {
        }

        public void onLogout() {
        }

        private void invokeTagSearchService(String searchText, int page) {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    // do some UI stuff to show success
                    SearchResults sr = (SearchResults) result;
                    if (sr != null && sr.isOK()) {
                        ItemInfo[] results = sr.getItemResults();
                        if (results.length == 0) {
                            mainTagPanel.setWidget(1, 0, new Label("No Match for " + sr.getQuery()));
                        //} else if (results.length == 1) {
                        //    ItemInfo ar = results[0];
                        //    invokeGetTagInfo(ar.getId(), false);
                        } else {
                            VerticalPanel hP = new VerticalPanel();
                            for (ItemInfo iI : results) {
                                cdm.getTagOracle().add(iI.getItemName());
                                Label lbl = new Label(iI.getItemName());
                                lbl.addClickListener(new TagClickListener(iI));
                                hP.add(lbl);
                            }
                            mainTagPanel.setWidget(1, 0, hP);
                        }
                    } else {
                        if (sr == null) {
                            Window.alert("Error. Resultset is null. There were probably no tags foud.s");
                        } else {
                            Window.alert("Whoops " + sr.getStatus());
                        }
                    }
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            mainTagPanel.setWidget(1, 0, WebLib.getLoadingBarWidget());

            try {
                musicServer.tagSearch(searchText, 100, callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        private void invokeOracleFetchService() {

            AsyncCallback callback = new AsyncCallback() {

                public void onSuccess(Object result) {
                    // do some UI stuff to show success
                    List<String> callBackList = (List<String>) result;
                    MultiWordSuggestOracle newOracle = new MultiWordSuggestOracle();
                    newOracle.addAll(callBackList);
                    cdm.setTagOracle(newOracle);
                    updateTagSuggestBox();
                }

                public void onFailure(Throwable caught) {
                    Window.alert(caught.getMessage());
                }
            };

            mainTagPanel.setWidget(0, 0, WebLib.getLoadingBarWidget());

            try {
                musicServer.getTagOracle(callback);
            } catch (Exception ex) {
                Window.alert(ex.getMessage());
            }
        }

        public class TagClickListener implements ClickListener {

            ItemInfo iI;

            public TagClickListener(ItemInfo iI) {
                this.iI = iI;
            }

            public void onClick(Widget arg0) {
                tagLand.addTag(iI);
            }
        }
    }

    public class ResizableTagWidget extends Composite {

        private Map<String, Double> tagMap;
        private List<ResizableTag> tagCloud;

        private Grid g;
        private FlowPanel fP;
        private FlowPanel flowP;

        private int lastX;
        private int lastY;

        public ResizableTagWidget() {

            //this.tagMap = tagMap;
            FlowPanel mainp = new FlowPanel();
            fP = new FlowPanel();
            fP.setWidth("100%");
            flowP = new FlowPanel();
            //fP.setWidget(flowP);
            fP.add(flowP);
            mainp.add(fP);
            initWidget(mainp);

            tagCloud = new LinkedList<ResizableTag>();
            tagCloud.add(new ResizableTag("i am a happy tag", 1.0)); //, fP));

            for (ResizableTag rT : tagCloud) {
                flowP.add(rT);
            }

            fP.setWidth("100%");
            /*
            fP.addMouseListener(new MouseListener() {

                public void onMouseDown(Widget arg0, int arg1, int arg2) {
                    lastX = arg1;
                    lastY = arg2;
                }

                public void onMouseEnter(Widget arg0) {
                }

                public void onMouseLeave(Widget arg0) {
                }

                public void onMouseMove(Widget arg0, int arg1, int arg2) {
                    int increment = lastX - arg1;
                    for (ResizableTag rT : tagCloud) {
                        rT.updateSize(increment);
                    }
                    lastX = arg1;
                    lastY = arg2;
                }

                public void onMouseUp(Widget arg0, int arg1, int arg2) {
                }
            });
             * */
        }

        public void addTag(ItemInfo tag) {
            ResizableTag rT = new ResizableTag(tag.getItemName(), 1); //, fP);
            tagCloud.add(rT);
            flowP.add(rT);
            Info.display("resizableTabWidget","add tag triggered", new Params());
        }

        public class ResizableTag extends Composite {

            private Label lbl;
            private boolean hasClicked = false;
            private FocusPanel fP;
            private int currentSize;
            private double value;

            public ResizableTag(String txt, double initValue) { // , FocusPanel fP) {

                lbl = new Label(txt);
                //this.fP = fP;

                //Label lbl = new Label("i am a happy tag");
                lbl.setHeight(currentSize + "px");
                lbl.addMouseListener(new MouseListener() {

                    public void onMouseDown(Widget arg0, int arg1, int arg2) {
                        hasClicked = true;
                    }

                    public void onMouseEnter(Widget arg0) {
                    }

                    public void onMouseLeave(Widget arg0) {
                    }

                    public void onMouseMove(Widget arg0, int arg1, int arg2) {
                    }

                    public void onMouseUp(Widget arg0, int arg1, int arg2) {
                        hasClicked = false;
                    }
                });

                initWidget(lbl);
            }

            public void updateSize(int increment) {
                currentSize += increment;
                lbl.setHeight(currentSize + "px");
            }
        }
    }
}

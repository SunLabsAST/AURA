/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ChangeListener;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Presents a way to select between the different components of the tool.
 */
public class MainSelector extends DockPanel {

    Label label;
    AdminServiceAsync service;

    public MainSelector() {
        super();

        service = GWTMainEntryPoint.getAdminService();

        Panel panel = new FlowPanel();
        panel.add(new Label("Music Administrator"));
        panel.setStyleName("title");

        add(panel, NORTH);
        add(getTabPanel(), CENTER);
    }

    public Widget getTabPanel() {
        // Create a tab panel
        DecoratedTabPanel tabPanel = new DecoratedTabPanel();
        tabPanel.setWidth("900px");
        tabPanel.setAnimationEnabled(true);

        tabPanel.add(new StatisticsWidget(), "Show Status");
        tabPanel.add(new AddWidget(), "Add Items");
        tabPanel.add(new TestingWidget(true), "Quick Diagnostics");
        tabPanel.add(new TestingWidget(false), "Full Diagnostics");
        tabPanel.add(new WorkbenchPanel(service), "Workbench");

        tabPanel.selectTab(0);
        tabPanel.ensureDebugId("cwTabPanel");
        return tabPanel;
    }

    class StatisticsWidget extends Composite {

        private Grid grid;
        private StatusWidget status = new StatusWidget();

        StatisticsWidget() {
            Panel panel = new FlowPanel();
            grid = new Grid();
            grid.setTitle("Database Statistics");
            panel.add(grid);
            Button refresh = new Button("Refresh");
            refresh.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    update();
                }
            });
            panel.add(refresh);

            panel.add(status);

            initWidget(panel);
            update();
        }

        void update() {
            status.processing();
            service.getStatistics(new AsyncCallback() {

                public void onFailure(Throwable t) {
                    if (t instanceof AdminException) {
                        AdminException e = (AdminException) t;
                        status.error(e.getDisplayMessage());
                    } else {
                        status.error(t.getMessage());
                    }
                }

                public void onSuccess(Object result) {
                    status.clear();
                    Map<String, String> map = (Map<String, String>) result;
                    grid.resize(map.size(), 2);
                    int row = 0;
                    for (String key : map.keySet()) {
                        grid.setText(row, 0, key);
                        grid.setText(row, 1, map.get(key));
                        row++;
                    }
                }
            });

        }
    }

    class AddWidget extends Composite {

        AddWidget() {
            Panel panel = new VerticalPanel();
            panel.add(new AddArtistWidget());
            panel.add(new AddListenerWidget());
            panel.add(new AddApplicationWidget());
            initWidget(panel);
        }
    }
    class AddArtistWidget extends Composite {

        StatusWidget status;
        TextBox mbaidTextBox;

        AddArtistWidget() {
            status = new StatusWidget();
            Panel panel = new FlowPanel();
            panel.add(new Label("Add Artist"));
            mbaidTextBox = new TextBox();
            mbaidTextBox.setVisibleLength(50);
            mbaidTextBox.setTitle("Enter the musicbrainz ID for the artist to add");
            panel.add(mbaidTextBox);
            Button go = new Button("Add");

            mbaidTextBox.addChangeListener(new ChangeListener() {

                public void onChange(Widget arg0) {
                    go();
                }
            });

            go.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    go();
                }
            });

            panel.add(go);
            panel.add(status);
            panel.setStyleName("frame");
            initWidget(panel);
        }

        public void go() {
            String mbaid = mbaidTextBox.getText().trim();

            if (mbaid.length() < 32 || mbaid.length() > 64) {
                status.error("Enter a valid musicbrainz ID");
            } else {
                status.processing();
                service.addArtist(mbaid, new AsyncCallback() {

                    public void onFailure(Throwable t) {
                        if (t instanceof AdminException) {
                            AdminException e = (AdminException) t;
                            status.error(e.getDisplayMessage());
                        } else {
                            status.error(t.getMessage());
                        }
                    }

                    public void onSuccess(Object result) {
                        status.info("Artist added");
                    }
                });
            }
        }
    }

    class AddListenerWidget extends Composite {

        StatusWidget status;
        TextBox keyTextBox;

        AddListenerWidget() {
            status = new StatusWidget();
            Panel panel = new FlowPanel();
            panel.add(new Label("Add Listener"));
            keyTextBox = new TextBox();
            keyTextBox.setVisibleLength(50);
            keyTextBox.setTitle("Enter the user ID");
            panel.add(keyTextBox);
            Button go = new Button("Add");

            keyTextBox.addChangeListener(new ChangeListener() {

                public void onChange(Widget arg0) {
                    go();
                }
            });

            go.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    go();
                }
            });

            panel.add(go);
            panel.add(status);
            panel.setStyleName("frame");
            initWidget(panel);
        }

        public void go() {
            String key = keyTextBox.getText().trim();

            if (key.length() == 0) {
                status.error("Enter a valid listener ID");
            } else {
                status.processing();
                service.addListener(key, new AsyncCallback() {

                    public void onFailure(Throwable t) {
                        if (t instanceof AdminException) {
                            AdminException e = (AdminException) t;
                            status.error(e.getDisplayMessage());
                        } else {
                            status.error(t.getMessage());
                        }
                    }

                    public void onSuccess(Object result) {
                        status.info("Listener added");
                    }
                });
            }
        }
    }

    class AddApplicationWidget extends Composite {

        StatusWidget status;
        TextBox keyTextBox;

        AddApplicationWidget() {
            status = new StatusWidget();
            Panel panel = new FlowPanel();
            panel.add(new Label("Add Application Key"));
            keyTextBox = new TextBox();
            keyTextBox.setVisibleLength(50);
            keyTextBox.setTitle("Enter the application key");
            panel.add(keyTextBox);
            Button go = new Button("Add");

            keyTextBox.addChangeListener(new ChangeListener() {

                public void onChange(Widget arg0) {
                    go();
                }
            });

            go.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    go();
                }
            });

            panel.add(go);
            panel.add(status);
            panel.setStyleName("frame");
            initWidget(panel);
        }

        public void go() {
            String key = keyTextBox.getText().trim();

            if (key.length() == 0) {
                status.error("Enter a valid application key");
            } else {
                status.processing();
                service.addApplication(key, new AsyncCallback() {

                    public void onFailure(Throwable t) {
                        if (t instanceof AdminException) {
                            AdminException e = (AdminException) t;
                            status.error(e.getDisplayMessage());
                        } else {
                            status.error(t.getMessage());
                        }
                    }

                    public void onSuccess(Object result) {
                        status.info("Application added");
                    }
                });
            }
        }
    }


    private ColumnManager cm = new ColumnManager();

    class TestingWidget extends Composite implements ClickListener {

        private StatusWidget status;
        private List<SingleTestWidget> singleTestWidgets;
        private Button run;
        private Button reset;
        private VerticalPanel mainPanel = null;

        TestingWidget(boolean justShortTests) {
            status = new StatusWidget();
            DockPanel p = new DockPanel();
            p.add(new Label("Tests"), NORTH);

            mainPanel = new VerticalPanel();
            mainPanel.setHorizontalAlignment(ALIGN_CENTER);
            p.add(mainPanel, CENTER);


            HorizontalPanel footer = new HorizontalPanel();
            run = new Button("Run All");
            run.addClickListener(this);

            reset = new Button("Reset");
            reset.addClickListener(this);


            footer.add(run);
            footer.add(reset);
            footer.add(status);

            footer.setHorizontalAlignment(ALIGN_CENTER);
            p.add(footer, SOUTH);
            collectTests(justShortTests);
            initWidget(p);
        }

        void addTests(List<String> testNames) {
            singleTestWidgets = new ArrayList<SingleTestWidget>();

            for (String test : testNames) {
                singleTestWidgets.add(new SingleTestWidget(test));
            }

            mainPanel.clear();

            mainPanel.add(cm.createHeader());
            int row = 0;
            for (Widget w : singleTestWidgets) {
                mainPanel.add(w);
                if (++row % 2 == 1) {
                    w.addStyleName("oddRow");
                } else {
                    w.addStyleName("evenRow");
                }
            }
        }


        void collectTests(boolean justShortTests) {
            status.processing();
            service.getTests(justShortTests, new AsyncCallback() {

                public void onFailure(Throwable t) {
                    if (t instanceof AdminException) {
                        AdminException e = (AdminException) t;
                        status.error(e.getDisplayMessage());
                    } else {
                        status.error(t.getMessage());
                    }
                }

                public void onSuccess(Object result) {
                    status.clear();
                    List<String> tests = (List<String>) result;
                    addTests(tests);
                }
            });

        }

        public void onClick(Widget button) {
            if (button == run) {
                run();
            } else if (button == reset) {
                reset();
            }
        }

        public void disable() {
            for (SingleTestWidget w : singleTestWidgets) {
                w.disable();
            }
        }

        public void run() {
            for (SingleTestWidget w : singleTestWidgets) {
                w.run();
            }
        }

        public void reset() {
            for (SingleTestWidget w : singleTestWidgets) {
                w.reset();
            }
        }

        public void enable() {
            for (SingleTestWidget w : singleTestWidgets) {
                w.enable();
            }
        }
    }

    class SingleTestWidget extends Composite implements ClickListener {

        String name;
        Button b = new Button("Run");
        Grid g;
        Label pass = new Label("PASS");
        Label fail = new Label("FAIL");
        Label error = new Label("ERROR");
        Label running = new Label("Running");
        Label idle = new Label("");
        boolean isRunning = false;
        private int count;
        private int failCount;

        SingleTestWidget(String name) {
            this.name = name;
            g = cm.createData();

            g.setText(0, ColumnManager.COL_STATUS, "");
            g.setText(0, ColumnManager.COL_NAME, name);
            g.setWidget(0, ColumnManager.COL_BUTTON, b);
            g.setText(0, ColumnManager.COL_MESSAGE, "");
            g.setText(0, ColumnManager.COL_TIME, "");
            g.setText(0, ColumnManager.COL_COUNT, "");
            g.setText(0, ColumnManager.COL_FAIL_COUNT, "");

            pass.addStyleName("testPass");
            fail.addStyleName("testFail");
            error.addStyleName("testFail");
            running.addStyleName("testNeutral");
            idle.addStyleName("testNeutral");

            b.addClickListener(this);
            initWidget(g);
        }

        void setMessage(String message) {
            g.setText(0, ColumnManager.COL_MESSAGE, message);
        }

        void clear() {
            g.setWidget(0, ColumnManager.COL_STATUS, idle);
            setMessage("");
        }

        void pass() {
            g.setWidget(0, ColumnManager.COL_STATUS, pass);
            setMessage("");
        }

        void inProcess() {
            g.setWidget(0, ColumnManager.COL_STATUS, running);
            g.clearCell(0, ColumnManager.COL_TRACE);
            setMessage("");
        }

        void fail(String msg) {
            g.setWidget(0, ColumnManager.COL_STATUS, fail);
            setMessage(msg);
        }

        void error(String msg) {
            g.setWidget(0, ColumnManager.COL_STATUS, error);
            setMessage(msg);
        }

        void disable() {
            b.setEnabled(false);
        }

        void enable() {
            b.setEnabled(true);
        }

        void reset() {
            count = 0;
            failCount = 0;
            showCounts();
            clear();
        }

        void showCounts() {
            g.setText(0, ColumnManager.COL_COUNT, Integer.toString(count));
            g.setText(0, ColumnManager.COL_FAIL_COUNT, Integer.toString(failCount));
        }

        void showError(String msg) {
            error(msg);
            showCounts();
        }

        void showStatus(TestStatus ts) {
            if (ts.isPassed()) {
                pass();
            } else {
                fail(getFailureSummary(ts));
                if (ts.getStackTrace() != null) {
                    g.setWidget(0, ColumnManager.COL_TRACE, new TraceButton(ts.getStackTrace()));
                }
            }
            g.setText(0, ColumnManager.COL_TIME, Long.toString(ts.getTime()));
            showCounts();
        }

        String getFailureSummary(TestStatus ts) {
            if (ts.getMostRecentQuery() != null) {
                return ts.getFailReason() + " : " + ts.getMostRecentQuery();
            } else {
                return ts.getFailReason();
            }
        }

        void run() {
            if (!isRunning) {
                isRunning = true;
                count++;
                inProcess();
                disable();
                service.runTest(name, new AsyncCallback() {

                    public void onFailure(Throwable t) {
                        failCount++;
                        if (t instanceof AdminException) {
                            AdminException e = (AdminException) t;
                            showError(e.getDisplayMessage());
                        } else {
                            showError(t.getMessage());
                        }
                        enable();
                        isRunning = false;
                    }

                    public void onSuccess(Object result) {
                        TestStatus ts = (TestStatus) result;
                        if (!ts.isPassed()) {
                            failCount++;
                        }
                        showStatus(ts);
                        enable();
                        isRunning = false;
                    }
                });
            }
        }

        public void onClick(Widget arg0) {
            run();
        }
    }
}


class Column {
    String header;
    String width;
    String style;

    Column(String header, String width, String style) {
        this.header = header;
        this.width = width;
        this.style = style;
    }

    Column(String header, String width) {
        this(header, width, null);
    }

    public String getHeader() {
        return header;
    }

    public String getStyle() {
        return style;
    }

    public String getWidth() {
        return width;
    }
    
    
}
class ColumnManager {
    public final static int COL_STATUS = 0;
    public final static int COL_NAME = 1;
    public final static int COL_MESSAGE = 2;
    public final static int COL_TRACE = 3;
    public final static int COL_TIME = 4;
    public final static int COL_COUNT = 5;
    public final static int COL_FAIL_COUNT = 6;
    public final static int COL_BUTTON = 7;
    public final static int COL_MAX = 8;

    public final static String OVERALL_WIDTH = "900px";

    Column[] columns;
    
    ColumnManager() {
        columns = new Column[COL_MAX];
        columns[COL_STATUS]     = new Column("Status",   "100px",   "columnHeavy");
        columns[COL_NAME]       = new Column("Test",     "200px",    "columnNormal");
        columns[COL_MESSAGE]    = new Column("Message",  "250px",    "columnLight");
        columns[COL_TRACE]      = new Column("",  "60px",    "columnLight");
        columns[COL_BUTTON]     = new Column("",  "60px");
        columns[COL_TIME]       = new Column("Time (ms)","80px",     "columnLight");
        columns[COL_COUNT]      = new Column("Count",    "80px",     "columnLight");
        columns[COL_FAIL_COUNT] = new Column("Fails",    "80px",     "columnLight");
    }
    
    Grid createHeader() {
        Grid g = new Grid(1, COL_MAX);
        for (int i = 0; i < COL_MAX; i++) {
            g.setText(0, i, columns[i].getHeader());
        }
        formatHeader(g);
        g.setWidth(OVERALL_WIDTH);
        return g;
    }

    Grid createData() {
        Grid g = new Grid(1, COL_MAX);
        formatData(g);
        g.setWidth(OVERALL_WIDTH);
        return g;
    }

    void formatHeader(Grid headerRow) {
        headerRow.getRowFormatter().addStyleName(0, "columnHeaders");
        for (int i = 0; i < columns.length; i++) {
            headerRow.getColumnFormatter().setWidth(i, columns[i].getWidth());
        }
    }

    void formatData(Grid row) {
        for (int i = 0; i < columns.length; i++) {
            row.getColumnFormatter().setWidth(i, columns[i].getWidth());
            if (columns[i].getStyle() != null) {
                row.getCellFormatter().addStyleName(0, i, columns[i].getStyle());
            }
        }
    }
}

class TraceButton extends Composite implements ClickListener {
    private String stackTrace;
    TraceButton(String stackTrace) {
        this.stackTrace = stackTrace;
        Button b = new Button("Trace");
        b.setStyleName("traceButton");
        b.addClickListener(this);
        initWidget(b);
    }

    public void onClick(Widget arg0) {
        PopupPanel pp = new PopupPanel(true);
        pp.setStyleName("tracePopup");
        pp.setWidget(new Label(stackTrace));
        pp.setAnimationEnabled(true);
        pp.center();
    }
}
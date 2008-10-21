/*
 *  Copyright (c) 2008, Sun Microsystems Inc.
 *  See license.txt for license.
 */
package com.sun.labs.aura.music.admin.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.TreeListener;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author plamere
 */
public class WorkbenchPanel extends Composite implements TreeListener {

    List<WorkbenchDescriptor> workers;
    AdminServiceAsync service;
    DockPanel dp = new DockPanel();
    Tree workerMenu = new Tree();
    StatusWidget status = new StatusWidget();
    Widget curWorkerPanel = null;
    Button goButton;
    Map<String, Widget> widgetCache = new HashMap<String, Widget>();

    WorkbenchPanel(AdminServiceAsync service) {
        this.service = service;
        dp.add(workerMenu, DockPanel.WEST);
        dp.add(status, DockPanel.SOUTH);


        workerMenu.addTreeListener(this);
        initWidget(dp);

        updateWorkbench();
    }

    private void addWorkers() {
        workerMenu.clear();
        TreeItem main = new TreeItem("main");
        for (WorkbenchDescriptor worker : workers) {
            TreeItem item = new TreeItem(worker.getName());
            item.setTitle(worker.getDescription());
            item.setUserObject(worker);
            item.addStyleName("workerTreeItem");
            main.addItem(item);
        }
        main.setState(true);
        workerMenu.addItem(main);
    }

    private void updateWorkbench() {
        status.processing();
        service.getWorkerDescriptions(new AsyncCallback() {

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
                workers = (List<WorkbenchDescriptor>) result;
                addWorkers();
            }
        });
    }

    public void onTreeItemSelected(TreeItem treeItem) {
        WorkbenchDescriptor worker = (WorkbenchDescriptor) treeItem.getUserObject();
        if (worker != null) {
            Widget w = getWorkerWidget(worker);
            if (curWorkerPanel != null) {
                dp.remove(curWorkerPanel);
                curWorkerPanel = null;
            }
            dp.add(w, DockPanel.CENTER);
            curWorkerPanel = w;
        }
    }

    Widget getWorkerWidget(WorkbenchDescriptor worker) {
        Widget widget = widgetCache.get(worker.getName());
        if (widget == null) {
            widget = new WorkerWidget(worker);
            widgetCache.put(worker.getName(), widget);
        }
        return widget;
    }

    public void onTreeItemStateChanged(TreeItem arg0) {
    }

    class WorkerWidget extends Composite {

        private WorkbenchDescriptor worker;
        private StatusWidget status;
        private TextArea output;
        private boolean isRunning = false;
        private int failCount;
        private Grid grid;

        WorkerWidget(WorkbenchDescriptor worker) {
            this.worker = worker;
            VerticalPanel panel = new VerticalPanel();
            panel.setHorizontalAlignment(VerticalPanel.ALIGN_CENTER);
            Label title = new Label(worker.getName());
            title.addStyleName("title");
            panel.add(title);

            grid = new Grid((worker.getParameters().size() + 1) / 2, 4);
            int item = 0;
            for (ParamDescriptor p : worker.getParameters()) {

                int row = item / 2;
                int col = (item % 2) * 2;
                Label prompt = new Label(p.getName());
                prompt.setStyleName("prompt");
                grid.setWidget(row, col, prompt);
                Widget w;
                if (p.getType() == ParamDescriptor.Type.TypeEnum) {
                    w = getListBoxForParam(p);
                } else {
                    TextBox box = new TextBox();
                    box.setText(p.getDefaultValue());
                    box.setTitle(p.getDescription());
                    w = box;
                }
                grid.setWidget(row, col + 1, w);
                item++;
            }
            panel.add(grid);
            status = new StatusWidget();
            panel.add(status);
            goButton = new Button(worker.getName());
            goButton.addClickListener(new ClickListener() {

                public void onClick(Widget arg0) {
                    run();
                }
            });
            goButton.setTitle(worker.getDescription());
            panel.add(goButton);

            output = new TextArea();
            output.addStyleName("output");
            output.setCharacterWidth(100);
            output.setVisibleLines(40);
            output.setReadOnly(true);
            panel.add(output);
            panel.addStyleName("workerWidget");
            initWidget(panel);
        }


        private Widget getListBoxForParam(ParamDescriptor p) {
            ListBox listBox = new ListBox();
            int defaultIndex = 0;
            int count = 0;
            for (String e : p.getEnumValues()) {
                if (e.equals(p.getDefaultValue())) {
                    defaultIndex = count;
                }
                listBox.addItem(e);
                count++;
            }
            listBox.setVisibleItemCount(1);
            listBox.setSelectedIndex(defaultIndex);
            listBox.setTitle(p.getDescription());
            return listBox;
        }

        private Map<String, String> gatherParams() {
            Map<String, String> p = new HashMap<String, String>();

            for (int i = 0; i < grid.getRowCount(); i++) {
                {
                    Label l = (Label) grid.getWidget(i, 0);
                    if (l != null) {
                        p.put(l.getText(), getText(grid.getWidget(i, 1)));
                    }

                }
                {
                    Label l = (Label) grid.getWidget(i, 2);
                    if (l != null) {
                        p.put(l.getText(), getText(grid.getWidget(i, 3)));
                    }
                }
            }
            return p;
        }


        private String getText(Widget w) {
            if (w instanceof TextBox) {
                TextBox tb = (TextBox) w;
                return tb.getText();
            } else if (w instanceof ListBox) {
                ListBox lb = (ListBox) w;
                return lb.getItemText(lb.getSelectedIndex());
            } else {
                return "";
            }
        }

        void processing(boolean state) {
            goButton.setEnabled(!state);
            if (state) {
                output.setText("");
            } else {
                status.clear();
            }
        }

        void run() {
            if (!isRunning) {
                isRunning = true;
                status.info("Processing");
                processing(true);
                Map<String, String> params = gatherParams();

                service.runWorker(worker.getName(), params, new AsyncCallback() {

                    public void onFailure(Throwable t) {
                        processing(false);
                        failCount++;
                        if (t instanceof AdminException) {
                            AdminException e = (AdminException) t;
                            status.error(e.getDisplayMessage());
                        } else {
                            status.error(t.getMessage());
                        }
                        isRunning = false;
                    }

                    public void onSuccess(Object result) {
                        processing(false);
                        WorkbenchResult wbr = (WorkbenchResult) result;
                        if (!wbr.isOk()) {
                            failCount++;
                            status.error(wbr.getFailReason());
                        } else {
                            status.info("OK");

                            StringBuilder sb = new StringBuilder();
                            for (String msg : wbr.getOutput()) {
                                sb.append(msg);
                                sb.append("\n");
                            }
                            output.setText(sb.toString());
                        }
                        isRunning = false;
                    }
                });
            }
        }
    }
}


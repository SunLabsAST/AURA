/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.client.shell;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.KeyboardListener;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.dbbrowser.client.GWTMainEntryPoint;

/**
 * An interface for running the AuraShell remotely
 */
public class ShellUI extends DockPanel {
    private ShellServiceAsync service = null;

    private final TextBox cmdInput = new TextBox();
    private TextArea resultArea = new TextArea();
    private VerticalPanel history = new VerticalPanel();
    private Label time = new Label();
    private ScriptDialog scriptDialog;

    public ShellUI() {
        service = GWTMainEntryPoint.getShellService();

        HorizontalPanel inputArea = new HorizontalPanel();
        inputArea.setStylePrimaryName("shell-input");
        final Button runBtn = new Button("Run", new ClickListener() {
            public void onClick(Widget sender) {
                //
                // Get the command from the text box
                String cmd = cmdInput.getText();

                addToHistory(cmd);
                sendCommand(cmd);
            }
        });
        runBtn.setStylePrimaryName("shell-inputButton");
        cmdInput.addKeyboardListener(new KeyboardListener() {
            public void onKeyDown(Widget arg0, char arg1, int arg2) {}
            
            public void onKeyPress(Widget w, char c, int mod) {
                if (c == KEY_ENTER)  {
                    runBtn.click();
                }
            }

            public void onKeyUp(Widget arg0, char arg1, int arg2) {}

        });
        Button clearBtn = new Button("Clear", new ClickListener() {
            public void onClick(Widget sender) {
                //
                // Get the command from the text box
                cmdInput.setText("");
            }

        });
        clearBtn.setStylePrimaryName("shell-inputButton");
        Button scriptBtn = new Button("Script...", new ClickListener() {
            public void onClick(Widget sender) {
                scriptDialog.show();
            }
        });
        scriptBtn.setStylePrimaryName("shell-inputButton");
        scriptDialog = new ScriptDialog();

        cmdInput.setVisibleLength(80);
        inputArea.add(cmdInput);
        inputArea.add(runBtn);
        inputArea.add(clearBtn);
        inputArea.add(scriptBtn);
        add(inputArea, NORTH);

        resultArea.setCharacterWidth(120);
        resultArea.setVisibleLines(40);
        resultArea.setReadOnly(true);
        add(resultArea, CENTER);

        history.setStylePrimaryName("shell-historyPanel");
        add(history, EAST);

        add(time, SOUTH);
    }

    protected void sendCommand(String cmd) {
        //
        // Send the command to the server
        final AsyncCallback callback = new AsyncCallback() {
            public void onFailure(Throwable t) {
                alert("Failed execute command, see the server log for details");
            }

            public void onSuccess(Object result) {
                showResult((ShellResult)result);
            }
        };
        service.runCommand(cmd, scriptDialog.getScript(), callback);

    }

    protected void showResult(ShellResult result) {
        resultArea.setText(result.getText());
        time.setText("Took " + result.getTime() + "ms");
    }

    protected void addToHistory(String cmd) {
        //
        // Make a link that runs the command and add it to the top of the
        // panel
        Label cmdLabel = new Label(cmd);
        cmdLabel.setStylePrimaryName("shell-cmdLabel");
        cmdLabel.addClickListener(new ClickListener() {
            public void onClick(Widget sender) {
                Label clicked = (Label)sender;
                String text = clicked.getText();
                cmdInput.setText(text);
                sendCommand(text);
            }
        });
        history.insert(cmdLabel, 0);
    }


    protected static void alert(String msg) {
        Window.alert(msg);
    }

}

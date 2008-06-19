/*
 * TagExplorer.java
 *
 * Created on April 9, 2007, 12:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author plamere
 */
public class TagExplorer extends Swidget {
    private MusicSearchInterfaceAsync  musicServer;
    private DockPanel mainPanel;
    private Widget curResult;
    
    /** Creates a new instance of TagExplorer */
    public TagExplorer() {
        super("Tag Explorer");
        initRPC();
        mainPanel = new DockPanel();
        initWidget(mainPanel);
        invokeGetTree();
    }

    public List<String> getTokenHeaders() {

        List<String> l = new ArrayList<String>();
        l.add("tagexplore:");
        return l;
    }

    private void setResults(String historyName, Widget result) {
        if (curResult == result) {
            return;
        }
        
        if (!History.getToken().equals(historyName)) {
            History.newItem(historyName);
        }
        if (curResult != null) {
            mainPanel.remove(curResult);
            curResult = null;
        }
        if (result != null) {
            mainPanel.add(result, DockPanel.CENTER);
            curResult = result;
        }
    }
  
    private void setTree(TagTree ttree) {
        TreeItem treeItem = getTree(ttree);
        Tree tree = new Tree();
        tree.addItem(treeItem);
        setResults("tree", tree);
    }
    
    private TreeItem getTree(TagTree ttree) {
        TreeItem treeItem = new TreeItem(ttree.getName());
        TagTree[] children = ttree.getChildren();
        for (int i = 0; i < children.length; i++) {
            treeItem.addItem(getTree(children[i]));
        }
        return treeItem;
    }
    
     private void initRPC() {
        // (1) Create the client proxy. Note that although you are creating the
        // service interface proper, you cast the result to the async version of
        // the interface. The cast is always safe because the generated proxy
        // implements the async interface automatically.
        //
        musicServer = (MusicSearchInterfaceAsync) GWT
                .create(MusicSearchInterface.class);
        
        // (2) Specify the URL at which our service implementation is running.
        // Note that the target URL must reside on the same domain and port from
        // which the host page was served.
        //
        ServiceDefTarget endpoint = (ServiceDefTarget) musicServer;
        String moduleRelativeURL = GWT.getModuleBaseURL() + "musicsearch";
        endpoint.setServiceEntryPoint(moduleRelativeURL);
    }
     
    private void invokeGetTree() {
        
        AsyncCallback callback = new AsyncCallback() {
            public void onSuccess(Object result) {
                // do some UI stuff to show success
                TagTree tree = (TagTree) result;
                if (tree != null) {
                    setTree(tree);
                } else {
                    Window.alert("Whoops"); // fix this
                }
            }
            
            public void onFailure(Throwable caught) {
                Window.alert("Failure!");
            }
        };
        
        musicServer.getTagTree(callback);
    }
}

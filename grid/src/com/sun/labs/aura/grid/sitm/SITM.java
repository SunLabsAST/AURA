/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.grid.sitm;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.rmi.RemoteException;

/**
 * A base class for starting and stopping the SITM services.
 */
public abstract class SITM extends ServiceAdapter {

    public String getArtistCrawlerName() {
        return "artistCrawler";
    }

    public String getListenerCrawlerName() {
        return "listenerCrawler";
    }

    public String getTagCrawlerName() {
        return "tagCrawler";
    }

    public ProcessConfiguration getArtistCrawlerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/artistCrawlerConfig.xml",
            "starter"
        };

        return gu.getProcessConfig(cmdLine, getArtistCrawlerName());
    }

    public ProcessConfiguration getListenerCrawlerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/listenerCrawlerConfig.xml",
            "starter"
        };

        return gu.getProcessConfig(cmdLine, getListenerCrawlerName());
    }

    public ProcessConfiguration getTagCrawlerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/music/resource/tagCrawlerConfig.xml",
            "starter"
        };

        return gu.getProcessConfig(cmdLine, getTagCrawlerName());
    }

    @Override
    public String serviceName() {
        return "SITM";
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
    }
}

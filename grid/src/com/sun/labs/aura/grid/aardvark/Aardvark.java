package com.sun.labs.aura.grid.aardvark;

import com.sun.caroline.platform.FileSystem;
import com.sun.caroline.platform.FileSystemMountParameters;
import com.sun.caroline.platform.Network;
import com.sun.caroline.platform.ProcessConfiguration;
import com.sun.caroline.platform.ProcessExitAction;
import com.sun.caroline.platform.ProcessRegistrationFilter;
import com.sun.caroline.platform.StorageManagementException;
import com.sun.labs.aura.grid.util.GridUtil;
import com.sun.labs.aura.grid.ServiceAdapter;
import com.sun.labs.util.props.ConfigInteger;
import com.sun.labs.util.props.ConfigurationManager;
import com.sun.labs.util.props.PropertyException;
import com.sun.labs.util.props.PropertySheet;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * A base class for aardvark operations on the grid.
 */
public abstract class Aardvark extends ServiceAdapter {

    protected ConfigurationManager cm;

    /**
     * The number of crawler processes to start.
     */
    @ConfigInteger(defaultValue = 6)
    public static final String PROP_NUM_CRAWLERS = "numCrawlers";

    protected int numCrawlers;

    public String getFMName(int n) {
        return "feedMgr-" + n;
    }

    public String getRecName() {
        return "recommender";
    }

    public String getSchedName() {
        return "feedSched";
    }

    public String getAAName() {
        return "aardvark";
    }

    protected ProcessConfiguration getFeedSchedulerConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-Xmx2g",
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-DcacheDir=" + GridUtil.cacheFSMntPnt,
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/feedSchedulerConfig.xml",
            "feedSchedulerStarter"
        };

        // create a configuration and set relevant properties
        return gu.getProcessConfig(cmdLine, getSchedName());
    }

    protected ProcessConfiguration getFeedManagerConfig(int n)
            throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/feedManagerConfig.xml",
            "feedManagerStarter"
        };

        // create a configuration and set relevant properties
        ProcessConfiguration pc = gu.getProcessConfig(cmdLine, getFMName(n));

        // don't overlap with other replicants
        pc.setLocationConstraint(
                new ProcessRegistrationFilter.NameMatch(
                Pattern.compile(instance + ".*-feedMgr-.*")));

        return pc;
    }

    protected ProcessConfiguration getRecommenderConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/recommenderManagerConfig.xml",
            "recommenderManagerStarter"
        };

        return gu.getProcessConfig(cmdLine, getRecName());
    }

    protected ProcessConfiguration getAardvarkConfig() throws Exception {
        String[] cmdLine = new String[]{
            "-DauraHome=" + GridUtil.auraDistMntPnt,
            "-DauraGroup=" + instance + "-aura",
            "-jar",
            GridUtil.auraDistMntPnt + "/dist/grid.jar",
            "/com/sun/labs/aura/aardvark/resource/aardvarkConfig.xml",
            "aardvarkStarter"
        };

        // create a configuration and set relevant properties
        return gu.getProcessConfig(cmdLine, getAAName());
    }

    public void newProperties(PropertySheet ps) throws PropertyException {
        super.newProperties(ps);
        cm = ps.getConfigurationManager();
        numCrawlers = ps.getInt(PROP_NUM_CRAWLERS);
    }
}

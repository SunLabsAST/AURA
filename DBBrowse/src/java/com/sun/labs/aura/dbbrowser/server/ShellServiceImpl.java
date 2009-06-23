/*
 * ShellServiceImpl.java
 *
 * Created on June 18, 2009, 1:52 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.sun.labs.aura.dbbrowser.server;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.sun.labs.aura.datastore.DataStore;
import com.sun.labs.aura.dbbrowser.client.shell.ShellResult;
import com.sun.labs.aura.dbbrowser.client.shell.ShellService;
import com.sun.labs.aura.service.StatService;
import com.sun.labs.aura.util.AuraException;
import com.sun.labs.aura.util.RemoteComponentManager;
import com.sun.labs.aura.util.RemoteMultiComponentManager;
import com.sun.labs.aura.util.ShellUtils;
import com.sun.labs.minion.util.StopWatch;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 *
 * @author ja151348
 */
public class ShellServiceImpl extends RemoteServiceServlet implements
        ShellService {
    protected CommandInterpreter shell = null;
    protected RemoteMultiComponentManager dsrcm = null;
    protected RemoteComponentManager statrcm = null;
    protected static Logger logger = Logger.getLogger("");

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        ServletContext context = getServletContext();
        ConfigurationManager cm = (ConfigurationManager)context.getAttribute("configManager");
        dsrcm = new RemoteMultiComponentManager(cm, DataStore.class);
        statrcm = new RemoteComponentManager(cm, StatService.class);

        shell = new CommandInterpreter();
        try {
            ShellUtils sutils = new ShellUtils(shell, getStore(), getStatService());
        } catch (AuraException e) {
            logger.log(Level.INFO, "Failed to set up shell", e);
        }
    }

    protected DataStore getStore() throws AuraException {
        return (DataStore)dsrcm.getComponent();
    }

    protected StatService getStatService() throws AuraException {
        return (StatService)statrcm.getComponent();
    }

    public ShellResult runCommand(String cmd, String script) {
        //
        // Save the script in case we need it
        File scriptFile = new File("/tmp/script.js");
        if (scriptFile.exists()) {
            scriptFile.delete();
        }
        try {
            scriptFile.createNewFile();
            PrintWriter pw = new PrintWriter(scriptFile);
            pw.println(script);
            pw.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error writing script file", e);
        }

        //
        // Redirect output
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        shell.setOutput(out);

        StopWatch sw = new StopWatch();

        //
        // Run
        sw.start();
        String ret = shell.execute(cmd);
        sw.stop();

        //
        // Return the result
        String result = baos.toString();
        result = result + ret;
        return new ShellResult(sw.getTime(), result);
    }
}

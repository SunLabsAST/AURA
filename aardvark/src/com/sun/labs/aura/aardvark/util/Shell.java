/*
 *  Copyright Expression year is undefined on line 4, column 30 in Templates/Licenses/license-default.txt. Sun Microsystems, Inc. 
 *  All Rights Reserved. Use is subject to license terms.
 * 
 *  See the file "license.terms" for information on usage and
 *  redistribution of this file, and for a DISCLAIMER OF ALL
 *  WARRANTIES. 
 */
package com.sun.labs.aura.aardvark.util;

import com.sun.labs.aura.aardvark.Aardvark;
import com.sun.labs.aura.aardvark.store.ItemStore;
import com.sun.labs.util.command.CommandInterface;
import com.sun.labs.util.command.CommandInterpreter;
import com.sun.labs.util.props.ConfigurationManager;
import java.io.IOException;
import java.net.URL;

/**
 *
 * @author plamere
 */
public class Shell {

    private CommandInterpreter shell;

    public Shell(String config) throws IOException {
        shell = new CommandInterpreter();
        ConfigurationManager cm = new ConfigurationManager();
        cm.addProperties(new URL(config));

        shell.add("users", new CommandInterface() {

                    public String execute(CommandInterpreter ci, String[] arg1) {
                        return "";
                    }

                    public String getHelp() {
                        return "shows the current users";
                    }
                });
    }

    public static void main(String[] args) {
    }
}

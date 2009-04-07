/*
 * Copyright 2007-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This code is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2
 * only, as published by the Free Software Foundation.
 * 
 * This code is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included in the LICENSE file that accompanied this code).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 16 Network Circle, Menlo
 * Park, CA 94025 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.labs.aura.music.test;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author plamere
 */
public class MailManager {
    public void sendMessage(String recipient, String subject, String message) {
        try {
            Runtime rt = Runtime.getRuntime();
            String[] args = {"mailx", "-s", subject, recipient};
            Process process = rt.exec(args);
            OutputStream os = process.getOutputStream();
            BufferedOutputStream out = new BufferedOutputStream(os);
            PrintWriter pw = new PrintWriter(out);
            pw.println(message);
            pw.flush();
            pw.close();
        } catch (IOException ex) {
            Logger.getLogger(MailManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        MailManager mm = new MailManager();

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < 100; i++) {
            sb.append("This is string " + i + "\n");
        }

        mm.sendMessage("Paul.Lamere@sun.com", "test email", sb.toString());
    }
}

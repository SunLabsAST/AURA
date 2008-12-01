/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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

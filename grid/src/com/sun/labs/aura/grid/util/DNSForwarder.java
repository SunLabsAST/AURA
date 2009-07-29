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
package com.sun.labs.aura.grid.util;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Forwards a UDP port on one host to another and relays the results back.
 */
public class DNSForwarder  implements Runnable {
    protected static final int FORWARD_PORT = 53;

    protected InetAddress remote = null;
    protected DatagramSocket socket = null;
    protected AtomicInteger port = new AtomicInteger(5000);

    public DNSForwarder(InetAddress remoteHost) throws Exception {
        remote = remoteHost;
        socket = new DatagramSocket(FORWARD_PORT);
    }

    public void run() {
        try {
            while (true) {
                byte[] data = new byte[512];
                DatagramPacket packet = new DatagramPacket(data, data.length);
                socket.receive(packet);
                int nextPort = port.getAndIncrement();
                if (nextPort >= 5100) {
                    port.set(5000);
                }
                PacketForwarder f = new PacketForwarder(packet, nextPort);
                Thread t = new Thread(f);
                t.start();
            }
        } catch (Exception e) {
            System.out.println("Failed to run forwarder: " + e.getMessage());
            e.printStackTrace();
        }
        socket.close();
    }

    class PacketForwarder implements Runnable {
        protected DatagramPacket packet;
        protected SocketAddress source;
        protected int listenPort;
        public PacketForwarder(DatagramPacket packet, int port) {
            this.packet = packet;
            source = packet.getSocketAddress();
            listenPort = port;
        }

        public void run() {
            try {
                //
                // Write the datagram to the remote host
                packet.setSocketAddress(new InetSocketAddress(remote, FORWARD_PORT));
                DatagramSocket remoteSocket = new DatagramSocket(listenPort);
                remoteSocket.send(packet);

                //
                // Wait for a response
                byte[] resData = new byte[512];
                DatagramPacket result = new DatagramPacket(resData, resData.length);
                remoteSocket.receive(result);

                //
                // Send the response back to the original requestor
                result.setSocketAddress(source);
                socket.send(result);
                remoteSocket.close();
            } catch (Exception e) {
                System.out.println("UDP Forwarder failed: " + e.getMessage());
                e.printStackTrace();
            }
        }

    }

    public static void usage() {
        System.out.println("Usage: DNSForwarder <host address>");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
            return;
        }

        String remoteHostStr = args[0];
        try {
            InetAddress remoteHost = InetAddress.getByName(remoteHostStr);

            //
            // Make a thing for forwarding the UDP data
            DNSForwarder udp = new DNSForwarder(remoteHost);
            Thread forwarder = new Thread(udp);
            forwarder.start();
            forwarder.join();
        } catch (Exception e) {
            System.out.println("Failed to set up in main: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

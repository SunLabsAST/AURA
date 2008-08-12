/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.util.io;

import java.io.IOException;

/**
 *
 * @author Will Holcomb <william.holcomb@sun.com>
 */
public interface RecordInputStream<K, V> {
    public Record<K, V> read() throws IOException;
}

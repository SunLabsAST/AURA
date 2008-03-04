
package com.sun.labs.aura.dbbrowser.client;
import com.google.gwt.user.client.rpc.RemoteService;
import com.sun.labs.aura.dbbrowser.client.ItemDesc;

/**
 *
 */
public interface DBService extends RemoteService {
    public ItemDesc[] searchItemByKey(String key);

    public ItemDesc[] searchItemByName(String key);
    
    public AttnDesc[] getAttentionForSource(String key);
    
    public AttnDesc[] getAttentionForTarget(String key);
}

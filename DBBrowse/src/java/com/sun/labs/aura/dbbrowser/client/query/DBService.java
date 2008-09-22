
package com.sun.labs.aura.dbbrowser.client.query;
import com.google.gwt.user.client.rpc.RemoteService;
import java.util.HashMap;

/**
 *
 */
public interface DBService extends RemoteService {
    public ItemDesc[] searchItemByKey(String key);

    public ItemDesc[] searchItemByName(String key);
    
    public ItemDesc[] searchItemByGen(String query);
    
    public ItemDesc[] findSimilar(String key);
    
    public AttnDesc[] getAttentionForSource(String key);
    
    public AttnDesc[] getAttentionForTarget(String key);

    /**
     * @gwt.typeArgs <java.lang.String,java.lang.String>
     */
    public HashMap getItemInfo(String key);
    
    public AttnDesc[] doTest();
}

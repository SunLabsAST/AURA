
package com.sun.labs.aura.music.web.lastfm;

/**
 * Represents a social tag
 */
public class SocialTag extends Item  {
    
    /**
     * Creates a tag
     * @param name the name of the tag
     * 
     * @param freq the frequency (as a percent)
     */
    public SocialTag(String name, int freq) {
        super(name, freq);
    }
}

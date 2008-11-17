/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.items;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mailletf
 */
public class AttentionItem<T extends IsSerializable> implements IsSerializable {

    private T item;
    private int rating;
    private Set<String> tags;

    public AttentionItem() {

    }

    public AttentionItem(T item) {
        this.item = item;
        this.rating = 0;
        this.tags = new HashSet<String>();
    }

    public void setItem(T item) {
        this.item = item;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public int getRating() {
        return rating;
    }

    public Set<String> getTags() {
        return tags;
    }

    public T getItem() {
        return item;
    }


}

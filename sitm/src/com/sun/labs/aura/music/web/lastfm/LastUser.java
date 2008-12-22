/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.web.lastfm;

/**
 *
 * @author plamere
 */
public class LastUser {
    enum Gender { Unknown, Male, Female};
    private String name = "";
    private String realName = "";
    private int age = 0;
    private int playCount = 0;
    private Gender gender = Gender.Unknown;
    private String country = "";

    public int getAge() {
        return age;
    }

    public String getCountry() {
        return country;
    }

    public Gender getGender() {
        return gender;
    }

    public String getName() {
        return name;
    }

    public int getPlayCount() {
        return playCount;
    }

    public String getRealName() {
        return realName;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String toString() {
        return String.format("%2d %6d %8s %20s %15s %s",
                age, playCount, gender.name(), country, name, realName);
    }
}

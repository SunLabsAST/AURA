/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.story;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 *
 * @author plamere
 */
public class SimulatedStoryManager implements StoryManager {
    private List<Story> stories;
    private boolean similateScores = true;
    private Random rng = new Random();

    public SimulatedStoryManager(String path) throws IOException {
        stories = loadStories(path);
    }

    private void randomizeScores(List<Story> stories) {
        for (Story story : stories) {
            story.setScore(getRandomScore());
        }
    }

    private List<Story> loadStories(String path) throws IOException {
        FileInputStream stream = new FileInputStream(path);
        List<Story> retStories = Util.loadStories(stream);
        stream.close();
        if (similateScores) {
            randomizeScores(retStories);
        }
        return retStories;
    }

    public List<Story> getNextStories(int count) {
        Collections.shuffle(stories);

        if (count > stories.size()) {
            count = stories.size();
        }
        return stories.subList(0, count);
    }

    
    private float getRandomScore() {
        return rng.nextFloat();
    }

    public void thumbsUp(Story story) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void thumbsDown(Story story) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public List<Story> findSimilar(Story story, int count) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

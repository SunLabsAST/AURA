/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

/**
 *
 * @author plamere
 */
import com.sun.labs.aura.aardvark.dashboard.*;
import com.jme.app.AbstractGame;
import com.jme.app.SimpleGame;
import com.jme.input.FirstPersonHandler;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.light.PointLight;
import com.jme.light.SpotLight;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.SceneElement;
import com.jme.scene.Text;
import com.jme.scene.state.FogState;
import com.jme.scene.state.TextureState;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.effects.Font3DGradient;
import java.awt.Font;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class shows the most simple physics with graphical representation: A dynamic geo falling onto a static floor
 * (also a geo).
 *
 * @author Irrisor
 */
public class Dashboard extends SimpleGame {

    private final static String VERSION = "Aardavark Dashboard V0.1";
    private StoryManager storyManager;
    private StoryPointFactory storyPointFactory;
    private Font3D font;
    private KeyboardHandler keyboardHandler;
    private boolean fixedFrameRate = true;
    private boolean simulate = false;
    private String baseUrl = "http://localhost:8080/DashboardWebServices/";

    public static void main(String[] args) {
        try {
            boolean simulate = false;
            boolean promptForResolution = false;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--simulate")) {
                    simulate = true;
                } else if (arg.equals("--setres")) {
                    promptForResolution = true;
                } else {
                    System.out.println("Unknown param " + arg);
                    System.out.println("Usage: dashboard [--simulate] [--setres]");
                    System.exit(1);
                }
            }

            Dashboard dashboard = new Dashboard(simulate, promptForResolution);
            dashboard.start();
        } catch (IOException ex) {
            System.err.println("Problem loading simulator " + ex);
        }
    }

    public Dashboard(boolean simulate, boolean promptForResolution) throws IOException {
        this.simulate = simulate;
        if (promptForResolution) {
            setDialogBehaviour(AbstractGame.ALWAYS_SHOW_PROPS_DIALOG);
        } else {
            setDialogBehaviour(AbstractGame.FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        }
    }

    protected void simpleInitGame() {
        initLogger();
        initStaticNodes();
        initLighting();
        initFog();
        initInput();
        initCamera();
        initFonts();
        initStoryQueue();
    }

    private void initInput() {
        // adjust the speed of the input handler
        FirstPersonHandler fph = new FirstPersonHandler(cam, 10, 2);
        fph.getMouseLookHandler().getMouseLook().setSpeed(.2f);
        input = fph;

        keyboardHandler = new KeyboardHandler();

        keyboardHandler.addKeyHandler(KeyInput.KEY_F, "findSimilar", new KeyActionHandler() {
            public void onKey(String opName) {
                if (storyPointFactory.hasAllSimStories()) {
                    storyPointFactory.clearSimStories();
                } else {
                    StoryPoint sp = storyPointFactory.getCurrentStoryPoint();
                    if (sp != null) {
                        storyManager.findSimilar(sp.getStory(), storyPointFactory.getNumSimStories());
                    }
                }
            }
        });

        input.addAction(new MousePick(cam, rootNode));
        addCrossHairs();
    }

    private void initCamera() {
        Vector3f loc = cam.getLocation();
        loc = loc.add(new Vector3f(0, 0, 25));
        cam.setLocation(loc);
    }

    private void initLogger() {
        Logger.getLogger("com.jme").setLevel(Level.WARNING);
        Logger.getLogger("com.jmex").setLevel(Level.WARNING);

    }

    private void initFonts() {
        font = new Font3D(new Font("Arial", Font.PLAIN, 1), 0.1, true, true, true);
        ColorRGBA color = ColorRGBA.randomColor();
        Font3DGradient gradient = new Font3DGradient(Vector3f.UNIT_Y, color, color);
        gradient.applyEffect(font);
    }

    private void initStaticNodes() {
        rootNode.setRenderQueueMode(Renderer.QUEUE_OPAQUE);
    }

    private void initLighting() {
        SpotLight sp1 = new SpotLight();
        sp1.setDiffuse(new ColorRGBA(0.0f, 1.0f, 0.0f, 1.0f));
        sp1.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        sp1.setDirection(new Vector3f(-1, -0.5f, 0));
        sp1.setLocation(new Vector3f(25, 10, 0));
        sp1.setAngle(15);
        sp1.setEnabled(true);

        //SpotLight sp2 = new SpotLight();
        //sp2.setDirection(new Vector3f(1, -0.5f, 0));
        //sp2.setAngle(15);
        PointLight sp2 = new PointLight();
        sp2.setDiffuse(new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
        sp2.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        sp2.setLocation(new Vector3f(-25, 10, 0));
        sp2.setEnabled(true);

        DirectionalLight dr = new DirectionalLight();
        dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        dr.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        dr.setSpecular(new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
        dr.setDirection(new Vector3f(150, 0, 150));
        dr.setEnabled(true);

    /*
    lightState.detachAll();
    lightState.attach(sp1);
    lightState.attach(dr);
    lightState.attach(sp2);
     */
    }

    private void addCrossHairs() {
        Text cross = Text.createDefaultTextLabel("Cross hairs", "+");
        cross.setCullMode(SceneElement.CULL_NEVER);
        cross.setTextureCombineMode(TextureState.REPLACE);
        cross.setLocalTranslation(new Vector3f(
                display.getWidth() / 2f - 8f, // 8 is half the width
                // of a font char
                display.getHeight() / 2f - 8f, 0));

        fpsNode.attachChild(cross);
    }

    private void initStoryQueue() {
        storyPointFactory = new StoryPointFactory(display, lightState, rootNode);
        storyManager = new StoryManager(storyPointFactory, baseUrl, simulate);
        storyManager.setAsyncMode(true);
        //storyManager.setLiveMode(true);
        storyManager.setMaxStoriesPerMinute(300);
        storyManager.start();
    }

    private void initFog() {
        FogState fogState = display.getRenderer().createFogState();
        fogState.setDensity(1f);
        fogState.setEnabled(true);
        fogState.setColor(new ColorRGBA(0.5f, 0.5f, 0.5f, .5f));
        fogState.setEnd(10);
        fogState.setStart(5);
        fogState.setDensityFunction(FogState.DF_LINEAR);
        fogState.setApplyFunction(FogState.AF_PER_VERTEX);
        rootNode.setRenderState(fogState);
    }

    @Override
    protected void simpleUpdate() {
        keyboardHandler.update();
        checkForNewStories();
        syncFrames();
    }

    long last;
    int frameRate = 60;
    int milliPerFrame = 1000 / 60;

    private void syncFrames() {
        if (fixedFrameRate) {
            long now = System.currentTimeMillis();
            long delta = now - last;
            long delay = milliPerFrame - delta;
            if (delay < 0) {
                if (delay < -10) {
                    System.out.println("late by " + (-delay) + " ms");
                }
            } else {
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                }
            }
            last = now;
        }

    }
    int max = Integer.MAX_VALUE;
    int cur = 0;

    private void checkForNewStories() {
        CPoint cp = storyManager.getNext();
        if (cp != null) {
            cur++;
            rootNode.attachChild(cp.getNode());
        }
    }
}


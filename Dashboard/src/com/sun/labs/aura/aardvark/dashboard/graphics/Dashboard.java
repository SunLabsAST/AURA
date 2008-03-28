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
import com.jme.input.FirstPersonHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.light.PointLight;
import com.jme.light.SpotLight;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.SceneElement;
import com.jme.scene.Text;
import com.jme.scene.state.TextureState;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.effects.Font3DGradient;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.material.Material;
import com.jmex.physics.util.SimplePhysicsGame;
import com.sun.labs.aura.aardvark.dashboard.story.SimulatedStoryManager;
import com.sun.labs.aura.aardvark.dashboard.story.Story;
import com.sun.labs.aura.aardvark.dashboard.story.StoryManager;
import java.awt.Font;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class shows the most simple physics with graphical representation: A dynamic geo falling onto a static floor
 * (also a geo).
 *
 * @author Irrisor
 */
public class Dashboard extends SimplePhysicsGame {

    private final static String VERSION = "Aardavark Dashboard V0.1";
    private StoryManager storyManager;
    private StoryPointFactory storyPointFactory;
    private Font3D font;
    private ArrayBlockingQueue<CPoint> storyQueue;
    private float timeSinceLastCheck = 0;
    private float newStoryTime = 1f;
    private boolean fixedFrameRate = false;

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
        if (promptForResolution) {
            setDialogBehaviour(AbstractGame.ALWAYS_SHOW_PROPS_DIALOG);
        } else {
            setDialogBehaviour(AbstractGame.FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        }

        if (simulate) {
            storyManager = new SimulatedStoryManager("stories.xml");
        } else {
            throw new UnsupportedOperationException("live story manager");
        }
    }

    protected void simpleInitGame() {
        showPhysics = false;
        initLogger();
        initPhysics();
        initStaticNodes();
        initLighting();
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

        KeyBindingManager.getKeyBindingManager().add("fire", KeyInput.KEY_F);
        KeyBindingManager.getKeyBindingManager().add("tfire", KeyInput.KEY_G);
        input.addAction(new MousePick(cam, rootNode));
        addCrossHairs();
    }

    private void initPhysics() {
        getPhysicsSpace().setDefaultMaterial(Material.GHOST);
    }

    private void initCamera() {
        Vector3f loc = cam.getLocation();
        loc = loc.add(new Vector3f(0, 0, 5));
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

        // first we will create the floor
        // as the floor can't move we create a _static_ physics node
        StaticPhysicsNode staticNode = getPhysicsSpace().createStaticNode();
        rootNode.attachChild(staticNode);
        staticNode.setMaterial(Material.CONCRETE);
        staticNode.generatePhysicsGeometry();
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
        storyPointFactory = new StoryPointFactory(display, getPhysicsSpace(), lightState);
        storyQueue = new ArrayBlockingQueue(10);
        Thread t = new Thread() {

            public void run() {
                collectStoryPoints();
            }
        };
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }
    private boolean oldFiring = false;

    @Override
    protected void simpleUpdate() {
        boolean firing = (KeyBindingManager.getKeyBindingManager().isValidCommand("fire", true));

        if (firing && !oldFiring) {
        //fire3();
        }

        boolean tfiring = (KeyBindingManager.getKeyBindingManager().isValidCommand("tfire", true));
        if (tfiring & !oldFiring) {
        //rootNode.attachChild(textController.addNewText(titles.remove(0)));
        //makeGrid(20, 20);
        }

        oldFiring = firing || tfiring;
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
                if (delay < -5) {
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

    private void checkForNewStories() {
        timeSinceLastCheck += tpf;
        if (timeSinceLastCheck > newStoryTime) {
            CPoint cp = storyQueue.poll();
            if (cp != null) {
                rootNode.attachChild(cp.getDNP());
                timeSinceLastCheck = 0f;
            }
        }
    }

    private void collectStoryPoints() {
        try {
            while (true) {
                List<Story> stories = storyManager.getNextStories(10);
                List<CPoint> points = new ArrayList<CPoint>();
                for (Story story : stories) {
                    CPoint cp = storyPointFactory.createStoryPoint(story);
                    points.add(cp);
                }

                for (CPoint cp : points) {
                    storyQueue.put(cp);
                }
            }
        } catch (InterruptedException ie) {
        }
    }
}


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
import com.jme.curve.BezierCurve;
import com.jme.curve.CurveController;
import com.jme.input.FirstPersonHandler;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.light.DirectionalLight;
import com.jme.light.PointLight;
import com.jme.light.SpotLight;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.CameraNode;
import com.jme.scene.Controller;
import com.jme.scene.SceneElement;
import com.jme.scene.Text;
import com.jme.scene.state.FogState;
import com.jme.scene.state.TextureState;
import com.sun.labs.aura.aardvark.dashboard.gui.QueryFrame;
import com.sun.labs.aura.aardvark.dashboard.story.Stats;
import com.sun.labs.aura.aardvark.dashboard.story.StatusManager;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;

/**
 * This class shows the most simple physics with graphical representation: A dynamic geo falling onto a static floor
 * (also a geo).
 *
 * @author Irrisor
 */
public class Dashboard extends SimpleGame {

    private final static String VERSION = "Aardavark Dashboard V0.1";
    private final static String LOCAL_URL = "http://localhost:8080/DashboardWebServices/";
    private final static String GRID_URL = "http://www.aardvark.tastekeeper.com/DashboardWebServices/";
    private StoryManager storyManager;
    private StoryPointFactory storyPointFactory;
    private StatusManager statusManager;
    private KeyboardHandler keyboardHandler;
    private JFrame queryFrame = null;
    private String baseUrl;
    private boolean liveMode;
    private int storiesPerMinute;
    private boolean fixedFrameRate = false;
    private boolean showStats = true;
    private int storiesSeen = 0;
    private CPoint rootControl;
    private CameraNode cameraNode;

    public static void main(String[] args) {
        try {
            boolean promptForResolution = false;
            boolean live = true;
            String url = GRID_URL;
            int spm = 100;

            for (int i = 0; i < args.length; i++) {
                String arg = args[i];
                if (arg.equals("--grid")) {
                    url = GRID_URL;
                } else if (arg.equals("--local")) {
                    url = LOCAL_URL;
                } else if (arg.equals("--setres")) {
                    promptForResolution = true;
                } else if (arg.equals("--live")) {
                    live = true;
                } else if (arg.equals("--replay")) {
                    live = false;
                } else if (arg.equals("--spm")) {
                    if (i < args.length - 1) {
                        spm = Integer.parseInt(args[++i]);
                    }
                } else {
                    System.out.println("Unknown param " + arg);
                    System.out.println("Usage: dashboard [[ [--local] | [--grid (default)] ]] [--spm n] [--setres] [--live (default) ] [--replay]");
                    System.exit(1);
                }
            }

            Dashboard dashboard = new Dashboard(url, promptForResolution, live, spm);
            dashboard.start();
        } catch (IOException ex) {
            System.err.println("Problem loading simulator " + ex);
        }
    }

    public Dashboard(String url, boolean promptForResolution, boolean live, int spm) throws IOException {
        if (promptForResolution) {
            setDialogBehaviour(AbstractGame.ALWAYS_SHOW_PROPS_DIALOG);
        } else {
            setDialogBehaviour(AbstractGame.FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        }
        baseUrl = url;
        liveMode = live;
        storiesPerMinute = spm;
    }

    protected void simpleInitGame() {
        //initRoot();
        initLogger();
        initLighting();
        initInput();
        initStoryQueue();

        display.setTitle("Aardvark Dashboard");
    }

    private void initRoot() {
       rootControl = new CPoint(rootNode) ;
       rootControl.add(new CmdControl(true));
       rootControl.poke();
    }


    private void initInput() {

        Vector3f loc = cam.getLocation();
        loc = loc.add(new Vector3f(0, 0, 25));
        cam.setLocation(loc);

        /*
         ControlledCamera controlledCamera = new ControlledCamera(cam);
         input = controlledCamera.getInputHandler();
         input = new NodeHandler(controlledCamera.getCameraNode(), 10, .2f);
         rootNode.attachChild(controlledCamera.getNode());
         */


        // adjust the speed of the input handler
        FirstPersonHandler fph = new FirstPersonHandler(cam, 10, 2);
        fph.getMouseLookHandler().getMouseLook().setSpeed(.2f);
        input = fph;

        input.addAction(new MousePick(cam, rootNode));

        // Remove some presets
        KeyBindingManager.getKeyBindingManager().remove("toggle_wire");

        keyboardHandler = new KeyboardHandler();

        keyboardHandler.addKeyHandler(KeyInput.KEY_F, "findSimilar", new KeyActionHandler() {

            public void onKey(String opName) {
                storyPointFactory.findStories();
            }
        });

        keyboardHandler.addKeyHandler(KeyInput.KEY_T, "findTags", new KeyActionHandler() {

            public void onKey(String opName) {
                storyPointFactory.findTags();
            }
        });

        keyboardHandler.addKeyHandler(KeyInput.KEY_O, "open", new KeyActionHandler() {

            public void onKey(String opName) {
                storyPointFactory.open();
            }
        });

        keyboardHandler.addKeyHandler(KeyInput.KEY_U, "toggleKiosk", new KeyActionHandler() {

            public void onKey(String opName) {
                toggleKioskMode();
            }
        });

        keyboardHandler.addKeyHandler(KeyInput.KEY_HOME, "search", new KeyActionHandler() {

            public void onKey(String opName) {
                if (queryFrame == null) {
                    queryFrame = new QueryFrame(storyManager);
                }
                queryFrame.setVisible(true);
            }
        });


        keyboardHandler.addKeyHandler(KeyInput.KEY_BACK, "clear", new KeyActionHandler() {

            public void onKey(String opName) {
                storyPointFactory.clear();
            }
        });

        /*
        keyboardHandler.addKeyHandler(KeyInput.KEY_HOME, "home", new KeyActionHandler() {
        public void onKey(String opName) {
        controlledCamera.add("home");
        }
        });
        keyboardHandler.addKeyHandler(KeyInput.KEY_END, "pan", new KeyActionHandler() {
        public void onKey(String opName) {
        controlledCamera.add("pan");
        }
        });
         * */

        addCrossHairs();
        

    }
    
    private void toggleKioskMode() {
        if (cameraNode == null) {
        // The path the camera will take.
        Vector3f[]cameraPoints=new Vector3f[]{
            new Vector3f(-20,5,20),
            new Vector3f(0,0,50),
            new Vector3f(20,0,20),
            new Vector3f(-5,-5,50),
        };

        // Create a path for the camera.
        BezierCurve bc=new BezierCurve("camera path",cameraPoints);

        // Create a camera node to move along that path.
        cameraNode=new CameraNode("camera node",cam);

        // Create a curve controller to move the CameraNode along the path
        CurveController cc=new CurveController(bc,cameraNode);

        // Cycle the animation.
        cc.setRepeatType(Controller.RT_CYCLE);

        // Slow down the curve controller a bit
        cc.setSpeed(.25f);
        cc.setActive(true);

        // Add the controller to the node.
        cameraNode.addController(cc);
        cameraNode.getLocalTranslation().set(cam.getLocation());

        // Attach the node to rootNode
        rootNode.attachChild(cameraNode);
        cam.lookAt(new Vector3f(0,0,0), Vector3f.UNIT_Y);

        } else {
            cameraNode.removeFromParent();
            cameraNode = null;
        }
    }

    private void initLogger() {
        Logger.getLogger("com.jme").setLevel(Level.WARNING);
        Logger.getLogger("com.jmex").setLevel(Level.WARNING);

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

        PointLight pl2 = new PointLight();
        sp2.setDiffuse(new ColorRGBA(7.0f, 7.0f, 7.0f, 1.0f));
        sp2.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        sp2.setLocation(new Vector3f(0, 10, 50));
        sp2.setEnabled(true);

        DirectionalLight dr = new DirectionalLight();
        dr.setDiffuse(new ColorRGBA(1.0f, 1.0f, 1.0f, 1.0f));
        dr.setAmbient(new ColorRGBA(0.5f, 0.5f, 0.5f, 1.0f));
        dr.setSpecular(new ColorRGBA(1.0f, 0.0f, 0.0f, 1.0f));
        dr.setDirection(new Vector3f(150, 0, 150));
        dr.setEnabled(true);

        lightState.detachAll();
        lightState.attach(sp1);
        lightState.attach(dr);
        lightState.attach(sp2);
        lightState.attach(pl2);
    /*
     */
    }

    private void addCrossHairs() {
        Text cross = Text.createDefaultTextLabel("Cross hairs", "+");
        cross.setCullMode(SceneElement.CULL_NEVER);
        cross.setTextureCombineMode(TextureState.REPLACE);
        cross.setLocalTranslation(
                new Vector3f(
                display.getWidth() / 2f - 8f, // 8 is half the width
                // of a font char
                display.getHeight() / 2f - 8f, 0));

        fpsNode.attachChild(cross);
    }

    private void initStoryQueue() {
        storyPointFactory = new StoryPointFactory(display, lightState, rootNode);
        storyManager = new StoryManager(storyPointFactory, baseUrl);
        statusManager = new StatusManager(baseUrl, 1000L);

        if (liveMode) {
            storyManager.setLiveMode(true);
            storyManager.setAsyncMode(false);
        } else {
            storyManager.setLiveMode(false);
            storyManager.setAsyncMode(true);
        }
        storyManager.setMaxStoriesPerMinute(storiesPerMinute);
        storyManager.start();

        // BUG - this mutual dependency between the storyManager and the
        // storypoint factory has got to go.

        storyPointFactory.setStoryManager(storyManager);

        if (showStats) {
            statusManager.start();
        }
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
        keyboardHandler.update(tpf);
        checkForNewStories();
        syncFrames();
        cameraUpdate();
        statsUpdate();
        //rootControl.update(tpf);
    }

    private void statsUpdate() {
        if (showStats) {
            Stats stats = statusManager.getStats();
            if (stats != null) {
                fps.print("Feeds: " + stats.getFeeds() + " Pulls: " + stats.getPulls() + " Entries: " + stats.getEntries() + " Seen: " + storiesSeen + " FPS " + (int) (1 / tpf));
            }
        }
    }

    private void cameraUpdate() {
        InteractivePoint sp = storyPointFactory.getCurrent();
        if (sp != null) {
        //cam.lookAt(sp.getNode().getWorldTranslation(), Vector3f.UNIT_Y);
        } else {
        }
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

    private void checkForNewStories() {
        CPoint cp = storyManager.getNext();
        if (cp != null) {
            storiesSeen++;
            rootNode.attachChild(cp.getNode());
        }
    }
}


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard;

/**
 *
 * @author plamere
 */
import com.jme.app.AbstractGame;
import com.jme.curve.BezierCurve;
import com.jme.curve.Curve;
import com.jme.curve.CurveController;
import com.jme.image.Texture;
import com.jme.input.ChaseCamera;
import com.jme.input.KeyBindingManager;
import com.jme.input.KeyInput;
import com.jme.input.action.InputActionEvent;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.scene.Controller;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import com.jme.scene.Text;
import com.jme.scene.shape.Box;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.util.TextureManager;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;
import com.jmex.font3d.effects.Font3DGradient;
import com.jmex.physics.DynamicPhysicsNode;
import com.jmex.physics.PhysicsSpace;
import com.jmex.physics.StaticPhysicsNode;
import com.jmex.physics.material.Material;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * This class shows the most simple physics with graphical representation: A dynamic box falling onto a static floor
 * (also a box).
 *
 * @author Irrisor
 */
public class Dashboard extends MySimplePhysicsGame {

    private Random rng = new Random();
    StaticPhysicsNode lowerFloor;
    TextController textController;
    List<String> titles;
    ChaseCamera chaseCamera;

    protected void simpleInitGame() {
        showPhysics = false;
        // first we will create the floor
        // as the floor can't move we create a _static_ physics node
        StaticPhysicsNode staticNode = getPhysicsSpace().createStaticNode();
        // attach the node to the root node to have it updated each frame
        rootNode.attachChild(staticNode);
        Spatial box = createFocusBox();
        staticNode.attachChild(box);
        box.setIsCollidable(false);


        textController = new TextController(getPhysicsSpace());
        textController.setBounds(80);


        rootNode.setLightCombineMode(LightState.OFF);
        Vector3f loc = cam.getLocation();
        loc = loc.add(new Vector3f(-2, 10, 10));
        cam.setLocation(loc);

        staticNode.setMaterial(Material.CONCRETE);
        // colorNode(visualFloorBox);

        // now we let jME Physics 2 generate the collision geometry for our box
        staticNode.generatePhysicsGeometry();


        // Adds the "u" key to the command "coordsUp"
        KeyBindingManager.getKeyBindingManager().add(
                "fire", KeyInput.KEY_F);
        KeyBindingManager.getKeyBindingManager().add(
                "tfire", KeyInput.KEY_G);
        titles = loadTitles();

        chaseCamera = new ChaseCamera(cam, box);
        
        input.addAction(new MousePick(cam, rootNode));
        addCrossHairs();
    //setInput(chaseCamera);
    }
    private boolean oldFiring = false;

    @Override
    protected void simpleUpdate() {
        //chaseCamera.update(tpf);
        // If the coordsDown command was activated
        boolean firing = (KeyBindingManager.getKeyBindingManager().isValidCommand("fire", true));

        if (firing && !oldFiring) {
            fire6();
        }

        boolean tfiring = (KeyBindingManager.getKeyBindingManager().isValidCommand("tfire", true));
        if (tfiring & !oldFiring) {
            //rootNode.attachChild(textController.addNewText(titles.remove(0)));
            makeGrid(20, 20);
        }

        oldFiring = firing || tfiring;
    //queueNewText();
    }
    long nextTime = 0l;
    int range = 300;
    int curIndex = 0;
    int count;

    private void queueNewText() {
        long now = System.currentTimeMillis();
        if (now >= nextTime) {
            nextTime = now + rng.nextInt(range);
            String text = titles.get(curIndex++);
            Node node = textController.addNewText(text);
            rootNode.attachChild(node);
            if (curIndex >= titles.size()) {
                curIndex = 0;
            }
        }
    }

    private void fire() {
        DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
        rootNode.attachChild(node);
        final Box box = new Box("missle", new Vector3f(), .1f, .1f, .1f);
        colorNode(box);
        node.attachChild(box);
        node.setMaterial(Material.CONCRETE);
        node.getLocalTranslation().set(cam.getLocation());
        // node.setAffectedByGravity(false);
        node.generatePhysicsGeometry();
        Vector3f dir = cam.getDirection();
        dir = dir.mult(50);
        node.setLinearVelocity(dir);
    }

    private void fire2() {
        DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
        final Box box = new Box("missle", new Vector3f(), .1f, .1f, .1f);
        CPoint cp = new CPoint(node, box);
        colorNode(box);
        cp.getDNP().setMaterial(Material.RUBBER);
        cp.getDNP().getLocalTranslation().set(cam.getLocation());
        cp.getDNP().setAffectedByGravity(false);
        rootNode.attachChild(node);

        Vector3f dir = cam.getDirection();
        dir = dir.mult(50);
        node.setLinearVelocity(dir);
    }

    private void fire3() {
        DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
        ControlledPhysicsNode cpn = new ControlledPhysicsNode(node);
        final Box box = new Box("missle", new Vector3f(), .1f, .1f, .1f);
        colorNode(box);
        node.attachChild(box);
        node.setMaterial(Material.RUBBER);
        node.getLocalTranslation().set(cam.getLocation());
        node.setAffectedByGravity(false);
        node.generatePhysicsGeometry();
        int col = (fcount % 10);
        int row = (fcount / 10);
        cpn.setSetPoint(new Vector3f(2 + col * .2f, 2 + row * .2f, 0));
        rootNode.attachChild(node);
        fcount++;
        Vector3f dir = cam.getDirection();
        dir = dir.mult(50);
        node.setLinearVelocity(dir);
    }

    private void fire4() {
        DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
        final Box box = new Box("missle", new Vector3f(), .1f, .1f, .1f);
        CPoint cp = new CPoint(node, box);
        colorNode(box);
        cp.getDNP().setMaterial(Material.RUBBER);

        cp.add(new CmdMove(-5, 5, 5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(-5, -5, 5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(-5, -5, -5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(-5, 5, -5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(5, -5, -5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(5, -5, 5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(5, 5, -5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(5, 5, 5));
        cp.add(new CmdWait(2));
        cp.add(new CmdMove(0, 10, 0));
        cp.add(new CmdWait(1));
        cp.add(new CmdMove(0, 20, 0));
        cp.add(new CmdWait(1));
        cp.add(new CmdMove(0, 0, 0));
        cp.add(new CmdWait(1));
        cp.add(new CmdWait(.5f));
        cp.add(new CmdControl(false));
        cp.add(new CmdGravity(true));
        cp.add(new CmdWait(10f));
        cp.add(new CmdRemove());

        cp.getDNP().getLocalTranslation().set(cam.getLocation());
        cp.getDNP().setAffectedByGravity(false);
        rootNode.attachChild(node);
    }
    Command[] cmdA = {
        new CmdStiff(),
        new CmdMove(-2, 3, -2), new CmdWait(1),
        new CmdMove(2, 3, -2), new CmdWait(1),
        new CmdMove(2, 3, 2), new CmdWait(1),
        new CmdMove(-2, 3, 2), new CmdWait(1),
        new CmdAddSet("loop")
    ,
                };

    Command[] cmdB = {
        new CmdSloppy(),
        new CmdMove(-2, -3, -2), new CmdWait(1),
        new CmdMove(2, -3, -2), new CmdWait(1),
        new CmdMove(2, -3, 2), new CmdWait(1),
        new CmdMove(-2, -3, 2), new CmdWait(1),
        new CmdAddSet("loop")
    ,
     

       
           
              

       
           
             };


    int fcount;
    private void fire5() {
        DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
        final Box box = new Box("missle", new Vector3f(), .1f, .1f, .1f);
        CPoint cp = new CPoint(node, box);
        colorNode(box);
        cp.getDNP().setMaterial(Material.RUBBER);

        Command[] cmds = fcount++ % 2 == 1 ? cmdA : cmdB;
        cp.addSet("loop", cmds);
        cp.add("loop");

        cp.getDNP().getLocalTranslation().set(cam.getLocation());
        rootNode.attachChild(node);
    }

    private void fire6() {
        DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
        final Box box = new Box("missle", new Vector3f(), 2.f, .2f, .1f);
        CPoint cp = new CPoint(node, box);
        textureNode(box, 512, 80);
        cp.getDNP().setMaterial(Material.RUBBER);

        Command[] cmds = fcount++ % 2 == 1 ? cmdA : cmdB;
        cp.addSet("loop", cmds);
        cp.add("loop");

        cp.getDNP().getLocalTranslation().set(cam.getLocation());
        rootNode.attachChild(node);
    }
    Command[] square = {
        new CmdMove(-6, 5, -6), new CmdWait(1),
        new CmdMove(6, 5, -6), new CmdWait(1),
        new CmdMove(6, 5, 6), new CmdWait(1),
        new CmdMove(-6, 5, 6), new CmdWait(1)
    };
    Command[] loop = {
        new CmdStiff(),
        new CmdAddSet("grid"),
        new CmdAddSet("square", 4),
        new CmdAddSet("loop")
    };


    CPoint selectedPoint;
    private void makeGrid(int rows, int cols) {
        ActionHandler ah = new ActionHandler() {
            public void performAction(CPoint cp, InputActionEvent evt) {
                if (selectedPoint != null) {
                    selectedPoint.unpoke();
                }
                selectedPoint = cp;
                selectedPoint.poke();
            }
        };

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                DynamicPhysicsNode node = getPhysicsSpace().createDynamicNode();
                final Box box = new Box("missle", new Vector3f(), .1f, .1f, .1f);
                node.getLocalTranslation().set(r * 2f, c * 2f, -20);
                CPoint cp = new CPoint(node, box);
                colorNode(box);
                cp.getDNP().setMaterial(Material.ICE);
                //cp.getDNP().setMaterial(Material.GHOST);
                float delay1 = 20 + (c + r * cols) * .1f;
                Command[] grid = {new CmdMove(c * .5f, 2 + r * .5f, 0),
                    new CmdWait(delay1)
                };

                cp.addActionHandler(ah);
                cp.addSet("grid", grid);
                cp.addSet("square", square);
                cp.addSet("loop", loop);

                cp.add("loop");

                rootNode.attachChild(node);
            }
        }
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

    private Spatial createFocusBox() {
        final Box box = new Box("fbox", new Vector3f(), 1.f, 1.f, 1.f);
        box.setRandomColors();
        box.updateRenderState();

        Vector3f[] pointsOld = {
            new Vector3f(-20, 10, 5),
            new Vector3f(-10, 5, 7),
            new Vector3f(0, 5, 3),
            new Vector3f(10, 5, 7),
            new Vector3f(20, 10, 5),
            new Vector3f(10, 5, 7),
            new Vector3f(0, 5, 3),
            new Vector3f(-10, 5, 7),
            new Vector3f(-20, 10, 5)
        ,
                };

        Vector3f[] points = {
                new Vector3f(0, 20, 0),
            new Vector3f(80, 40, 0),
            new Vector3f(0, 20, 0),
            new Vector3f(-80, 00, 0),
            new Vector3f(0, 20, 0)
        ,
                };
        Curve curve = new BezierCurve("bcurve", points);
        CurveController cc = new CurveController(curve, box);
        cc.setRepeatType(Controller.RT_WRAP);
        cc.setMinTime(0f);
        cc.setMaxTime(Float.MAX_VALUE);
        cc.setSpeed(.01f);

        //box.addController(cc);

        return box;
    }

    private void colorNode(Geometry node) {
        node.setRandomColors();
        node.updateRenderState();
    }
    Font font = new Font("Arial", Font.PLAIN, 24);

    private void textureNode(Geometry node, int width, int height) {
        TextureState bgts = display.getRenderer().createTextureState();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics g = image.getGraphics();
        g.setColor(Color.GRAY);
        g.fillRect(0, 0, width, height);
        g.setColor(Color.GREEN);
        g.setFont(font);
        Date d = new Date();
        String text = titles.get(curIndex++);
        g.drawString(text, 5, height / 2);
        Texture texture = TextureManager.loadTexture(image, Texture.MM_LINEAR, Texture.FM_LINEAR, true);
        bgts.setTexture(texture);
        node.setRenderState(bgts);
        node.updateRenderState();
    }

    private List<String> loadTitles() {
        List<String> results = new ArrayList<String>();
        try {
            BufferedReader reader = new BufferedReader(new FileReader("titles.txt"));

            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    line = filter(line);
                    results.add(line);
                }
            } finally {
                reader.close();
            }
        } catch (IOException ioe) {
            System.out.println("Trouble " + ioe);
        }
        Collections.shuffle(results);
        return results;
    }

    String filter(String line) {
        line = line.trim();
        line = line.replaceAll("[^\\p{ASCII}]", "");
        return line;
    }

    /**
     * The main method to allow starting this class as application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        Logger.getLogger("").setLevel(Level.WARNING); // to see the important stuff
        Dashboard dashboard = new Dashboard();
        //dashboard.setDialogBehaviour(AbstractGame.FIRSTRUN_OR_NOCONFIGFILE_SHOW_PROPS_DIALOG);
        dashboard.setDialogBehaviour(AbstractGame.ALWAYS_SHOW_PROPS_DIALOG);
        dashboard.start();
    }

    class TextController {

        private PhysicsSpace physicsSpace;
        private int count;
        private Random rng = new Random();
        float angle = 0;
        float delta = (float) (2 * Math.PI / 20);
        Font3D fonts[] = new Font3D[6];
        float bounds = 40;
        int rows = 40;

        //Font3D myfont = new Font2D(new Font("Arial", Font.PLAIN, 1), 0.1, true, true, true);
        TextController(PhysicsSpace ps) {
            physicsSpace = ps;

            for (int i = 0; i < fonts.length; i++) {
                fonts[i] = new Font3D(new Font("Arial", Font.PLAIN, 1), 0.1, true, true, true);
                ColorRGBA color = ColorRGBA.randomColor();
                Font3DGradient gradient = new Font3DGradient(Vector3f.UNIT_Y, color, color);
                gradient.applyEffect(fonts[i]);
            }
        }

        void setBounds(float bounds) {
            this.bounds = bounds;
        }

        DynamicPhysicsNode addNewText(String txt) {
            count++;
            angle += delta;
            if (angle > Math.PI * 2) {
                angle -= Math.PI * 2;
            }

            float curY = rng.nextInt(rows);
            float curZ = (float) (Math.cos(angle + Math.PI) * 5 + 5);

            Font3D myfont = fonts[count % fonts.length];
            Text3D text = myfont.createText(txt, 1, 0);
            text.setLocalScale(new Vector3f(1, 1, 0.1f));
            text.updateWorldBound();
            DynamicPhysicsNode node = physicsSpace.createDynamicNode();
            node.attachChild(text);
            node.getLocalTranslation().set(bounds - 10, curY, curZ);
            node.setAffectedByGravity(false);
            //node.generatePhysicsGeometry();
            Vector3f dir = new Vector3f(-1f, .0f, .0f);
            float vel = (rng.nextFloat() + 2) * 6.0f;
            dir = dir.mult(vel);
            node.setLinearVelocity(dir);

            // fun with rotations!
            // Vector3f ang = new Vector3f(10f, .1f, .1f);
            // node.setAngularVelocity(ang);

            node.addController(new OutOfBoundsCleanupController(text, bounds));
            return node;

        }
    }

    class OutOfBoundsCleanupController extends Controller {

        private Spatial node;
        private float bounds;

        OutOfBoundsCleanupController(Spatial node, float bounds) {
            this.node = node;
            this.bounds = bounds;
        }

        @Override
        public void update(float time) {
            Vector3f point = node.getWorldTranslation();
            check(point.x);
            check(point.y);
            check(point.z);
        }

        private void check(float val) {
            //System.out.println("check " + val + " " + bounds);
            if (val > bounds) {
                node.removeFromParent();
            }
            if (val < -bounds) {
                node.removeFromParent();
            }
        }
    }
}

class PNode {

    private DynamicPhysicsNode physNode;
    private Spatial spatial;

    PNode(Spatial spatial) {
        this.spatial = spatial;
    }

    DynamicPhysicsNode getPhysNode() {
        return physNode;
    }

    Spatial getSpatial() {
        return spatial;
    }
}

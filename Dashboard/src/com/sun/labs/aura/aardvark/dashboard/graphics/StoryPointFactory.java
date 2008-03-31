/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.math.Vector3f;
import com.jme.renderer.Renderer;
import com.jme.scene.Geometry;
import com.jme.scene.shape.Box;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.physics.PhysicsSpace;
import com.sun.labs.aura.aardvark.dashboard.story.Story;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.border.Border;

/**
 *
 * @author plamere
 */
public class StoryPointFactory {
    private DisplaySystem display;
    private PhysicsSpace physicsSpace;
    private LightState lightState;
    private Random rng = new Random();
    private int pointCount = 0;

    private final static int ROWS = 20;
    private float rowWidth = 2.3f;
    private Font lfont = new Font("Arial", Font.PLAIN, 24);

    final static int MAX_DESCRIPTION_LEN = 700;
    
    public StoryPointFactory(DisplaySystem display, PhysicsSpace physicsSpace, LightState lightState) {
        this.display = display;
        this.physicsSpace = physicsSpace;
        this.lightState = lightState;
    }
    
    public CPoint createStoryPoint(Story story) {
        return new StoryPoint(story);
    }

    class StoryPoint extends CPoint {

        private Story story;
        private final int width = 350;
        private final int height = 350;
        private float initX = rng.nextFloat() * 20;
        private float initY = rng.nextFloat() * 20;

        StoryPoint(Story story) {
            super(physicsSpace.createDynamicNode());

            this.story = story;
            addGeometry(getFrontImageTile());
            addGeometry(getBackImageTile());

            getDNP().getLocalTranslation().set(initX, initY, -50);
            getDNP().setRenderQueueMode(Renderer.QUEUE_OPAQUE);

            float LEFT = -1.6f;
            float RIGHT = .6f;

            pointCount++;
            float xpos = pointCount % 2 == 1 ? LEFT : RIGHT;


            /*
            Command[] grid = {
            new CmdStiff(),
            new CmdMove(xpos, 10, -50),
            new CmdWait(.5f),
            new CmdMove(row * rowWidth, 0, -10),
            new CmdWait(.5f),
            new CmdMove(xpos, ypos, 0),
            new CmdWait(120),
            //new CmdMove(0, 0, 30),
            new CmdMove(xpos, ypos, 5),
            new CmdWait(.1f),
            new CmdGravity(true),
            new CmdControl(false),
            new CmdWait(3f),
            new CmdRemove()
            };
             **/

            /*
            Command[] twolines = {
            new CmdVeryStiff(),
            new CmdMove(0, 10, -50),
            new CmdWait(2f),
            new CmdMove(xpos, 0, -5),
            new CmdWait(5f),
            new CmdMove(xpos * 10, 0, -4),
            new CmdWait(.2f),
            new CmdGravity(true),
            new CmdControl(false),
            new CmdWait(3f),
            new CmdRemove()
            };
            add(twolines);
             */


            float[] rows = {-4.5f, 0, 4.5f};
            float row = rows[pointCount % 3];
            float zOffset = rng.nextFloat() * .3f;

            Command[] threelines = {
                new CmdVeryStiff(),
                new CmdMove(0, 10, -50 + zOffset),
                new CmdWait(2f),
                new CmdMove(-20, row, -5 + zOffset),
                new CmdWait(.2f),
                new CmdMove(-20, row, 0 + zOffset),
                new CmdWait(.2f),
                new CmdVel(1, 0, 0),
                new CmdWait(10f),
                new CmdGravity(true),
                new CmdWait(3f),
                new CmdRemove()
            };

            add(threelines);
        }

        Story getStory() {
            return story;
        }

        Geometry getFrontImageTile() {
            String imgHtml = "";

            // image loading is async
            if (false && story.getImageUrl() != null) {
                imgHtml = "<img src=\"" + story.getImageUrl() + "\">";
            }
            String description = story.getDescription();

            if (description.length() > MAX_DESCRIPTION_LEN) {
                description = description.substring(0, MAX_DESCRIPTION_LEN) + "...";
            }
            String html = "<html><body>" + "<h2>" + story.getTitle() + "</h2>" +
                    imgHtml + getStarRatingHtml() + description + "<p> From <b>" + story.getSource() + "</b>" + "</body><html>";

            Box box = new Box(story.getTitle(), new Vector3f(), 1, 1f, .01f);
            BufferedImage image = renderHTML(html);
            box.setRenderState(imageToTextureState(image));
            applyStandardAttributes(box);
            return box;
        }

        Geometry getBackImageTile() {
            Box box = new Box("backside of " + story.getTitle(), new Vector3f(0, 0, -.011f), 1, 1f, .01f);
            String html = "<body> This <b>is the</b> backside. Isn't this wonderful?!!?!?</body>";
            BufferedImage image = renderHTML(html);
            box.setRenderState(imageToTextureState(image));
            applyStandardAttributes(box);
            return box;
        }

        private void applyStandardAttributes(Geometry geo) {
            geo.setRenderState(lightState);
            ZBufferState zstate = display.getRenderer().createZBufferState();
            zstate.setEnabled(true);
            geo.setRenderState(zstate);
            geo.updateRenderState();
            geo.setModelBound(new BoundingBox());
            geo.updateModelBound();
        }

        private TextureState imageToTextureState(BufferedImage image) {
            Texture texture = TextureManager.loadTexture(image, Texture.MM_LINEAR, Texture.FM_LINEAR, true);
            TextureManager.clearCache(); // don't trust the cache
            TextureState ts = display.getRenderer().createTextureState();
            ts.setEnabled(true);
            ts.setTexture(texture);
            return ts;
        }

        private BufferedImage renderHTML(String html) {
            JEditorPane htmlDisplay = new JEditorPane();
            htmlDisplay.setEditable(false);
            htmlDisplay.setContentType("text/html");
            attachBorder(htmlDisplay);
            htmlDisplay.setText(html);
            htmlDisplay.setPreferredSize(new Dimension(width, height));
            htmlDisplay.setBounds(0, 0, width, height);
            return createImage(htmlDisplay, BufferedImage.TYPE_INT_ARGB);
        }

        private void attachBorder(JComponent jc) {
            Border outerborder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            Border outborder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            Border inborder = BorderFactory.createEtchedBorder();
            Border tborder = BorderFactory.createCompoundBorder(inborder, outborder);
            Border border = BorderFactory.createCompoundBorder(outerborder, tborder);
            jc.setBorder(border);
        }

        private String htmlColor(String s, String color) {
            return "<font color=\"" + color + "\">" + s + "</font>";
        }

        private String htmlSize(String s, int size) {
            return "<font size=\"" + size + "\">" + s + "</font>";
        }

        private String getStarRatingHtml() {
            int stars = (int) (story.getScore() * 4 + 1);
            String[] ratings = {"", "*", "**", "***", "****", "*****"};

            String rating = ratings[stars];
            if (stars >= 3) {
                rating = htmlColor(rating, "green");
            }
            return ("<b>" + rating + "</b> ");
        }

        public BufferedImage createImage(JComponent component, int imageType) {
            Dimension componentSize = component.getPreferredSize();
            component.setSize(componentSize); //Make sure these 
            //are the same
            BufferedImage img = new BufferedImage(componentSize.width, componentSize.height, imageType);
            Graphics2D grap = img.createGraphics();
            grap.fillRect(0, 0, img.getWidth(), img.getHeight());
            component.paint(grap);
            return img;
        }
    }
}

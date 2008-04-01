/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sun.labs.aura.aardvark.dashboard.graphics;

import com.jme.bounding.BoundingBox;
import com.jme.image.Texture;
import com.jme.input.MouseInput;
import com.jme.input.action.InputActionEvent;
import com.jme.math.FastMath;
import com.jme.math.Vector3f;
import com.jme.renderer.ColorRGBA;
import com.jme.renderer.Renderer;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.shape.Box;
import com.jme.scene.state.LightState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;
import com.jmex.font3d.effects.Font3DGradient;
import com.sun.labs.aura.aardvark.dashboard.story.Classification;
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
    private LightState lightState;
    private Random rng = new Random();
    private int pointCount = 0;
    final static int MAX_DESCRIPTION_LEN = 700;
    private Font3D fonts[] = new Font3D[6];
    private Node rootNode;
    private StoryPoint curStoryPoint = null;
    private final static int MAX_SIMS = 8;
    private StoryPoint[] simStories = new StoryPoint[MAX_SIMS];

    public StoryPointFactory(DisplaySystem display, LightState lightState, Node rootNode) {
        this.display = display;
        this.lightState = lightState;
        this.rootNode = rootNode;

        for (int i = 0; i < fonts.length; i++) {
            fonts[i] = new Font3D(new Font("Arial", Font.PLAIN, 1), 0.1, true, true, true);
            ColorRGBA color = ColorRGBA.randomColor();
            Font3DGradient gradient = new Font3DGradient(Vector3f.UNIT_Y, color, color);
            gradient.applyEffect(fonts[i]);
        }
    }

    public StoryPoint createTileStoryPoint(Story story, float x, float y, float z) {
        return new TileStoryPoint(story, x, y, z);
    }

    public StoryPoint createTileStoryPoint(Story story, int simSpot) {
        StoryPoint sp = new TileStoryPoint(story, simSpot);
        simStories[simSpot] = sp;
        return sp;
    }

    public StoryPoint createBoxStoryPoint(Story story) {
        return new BoxStoryPoint(story);
    }

    public StoryPoint createHeadlineStoryPoint(Story story) {
        return new HeadlineStoryPoint(story);
    }
    
    public int getNumSimStories() {
        return simStories.length;
    }

    public void clearSimStories() {
        for (int i = 0; i < simStories.length; i++) {
            if (simStories[i] != null) {
                simStories[i].add("dismiss");
                simStories[i] = null;
            }
        }
    }
    
    public boolean hasAllSimStories() {
        for (int i = 0; i < simStories.length; i++) {
            if (simStories[i] == null) {
                return false;
            }
        }
        return true;
    }


    private void applyStandardAttributes(Geometry geo) {
        geo.setRenderState(lightState);
        ZBufferState zstate = display.getRenderer().createZBufferState();
        zstate.setEnabled(true);
        geo.setRenderState(zstate);
        geo.updateRenderState();
        geo.setModelBound(new BoundingBox());
        geo.updateModelBound();
        geo.updateWorldBound();
    }

    public StoryPoint getCurrentStoryPoint() {
        return curStoryPoint;
    }

    class BoxStoryPoint extends StoryPoint {

        BoxStoryPoint(Story story) {
            super(story, -10, 10, -3);
            addGeometry(getGeometry());

            pointCount++;

            Command[] cmds = {
                new CmdControl(false),
                new CmdVel(5, 10 + rng.nextFloat(), 0),
                new CmdWait(1.5f),
                new CmdRotate(FastMath.PI / 2, FastMath.PI / 4, 0),
                new CmdGravity(true),
                new CmdWait(5f),
                new CmdRemove()
            };
            add(cmds);
        }

        Geometry getGeometry() {
            Box box = new Box("box " + story.getTitle(),
                    new Vector3f(), .1f, .1f, .1f);
            //box.setDefaultColor(ColorRGBA.randomColor());
            //box.setRandomColors();
            applyStandardAttributes(box);
            box.setDefaultColor(ColorRGBA.randomColor());
            return box;
        }
    }

    class HeadlineStoryPoint extends StoryPoint {

        HeadlineStoryPoint(Story story) {
            super(story, 160, 40, rng.nextFloat() * 5);

            pointCount++;

            createGeometries();

            //getNode().updateWorldBound();
            //getNode().setModelBound(new BoundingBox());
            //getNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);
            float dropAngle = FastMath.PI / 2 * rng.nextFloat();

            float yspot = rng.nextFloat() * 40 - 20;
            float zspot = rng.nextFloat() * 25;

            Command[] cmds = {
                new CmdControl(true),
                new CmdVeryStiff(),
                new CmdMove(40, yspot, zspot),
                new CmdWait(2.f),
                new CmdMove(30, yspot, zspot),
                new CmdWait(1.f),
                new CmdControl(false),
                new CmdVel(-5 + -5 * rng.nextFloat(), 0, 0),
                new CmdWaitBounds(50, 50, 50),
                new CmdRotate(dropAngle, dropAngle, dropAngle),
                new CmdGravity(true),
                new CmdWait(5f),
                new CmdRemove()
            };

            add(cmds);

            addActionHandler(new ActionHandler() {

                public void performAction(CPoint cp, InputActionEvent evt) {
                    cp.setRelativeAngle(0f, FastMath.PI, 0f, .5f);
                    Vector3f cur = getNode().getWorldTranslation();
                    StoryPoint sp = createTileStoryPoint(getStory(), cur.x, cur.y, cur.z - 10);
                    if (curStoryPoint != null) {
                        curStoryPoint.add("dismiss");
                    }
                    rootNode.attachChild(sp.getNode());
                    sp.add("home");
                    curStoryPoint = sp;
                }
            });
        }

        private void createGeometries() {
            Font3D myfont = fonts[fontCount++ % fonts.length];
            Text3D text = myfont.createText(story.getTitle(), 1, 0);
            text.alignCenter();
            text.setLocalScale(new Vector3f(1, 1, 0.1f));
            applyStandardAttributes(text);
            //TBD culling is messed
            text.setCullMode(SceneElement.CULL_NEVER);


            addGeometry(text);

            // add a bounding box

            Box box = new Box("bbox " + story.getTitle(),
                    new Vector3f(0, .3f, .05f), text.getWidth() / 2, 1f / 2, .05f);
            box.setDefaultColor(new ColorRGBA(0f, .0f, 0f, 1f));
            addGeometry(box);
        }
    }
    int fontCount;

    class TileStoryPoint extends StoryPoint {

        private final int width = 350;
        private final int height = 350;
        private Command[] clicked = {new CmdRotateRelative(0, FastMath.PI, 0, .5f)};

        private Command[] home = {
            new CmdControl(true),
            new CmdVeryStiff(),
            new CmdMove(0, 0, 45),
            new CmdRotate(0, FastMath.PI, 0),
            new CmdWait(.5f),
            new CmdRotate(0, 0, 0),
            new CmdWait(.5f)
        };
        private Command[] dismiss = {
            new CmdControl(true),
            new CmdSloppy(),
            new CmdMove(0, 0, -245),
            new CmdRotate(FastMath.PI, FastMath.PI / 2, FastMath.PI, .5f),
            new CmdWait(.5f),
            new CmdRotate(FastMath.PI / 2, FastMath.PI, FastMath.PI, 1.5f),
            new CmdWait(1.5f),
            new CmdWait(5f),
            new CmdRemove()
        };

        Vector3f[] spots = {
            new Vector3f(0,     -2.1f,  45),
            new Vector3f(0,     2.1f,   45),
            new Vector3f(2.1f,  0,      45),
            new Vector3f(-2.1f, 0,      45),
            new Vector3f(-2.1f, -2.1f,  45),
            new Vector3f(-2.1f, 2.1f,   45),
            new Vector3f(2.1f,  -2,     45),
            new Vector3f(2.1f,  2.1f,   45) };


        TileStoryPoint(Story story, float x, float y, float z) {
            super(story, x, y, z);
            this.story = story;
            addGeometry(getFrontImageTile());
            addGeometry(getBackImageTile());
            getNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);

            pointCount++;

            addActionHandler(new ActionHandler() {

                public void performAction(CPoint cp, InputActionEvent evt) {
                    if (MouseInput.get().isButtonDown(0)) {
                        add("clicked");
                    } else if (MouseInput.get().isButtonDown(1)) {
                        add("dismiss");
                        curStoryPoint = null;
                        clearSimStories();
                    }
                }
            });

            addSet("home", home);
            addSet("clicked", clicked);
            addSet("dismiss", dismiss);
        }

        TileStoryPoint(Story story, int which) {
            this(story, 0, 0, 44);

            Command[] spothome = {
                new CmdControl(true),
                new CmdVeryStiff(),
                new CmdMove(spots[which % spots.length]),
                new CmdRotate(0, FastMath.PI, 0),
                new CmdWait(.5f),
                new CmdRotate(0, 0, 0),
                new CmdWait(.5f)
            };

            addSet("home", spothome);
        }


        private void goSpot(int which) {
            add(new CmdMove(spots[which % spots.length]));
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
            BufferedImage image = renderHTML(html, null);
            box.setRenderState(imageToTextureState(image));
            applyStandardAttributes(box);
            return box;
        }

        Geometry getBackImageTile() {
            Box box = new Box("backside of " + story.getTitle(), new Vector3f(0, 0, -.011f), 1, 1f, .01f);
            StringBuilder sb = new StringBuilder();
            sb.append("<body>");

            for (Classification c : story.getClassifications()) {
                sb.append(getClassificationTextForCloud(c));
                sb.append("  ");
            }
            sb.append("</body>");
            String html = sb.toString();
            BufferedImage image = renderHTML(html, "Tags");
            box.setRenderState(imageToTextureState(image));
            applyStandardAttributes(box);
            return box;
        }

        private String getClassificationTextForCloud(Classification c) {
            int size = (int) (c.getScore() * 3 + 1);
            return htmlSize(c.getName(), size);
        }

        /*
        private void applyStandardAttributes(Geometry geo) {
        geo.setRenderState(lightState);
        ZBufferState zstate = display.getRenderer().createZBufferState();
        zstate.setEnabled(true);
        geo.setRenderState(zstate);
        geo.updateRenderState();
        geo.setModelBound(new BoundingBox());
        geo.updateModelBound();
        }
         * */
        private TextureState imageToTextureState(BufferedImage image) {
            Texture texture = TextureManager.loadTexture(image, Texture.MM_LINEAR, Texture.FM_LINEAR, true);
            TextureManager.clearCache(); // don't trust the cache
            TextureState ts = display.getRenderer().createTextureState();
            ts.setEnabled(true);
            ts.setTexture(texture);
            return ts;
        }

        private BufferedImage renderHTML(String html, String title) {
            JEditorPane htmlDisplay = new JEditorPane();
            htmlDisplay.setEditable(false);
            htmlDisplay.setContentType("text/html");
            attachBorder(htmlDisplay, title);
            htmlDisplay.setText(html);
            htmlDisplay.setPreferredSize(new Dimension(width, height));
            htmlDisplay.setBounds(0, 0, width, height);
            return createImage(htmlDisplay, BufferedImage.TYPE_INT_ARGB);
        }

        private void attachBorder(JComponent jc, String title) {
            Border outerborder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            Border outborder = BorderFactory.createEmptyBorder(10, 10, 10, 10);
            Border inborder;
            if (title != null) {
                inborder = BorderFactory.createTitledBorder(title);
            } else {
                inborder = BorderFactory.createEtchedBorder();
            }
            Border tborder = BorderFactory.createCompoundBorder(inborder, outborder);
            Border border = BorderFactory.createCompoundBorder(outerborder, tborder);
            jc.setBorder(border);
        }

        private String htmlColor(String s, String color) {
            return "<font color=\"" + color + "\">" + s + "</font>";
        }

        private String htmlSize(String s, int size) {
            return "<font size=\"+" + size + "\">" + s + "</font>";
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

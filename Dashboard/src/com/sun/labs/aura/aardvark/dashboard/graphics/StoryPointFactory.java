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
import com.jme.scene.Controller;
import com.jme.scene.Geometry;
import com.jme.scene.Node;
import com.jme.scene.SceneElement;
import com.jme.scene.Spatial;
import com.jme.scene.shape.Box;
import com.jme.scene.state.LightState;
import com.jme.scene.state.MaterialState;
import com.jme.scene.state.TextureState;
import com.jme.scene.state.ZBufferState;
import com.jme.system.DisplaySystem;
import com.jme.util.TextureManager;
import com.jmex.font3d.Font3D;
import com.jmex.font3d.Text3D;
import com.jmex.font3d.effects.Font3DGradient;
import com.sun.labs.aura.aardvark.dashboard.story.ScoredString;
import com.sun.labs.aura.aardvark.dashboard.story.Story;
import com.sun.labs.aura.aardvark.dashboard.story.TagInfo;
import com.sun.labs.aura.aardvark.dashboard.story.Util;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.net.URL;
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
    private final static int MAX_SIMS = 8;
    // tileCloud[0] is center - all the others are neighbors
    private StoryPoint[] tileCloud = new StoryPoint[MAX_SIMS + 1];
    private StoryManager storyManager;
    int fontCount;

    public StoryPointFactory(DisplaySystem display, LightState lightState, Node rootNode) {
        this.display = display;
        this.lightState = lightState;
        this.rootNode = rootNode;

        // applyTexture(rootNode, "dirt.jpg");

        for (int i = 0; i < fonts.length; i++) {
            fonts[i] = new Font3D(new Font("Arial", Font.PLAIN, 1), 0.1, true, true, true);
            ColorRGBA color = ColorRGBA.randomColor();
            Font3DGradient gradient = new Font3DGradient(Vector3f.UNIT_Y, color, color);
            gradient.applyEffect(fonts[i]);
        }
    }

    public void setStoryManager(StoryManager sm) {
        storyManager = sm;
    }

    public StoryPoint createTileStoryPoint(Story story, float x, float y, float z) {
        StoryPoint sp = new TileStoryPoint(story, x, y, z);
        sp.init();
        return sp;
    }

    public StoryPoint createTileStoryPoint(Story story, int cloudSpot) {
        StoryPoint sp = new TileStoryPoint(story, cloudSpot);
        sp.init();
        tileCloud[cloudSpot] = sp;
        return sp;
    }

    public StoryPoint createTagInfoTileStoryPoint(Story story, TagInfo ti, int cloudSpot) {
        StoryPoint sp = new TagInfoTileStoryPoint(story, ti, cloudSpot);
        tileCloud[cloudSpot] = sp;
        sp.init();
        return sp;
    }

    public StoryPoint createBoxStoryPoint(Story story) {
        StoryPoint sp = new BoxStoryPoint(story);
        sp.init();
        return sp;
    }

    public StoryPoint createHeadlineStoryPoint(Story story) {
        StoryPoint sp = new HeadlineStoryPoint(story);
        sp.init();
        return sp;
    }

    public int getNumSimStories() {
        return MAX_SIMS;
    }

    public void clearCloud() {
        for (int i = 0; i < tileCloud.length; i++) {
            if (tileCloud[i] != null) {
                tileCloud[i].add("dismiss");
                tileCloud[i] = null;
            }
        }
    }

    public void clearNeighbors() {
        for (int i = 1; i < tileCloud.length; i++) {
            if (tileCloud[i] != null) {
                tileCloud[i].add("dismiss");
                tileCloud[i] = null;
            }
        }
    }

    public boolean isFullCloud() {
        for (int i = 0; i < tileCloud.length; i++) {
            if (tileCloud[i] == null) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnyCloud() {
        for (int i = 0; i < tileCloud.length; i++) {
            if (tileCloud[i] != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isAnyNeighbors() {
        for (int i = 1; i < tileCloud.length; i++) {
            if (tileCloud[i] != null) {
                return true;
            }
        }
        return false;
    }

    private boolean findAndClearFromNeighbors(StoryPoint sp) {
        for (int i = 1; i < tileCloud.length; i++) {
            if (tileCloud[i] == sp) {
                tileCloud[i] = null;
                return true;
            }
        }
        return false;
    }

    public void findStories() {
        if (isAnyNeighbors()) {
            clearNeighbors();
        } else {
            StoryPoint sp = getCurrentStoryPoint();
            if (sp != null) {
                sp.add("poke");
                sp.findStories();
            }
        }
    }

    public void findTags() {
        if (isAnyNeighbors()) {
            clearNeighbors();
        } else {
            StoryPoint sp = getCurrentStoryPoint();
            if (sp != null) {
                sp.add("poke");
                sp.findTags();
            }
        }
    }

    public void open() {
        StoryPoint sp = getCurrentStoryPoint();
        if (sp != null) {
            sp.add("poke");
            sp.open();
        }
    }

    public StoryPoint getCurrentStoryPoint() {
        return tileCloud[0];
    }

    public void setCurrentStoryPoint(StoryPoint newCur) {
        if (tileCloud[0] != null) {
            clearCloud();
        }

        tileCloud[0] = newCur;

        if (tileCloud[0] != null) {
            tileCloud[0].add("home");
        }
    }

    public void clearAllStories() {
        setCurrentStoryPoint(null);
        clearCloud();
    }

    private float clamp(float val, float min, float max) {
        return val > max ? max : val < min ? min : val;
    }

    private void applyTexture(Spatial spatial, String name) {
        URL url = StoryPointFactory.class.getResource("data/" + name);
        Texture texture = TextureManager.loadTexture(url, Texture.MM_LINEAR, Texture.FM_LINEAR);
        TextureState ts = display.getRenderer().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(texture);
        spatial.setRenderState(ts);
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

        private float scale = 1.0f;

        HeadlineStoryPoint(Story story) {
            super(story, 160, 40, rng.nextFloat() * 5);
            pointCount++;

            scale = story.getLength() / 1000.0f;
            scale = clamp(scale, .6f, 2);

            createGeometries();

            //getNode().updateWorldBound();
            //getNode().setModelBound(new BoundingBox());
            //getNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);
            float dropAngle = FastMath.PI / 2 * rng.nextFloat();

            float yspot = rng.nextFloat() * 40 - 20;
            float zspot = rng.nextFloat() * 10;

            Command[] cmds = {
                new CmdControl(true),
                new CmdVeryStiff(),
                new CmdMove(40, yspot, zspot),
                new CmdWait(2.f),
                new CmdMove(30, yspot, zspot),
                new CmdWait(1.f),
                new CmdControl(false),
                new CmdVel(-5 + -5 * rng.nextFloat(), 0, 0),
                new CmdWaitBounds(70, 50, 50),
                new CmdRotate(dropAngle, dropAngle, dropAngle),
                new CmdGravity(true),
                new CmdWait(5f),
                new CmdRemove()
            };

            add(cmds);

            addActionHandler(new ActionHandler() {

                public void performAction(CPoint cp, InputActionEvent evt) {
                    cp.setRelativeAngle(FastMath.PI, 0f, 0f, .5f);
                    Vector3f cur = getNode().getWorldTranslation();
                    StoryPoint sp = createTileStoryPoint(getStory(), cur.x, cur.y, cur.z - 10);
                    setCurrentStoryPoint(sp);
                    rootNode.attachChild(sp.getNode());
                }
            });
        }

        private void createGeometries() {
            Font3D myfont = fonts[fontCount++ % fonts.length];
            Text3D text = myfont.createText(story.getTitle(), 1, 0);
            //text.alignCenter();

            // add a bounding box

            Box box = new Box("bbox " + story.getTitle(),
                    new Vector3f(text.getWidth() / 2, .3f, .05f), text.getWidth() / 2, 1f / 2, .05f);

            int offset = 0;

            for (ScoredString c : story.getTags()) {
                float size = .1f * scale;
                attachChild(new Orbiter(
                        new Vector3f((offset++ * size * 8), .3f, 0), scale * 2, size, true));
                        //new Vector3f(text.getWidth() * 3 / 4 - (offset++ * size * 8), .3f, 0), scale, size, true));
            }

            offset = 0;
            for (ScoredString c : story.getAutotags()) {
                float size = .1f * scale;
                attachChild(new Orbiter(
                        new Vector3f(text.getWidth() / 4 + (offset++ * size * 8), .3f, 0), scale * 2, size, false));
            }

            text.setLocalScale(new Vector3f(scale, scale, scale / 10));
            box.setLocalScale(new Vector3f(scale, scale, scale / 10));

            applyStandardAttributes(text);
            text.setCullMode(SceneElement.CULL_NEVER);
            box.setDefaultColor(new ColorRGBA(0f, .0f, 0f, 0f));
            box.setRenderState(lightState);

            addGeometry(text);
            addGeometry(box);

        }
    }

    class TileStoryPoint extends StoryPoint {

        private final int width = 350;
        private final int height = 350;

        TileStoryPoint(Story story, float x, float y, float z) {
            super(story, x, y, z);
            this.story = story;

            addSet("home", home);
            addSet("clicked", clicked);
            addSet("dismiss", dismiss);
        }

        @Override
        public void init() {
            addGeometry(getFrontImageTile());
            addGeometry(getBackImageTile());
            getNode().setRenderQueueMode(Renderer.QUEUE_OPAQUE);

            pointCount++;

            addActionHandler(new ActionHandler() {

                public void performAction(CPoint cp, InputActionEvent evt) {
                    if (MouseInput.get().isButtonDown(0)) {
                        add("clicked");
                    } else if (MouseInput.get().isButtonDown(1)) {
                        // if this is a sim story, make it be the current
                        // story, otherwise dismiss it

                        if (findAndClearFromNeighbors(TileStoryPoint.this)) {
                            addSet("home", home);
                            setCurrentStoryPoint(TileStoryPoint.this);
                        } else {
                            add("dismiss");
                            if (getCurrentStoryPoint() == TileStoryPoint.this) {
                                setCurrentStoryPoint(null);
                            }
                        }
                    }
                }
            });

        }

        TileStoryPoint(Story story, int which) {
            this(story, 0, 0, 0);

            Vector3f spot;

            spot = spots[which % spots.length];

            Command[] spothome = {
                new CmdControl(true),
                new CmdVeryStiff(),
                new CmdMove(spot),
                new CmdRotate(0, FastMath.PI, 0),
                new CmdWait(.5f),
                new CmdRotate(0, 0, 0),
                new CmdWait(.5f)
            };

            System.out.println("Adding set for " + which);
            addSet("home", spothome);
        }

        Geometry getFrontImageTile() {
            return createImageTile(getFrontTitle(), getFrontHTML(), 0);
        }

        Geometry getBackImageTile() {
            return createImageTile(getBackTitle(), getBackHTML(), -.011f);
        }

        @Override
        public void findStories() {
            storyManager.findSimilar(story, tileCloud.length);
        }

        @Override
        public void findTags() {
            storyManager.getTagInfo(story, tileCloud.length);
        }

        @Override
        public void open() {
            Util.openInBrowser(story.getUrl());
        }

        Geometry createImageTile(String title, String html, float offset) {
            Box box = new Box("tilebox", new Vector3f(0, 0, offset), 1, 1f, .01f);
            //Disk box = new Disk("",  10, 10,  1);
            //box.getLocalTranslation().set(0,0,offset);

            BufferedImage image = renderHTML(html, title, width, height, getBackgroundColor());
            box.setRenderState(imageToTextureState(image));
            applyStandardAttributes(box);
            return box;
        }

        String getFrontTitle() {
            return null;
        }

        String getBackTitle() {
            return "Tag Info";
        }

        Color getBackgroundColor() {
            return Color.WHITE;
        }

        String getFrontHTML() {
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
                    imgHtml + getStarRatingHtml(story) + description + "<p> From <b>" + story.getSource() + "</b>" + "</body><html>";
            return html;

        }

        String getBackHTML() {
            StringBuilder sb = new StringBuilder();
            sb.append("<body>");

            if (story.getTopTerms().size() > 0) {
                sb.append("<h2> Top Terms </h2>");
                for (ScoredString c : story.getTopTerms()) {
                    sb.append(getScoredStringForCloud(c));
                    sb.append("  ");
                }
            }

            if (story.getTags().size() > 0) {
                sb.append("<h2> Manual Tags </h2>");
                for (ScoredString c : story.getTags()) {
                    sb.append(c.getName());
                    sb.append("  ");
                }
            }

            if (story.getAutotags().size() > 0) {
                sb.append("<h2> Auto Tags </h2>");
                for (ScoredString c : story.getAutotags()) {
                    sb.append(getScoredStringForCloud(c));
                    sb.append("  ");
                }
            }
            sb.append("</body>");
            return sb.toString();
        }
    }

    class TagInfoTileStoryPoint extends TileStoryPoint {

        private TagInfo tagInfo;

        TagInfoTileStoryPoint(Story story, TagInfo ti, int which) {
            super(story, which);
            tagInfo = ti;
        }

        String getFrontTitle() {
            return tagInfo.getTagName() + " Info";
        }

        String getBackTitle() {
            return tagInfo.getTagName() + " Details";
        }

        String getFrontHTML() {
            StringBuilder sb = new StringBuilder();
            sb.append("<body>");

            if (tagInfo.getDocTerms().size() > 0) {
                sb.append("<h2> Story Terms </h2>");
                for (ScoredString t : tagInfo.getDocTerms()) {
                    sb.append(getScoredStringForCloud(t));
                    sb.append("  ");
                }
            }

            sb.append("</body>");
            return sb.toString();
        }

        @Override
        String getBackHTML() {
            StringBuilder sb = new StringBuilder();
            sb.append("<body>");

            if (tagInfo.getTopTerms().size() > 0) {
                sb.append("<h2> Top Autotag Terms </h2>");
                for ( ScoredString t : tagInfo.getTopTerms()) {
                    sb.append(getScoredStringForCloud(t));
                    sb.append("  ");
                }
            }
            sb.append("</body>");
            return sb.toString();
        }

        @Override
        Color getBackgroundColor() {
            return new Color(220, 220, 250);
        }

        @Override
        public void findStories() {
            storyManager.getStoriesSimilarToTag(tagInfo.getTagName(), tileCloud.length);
        }

        @Override
        public void findTags() {
            storyManager.getTagsSimilarToTag(tagInfo.getTagName(), tileCloud.length);
        }
    }

    private String getScoredStringForCloud(ScoredString c) {
        int size = (int) (c.getScore() * 2 + 1);
        return htmlSize(c.getName(), size);
    }

    private TextureState imageToTextureState(BufferedImage image) {
        Texture texture = TextureManager.loadTexture(image, Texture.MM_LINEAR, Texture.FM_LINEAR, true);
        TextureManager.deleteTextureFromCard(texture);
        //TextureManager.clearCache(); // don't trust the cache
        TextureState ts = display.getRenderer().createTextureState();
        ts.setEnabled(true);
        ts.setTexture(texture);
        return ts;
    }

    private BufferedImage renderHTML(String html, String title, int width, int height, Color bgcolor) {
        JEditorPane htmlDisplay = new JEditorPane();
        htmlDisplay.setEditable(false);
        htmlDisplay.setContentType("text/html");
        attachBorder(htmlDisplay, title);
        htmlDisplay.setText(html);
        htmlDisplay.setPreferredSize(new Dimension(width, height));
        htmlDisplay.setBounds(0, 0, width, height);
        htmlDisplay.setBackground(bgcolor);
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

    private String getStarRatingHtml(Story story) {
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
    private Command[] clicked = {new CmdRotateRelative(0, FastMath.PI, 0, .5f)};
    private Command[] home = {
        new CmdControl(true),
        new CmdVeryStiff(),
        new CmdMove(0, 0, 45),
        new CmdRotate(0, FastMath.PI, 0, .5f),
        new CmdWait(.5f),
        new CmdRotate(0, 0, 0),
        new CmdWait(.5f)
    };
    private Command[] dismiss = {
        new CmdControl(true),
        new CmdSloppy(),
        new CmdWait(0, .3f),
        new CmdMove(0, 0, -245),
        new CmdRotate(FastMath.PI, FastMath.PI / 2, FastMath.PI, .5f),
        new CmdWait(.5f),
        new CmdRotate(FastMath.PI / 2, FastMath.PI, FastMath.PI, 1.5f),
        new CmdWait(1.5f),
        new CmdWait(5f),
        new CmdRemove()
    };
    Vector3f[] spots = {
        new Vector3f(0f, 0f, 45f),

        new Vector3f(-2.1f, 0, 45),
        new Vector3f(2.1f, 0, 45),
        new Vector3f(0, -2.1f, 45),
        new Vector3f(0, 2.1f, 45),
        new Vector3f(-2.1f, -2.1f, 45),
        new Vector3f(-2.1f, 2.1f, 45),
        new Vector3f(2.1f, -2.1f, 45),
        new Vector3f(2.1f, 2.1f, 45)
    };


    class Orbiter extends Node {

        private Vector3f center;
        private Vector3f rotationAxis = new Vector3f(
                1, (float) (.5f - Math.random() * 1f), 0);
        private float rps = FastMath.PI + (float) (Math.random() * FastMath.PI);
        private float curAngle = 0;

        Orbiter(Vector3f center, float radius, float size, boolean sphere) {
            this.center = center;
            setLocalTranslation(center);

            Geometry geometry;
            if (sphere) {
                //geometry = new Sphere("", new Vector3f(0, radius, 0), 20, 20, size);
                geometry = new Box("", new Vector3f(0, radius, 0), size, size, size);
                rps *= -.5f; // slow and backwards
            } else {
                radius *= 2;
                geometry = new Box("", new Vector3f(0, radius, 0), size, size, size);
            }

            MaterialState ms = display.getRenderer().createMaterialState();
            ColorRGBA color = ColorRGBA.randomColor();
            ms.setAmbient(color);
            ms.setDiffuse(color);

            /* has been depricated */
            //ms.setAlpha(1f);

            ms.setEnabled(true);
            geometry.setRenderState(ms);
            geometry.setRenderState(lightState);


            /*
            //box.setDefaultColor(new ColorRGBA(.5f, .9f, .5f, 1.f));
            geometry.setDefaultColor(new ColorRGBA(.5f, .9f, .5f, 1.f));
            geometry.setSolidColor(ColorRGBA.randomColor());
            geometry.setLightCombineMode(LightState.OFF);
             */

            attachChild(geometry);
            addController(new Rotator());
        }

        class Rotator extends Controller {

            @Override
            public void update(float time) {
                curAngle += time * rps;
                if (curAngle >= FastMath.PI * 2) {
                    curAngle -= FastMath.PI * 2;
                }
                getLocalRotation().fromAngleAxis(curAngle, rotationAxis);
            }
        }
    }
}

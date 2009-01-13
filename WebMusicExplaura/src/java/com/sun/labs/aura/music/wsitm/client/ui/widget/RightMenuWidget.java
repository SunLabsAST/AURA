/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.HoverListener;
import com.sun.labs.aura.music.wsitm.client.ui.SpannedFlowPanel;
import java.util.HashSet;

/**
 *
 * @author mailletf
 */
//public class RightMenuWidget <T extends Widget> extends AnimatedComposite
public class RightMenuWidget <T extends Widget> extends Composite
        implements SourcesMouseEvents {

    private Panel mainPanel; // panel containing the main widget and right menu
    private Panel rightMenu;
    protected T w; // main widget
    
    protected boolean isHovering = false;

    private double lastRightMenuHeight=-1;

    private HashSet<Widget> rightMenuWidgets;
    
    private MouseListenerCollection mouseListeners;
    private HoverListenerManager hoverListenerManager;
    
    public RightMenuWidget(T w) {
        init(w, new SpannedFlowPanel());
    }

    public RightMenuWidget(T w, Panel mainPanel) {
        init(w, mainPanel);
    }
    
    private void init(T w, Panel mainPanel) {
        this.w = w;
        this.mainPanel = mainPanel;
        this.rightMenu = new SpannedFlowPanel();
        //this.rightMenu = new SpannedVerticalPanel();

        mainPanel.add(w);
        mainPanel.add(rightMenu);
        initWidget(mainPanel);
        
        rightMenuWidgets = new HashSet<Widget>();
        
        hoverListenerManager = new HoverListenerManager();
        
        addMouseListener(new MouseListener() {

            public void onMouseEnter(Widget arg0) {
                isHovering = true;
                hoverListenerManager.triggerOnMouseHover();
            }

            public void onMouseLeave(Widget arg0) {
                isHovering = false;
                hoverListenerManager.triggerOnMouseOut();
                Timer t = new Timer() {
                    public void run() {
                        if (!isHovering) {
                            hoverListenerManager.triggerOnOutTimer();
                        }
                    }
                };
                t.schedule(250);
            }
            
            public void onMouseDown(Widget arg0, int arg1, int arg2) {}
            public void onMouseMove(Widget arg0, int arg1, int arg2) {}
            public void onMouseUp(Widget arg0, int arg1, int arg2) {}
        });
    }

    /**
     * Set the right menu's bottom spacing
     * @param px
     */
    public void setRightMenuHeight(double px) {
        lastRightMenuHeight = px;
        for (Widget tw : rightMenuWidgets) {
            if (tw instanceof com.sun.labs.aura.music.wsitm.client.ui.widget.SwapableWidget) {
                ((SwapableWidget)tw).getWidget1().getElement().getStyle().setPropertyPx("marginBottom", (int)px);
                ((SwapableWidget)tw).getWidget2().getElement().getStyle().setPropertyPx("marginBottom", (int)px);
            } else {
                tw.getElement().getStyle().setPropertyPx("marginBottom", (int)px);
            }
        }
    }
    
    /**
     * Returns the contained widget
     * @return
     */
    @Override
    public T getWidget() {
        return w;
    }
    
    public void addWidgetToRightMenu(Widget wToAdd) {
        addWidgetToRightMenu(wToAdd, null);
    }
    
    public void addWidgetToRightMenu(Widget wToAdd, HoverListener hL) {
        rightMenu.add(wToAdd);
        rightMenuWidgets.add(wToAdd);
        if (hL != null) {
            hoverListenerManager.addHoverListener(hL);
        }

        // Set height of newly added widget
        if (lastRightMenuHeight!=-1) {
            setRightMenuHeight(lastRightMenuHeight);
        }
    }

    public void addMouseListener(MouseListener listener) {
        if (mouseListeners == null) {
            mouseListeners = new MouseListenerCollection();
            sinkEvents(Event.MOUSEEVENTS);
        }
        mouseListeners.add(listener);
    }

    public void removeMouseListener(MouseListener listener) {
        if (mouseListeners != null) {
            mouseListeners.remove(listener);
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        switch (DOM.eventGetType(event)) {

            case Event.ONMOUSEDOWN:
            case Event.ONMOUSEUP:
            case Event.ONMOUSEMOVE:
            case Event.ONMOUSEOVER:
            case Event.ONMOUSEOUT:
                if (mouseListeners != null) {
                    mouseListeners.fireMouseEvent(this, event);
                }
                break;
        }
    }
    
    private class HoverListenerManager {
        
        private HashSet<HoverListener> hoverListeners;
        
        public void addHoverListener(HoverListener hL) {
            if (hoverListeners == null) {
                hoverListeners = new HashSet<HoverListener>();
            }
            hoverListeners.add(hL);
        }
        
        public void removeHoverListener(HoverListener hL) {
            if (hoverListeners != null) {
                hoverListeners.remove(hL);
            }
        }
        
        public void triggerOnMouseHover() {
            if (hoverListeners != null) {
                for (HoverListener hL : hoverListeners) {
                    hL.onMouseHover();
                }
            }
        }
        
        public void triggerOnMouseOut() {
            if (hoverListeners != null) {
                for (HoverListener hL : hoverListeners) {
                    hL.onMouseOut();
                }
            }
        }
        
        public void triggerOnOutTimer() {
            if (hoverListeners != null) {
                for (HoverListener hL : hoverListeners) {
                    hL.onOutTimer();
                }
            }
        }
    }

    public class SpannedVerticalPanel extends VerticalPanel {

        public SpannedVerticalPanel() {
            setElement(DOM.createSpan());
        }
    }
}
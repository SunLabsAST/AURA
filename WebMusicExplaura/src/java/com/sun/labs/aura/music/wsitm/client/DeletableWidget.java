
package com.sun.labs.aura.music.wsitm.client;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.MouseListenerCollection;
import com.google.gwt.user.client.ui.SourcesMouseEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public abstract class DeletableWidget <T extends Widget> extends Composite
        implements SourcesMouseEvents {

    protected FlowPanel fP;
    protected T w;
    protected Image xB;
    protected boolean isHovering = false;

    private MouseListenerCollection mouseListeners;

    public DeletableWidget(T w) {
        super();

        this.w = w;
        fP = new SpannedFlowPanel();


        fP.add(w);

        xB = new Image("green-x.jpg");
        xB.addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                onDelete();
            }
        });
        xB.getElement().setAttribute("style", "vertical-align:top; display:none; margin-top: 3px;");
        fP.add(xB);

        initWidget(fP);

        this.addMouseListener(new MouseListener() {

            public void onMouseDown(Widget arg0, int arg1, int arg2) {
            }

            public void onMouseEnter(Widget arg0) {
                isHovering = true;
                xB.setVisible(true);
            }

            public void onMouseLeave(Widget arg0) {
                Timer t = new Timer() {

                    public void run() {
                        if (!isHovering) {
                            xB.setVisible(false);
                        }
                    }
                };
                t.schedule(100);
                isHovering = false;
            }

            public void onMouseMove(Widget arg0, int arg1, int arg2) {
            }

            public void onMouseUp(Widget arg0, int arg1, int arg2) {
            }
        });

    }

    /**
     * Returns the contained widget
     * @return
     */
    public T getWidget() {
        return w;
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

    /**
     * Called when the widget's delete button is clicked
     */
    public abstract void onDelete();

    public class SpannedFlowPanel extends FlowPanel {

        public SpannedFlowPanel() {
            setElement(DOM.createSpan());
        }
    }
}

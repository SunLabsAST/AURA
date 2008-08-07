
package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.MouseListener;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SourcesClickEvents;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author mailletf
 */
public abstract class DeletableWidget <T extends Widget> extends RightMenuWidget<T> {

    protected SwapableImage xB; // close button

    public DeletableWidget(T w) {
        super(w);
        doDefaultXButtonConstruct();
    }

    public DeletableWidget(T w, Panel mainPanel) {
        super(w, mainPanel);
        doDefaultXButtonConstruct();
    }

    private void doDefaultXButtonConstruct() {
        xB = new SwapableImage("green-x.jpg","green-x-hidden.jpg");
        xB.setImg2();
        xB.addStyleName("image");
    }
  
    public DeletableWidget(T w, Panel mainPanel, SwapableImage xButton) {
        super(w, mainPanel);
        this.xB = xButton;
    }

    /**
     * Must be called to add the remove button
     */
    public void addRemoveButton() {
        
        super.addWidgetToRightMenu(xB, new HoverListener<SwapableImage>(xB) {

            @Override
            void onMouseHover() {
                data.setImg1();
            }

            @Override
            void onOutTimer() {
                data.setImg2();
            }
            
            @Override
            void onMouseOut() {}
        });
        
        ((SourcesClickEvents)xB).addClickListener(new ClickListener() {

            public void onClick(Widget arg0) {
                onDelete();
            }
        });
    }

    /**
     * Called when the widget's delete button is clicked
     */
    public abstract void onDelete();

}

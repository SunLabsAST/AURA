
package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.sun.labs.aura.music.wsitm.client.event.HoverListener;

/**
 *
 * @author mailletf
 */
public abstract class DeletableWidget <T extends Widget> extends RightMenuWidget<T> {

    protected SwapableWidget<Image, Image> xB; // close button

    public DeletableWidget(T w) {
        super(w);
        doDefaultXButtonConstruct();
    }

    public DeletableWidget(T w, Panel mainPanel) {
        super(w, mainPanel);
        doDefaultXButtonConstruct();
    }

    private void doDefaultXButtonConstruct() {
        xB = new SwapableWidget<Image, Image>(new Image("green-x.jpg"), new Image("green-x-hidden.jpg"));
        xB.getWidget1().setTitle("Click to delete this tag");
        xB.showWidget(SwapableWidget.LoadableWidget.W2);
        xB.addStyleName("image");
    }
  
    public DeletableWidget(T w, Panel mainPanel, SwapableWidget xButton) {
        super(w, mainPanel);
        this.xB = xButton;
    }

    /**
     * Must be called to add the remove button
     */
    public void addRemoveButton() {
        
        super.addWidgetToRightMenu(xB, new HoverListener<SwapableWidget>(xB) {

            @Override
            public void onMouseHover() {
                data.showWidget(SwapableWidget.LoadableWidget.W1);
            }

            @Override
            public void onOutTimer() {
                data.showWidget(SwapableWidget.LoadableWidget.W2);
            }
            
            @Override
            public void onMouseOut() {}
        });

        xB.getWidget1().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent ce) {
                onDelete();
            }
        });
    }

    /**
     * Called when the widget's delete button is clicked
     */
    public abstract void onDelete();

}

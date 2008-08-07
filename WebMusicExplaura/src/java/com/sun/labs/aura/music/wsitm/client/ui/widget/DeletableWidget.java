
package com.sun.labs.aura.music.wsitm.client.ui.widget;

import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

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
            void onMouseHover() {
                data.showWidget(SwapableWidget.LoadableWidget.W1);
            }

            @Override
            void onOutTimer() {
                data.showWidget(SwapableWidget.LoadableWidget.W2);
            }
            
            @Override
            void onMouseOut() {}
        });

        xB.getWidget1().addClickListener(new ClickListener() {

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

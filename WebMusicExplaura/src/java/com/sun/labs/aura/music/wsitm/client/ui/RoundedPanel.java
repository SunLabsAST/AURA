/*
 * Copyright 2007-2008 Hilbrand Bouwkamp, hs@bouwkamp.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * Taken from : http://code.google.com/p/cobogw/
 * Version 1.1.1
 * 
 */

package com.sun.labs.aura.music.wsitm.client.ui;

import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The <code>RoundedPanel</code> class adds similar rounded corners, as seen
 * in other Google applications, to a widget. This is done by adding several
 * <code>div</code> Elements around a Widget. The HTML code roughly for a
 * corner height 2 looks as follows:
 *
 * <pre>
 * &lt;div&gt;
 *   &lt;div style=&quot;margin:0 2px&quot;&gt;&lt;/div&gt;
 *   &lt;div style=&quot;margin:0 1px&quot;&gt;&lt;/div&gt;
 *   &lt;div&gt;your widget&lt;/div&gt;
 *   &lt;div style=&quot;margin:0 1px&quot;&gt;&lt;/div&gt;
 *   &lt;div style=&quot;margin:0 2px&quot;&gt;&lt;/div&gt;
 * &lt;/div&gt;
 * </pre>
 *
 * To add the rounded corners to a widget start simply wrap your own widget
 * in the <code>RoundedPanel</code> class, for example with all corners
 * rounded:
 *
 * <pre>
 * // all 4 corners are rounded.
 * RoundedPanel rp = new RoundedPanel(yourWidget);
 * // use rp where you would use 'yourWidget' otherwise
 * </pre>
 *
 * Or with custom set corners, like only on the left:
 *
 * <pre>
 * // custom set corners
 * RoundedPanel rp = new RoundedPanel(yourWidget, RoundedPanel.LEFT);
 * // use rp where you would use 'yourWidget' otherwise
 * </pre>
 *
 * By default the height of the corners is 2px. It is possible to set
 * a different height at construction time. The height can be a value
 * between and including 1 and 9. This value doesn't correspond exactly
 * with the height, e.g. 9 is 12px height.
 *
 * <pre>
 * // all 4 corners are rounded and height index 5
 * RoundedPanel rp = new RoundedPanel(yourWidget, ALL, 5);
 * // use rp where you would use 'yourWidget' otherwise
 * </pre>
 *
 * There are 2 ways to set the color on the border programmatically. First via
 * {@link #setCornerColor(String)} and second via
 * {@link #setBorderColor(String)}. The second method will also add a solid
 * border style in the given color on the left and/or right of the widget. The
 * first method doesn't set a border style. Use the first method when you don't
 * want to have a border or when the background color of your widget is the same
 * as the background color of the rounded corners or if you wan to have the
 * flexibility to set the border via CSS.
 *
 * Example to set the color of the corners with {@link #setCornerColor(String)}:
 *
 * <pre>
 * // all 4 corners are rounded.
 * RoundedPanel rp = new RoundedPanel(yourWidget);
 * rp.setCornerColor(&quot;red&quot;);
 * </pre>
 *
 * To add also a solid border style to the left and/or right of the widget use
 * {@link #setBorderColor(String)}:
 *
 * <pre>
 * // all 4 corners are rounded and solid border style left and right of widget
 * RoundedPanel rp = new RoundedPanel(yourWidget);
 * rp.setBorderColor(&quot;red&quot;);
 * </pre>
 *
 * Default the CSS style name of the rounded corner divs is
 * <code>cbg-RP</code>. Use it to set the colors of the corner. For example:
 *
 * <pre>
 * .cbg-RP { background-color:#c3d9ff; }
 * </pre>
 *
 * A custom style might be needed when the corners are visible only when a panel
 * is selected (see the KitchenSink example modification below). Use the
 * <code>setCornerStyleName</code> method to set the custom style name. For
 * example set a custom style <code>my-RP</code> and add something like the
 * following to the style sheet:
 *
 * <pre>
 * .selected .my-RP { background-color:#c3d9ff; }
 * </pre>
 *
 * <h3>Adding rounded corners to DialogBox widgets</h3>
 * Adding rounded corners to a <code>DialogBox</code> is somewhat more
 * complicated. The problem with <code>DialogBox</code> is that it uses a
 * private panel and the <code>RoundedPanel</code> should be applied to this
 * private <code>Panel</code>. To add the rounded corners to a
 * <code>DialogBox</code> you have to rewrite the implementation of the
 * <code>DialogBox</code>. For the <code>DialogBox</code> widget this can
 * be done as follows. First create a new class like
 * <code>RoundedDialogBox</code> and copy the code from the original
 * <code>DialogBox</code> (Please take the copyright of this class into
 * account). Next make the following changes to the code:
 *
 * <pre>
 * // Private variable for RoundedPanel
 * private RoundedPanel rp;
 * </pre>
 *
 * In the constructor change:
 *
 * <pre>
 * super.add(panel);
 * </pre>
 *
 * into:
 *
 * <pre>
 * rp = new RoundedPanel(panel);
 * super.add(rp);
 * </pre>
 *
 * Next change the style, otherwise the style of the dialog box is applied to
 * the rounded lines too. Do this by changing the following line:
 *
 * <pre>
 * setStyleName(&quot;gwt-DialogBox&quot;);
 * </pre>
 *
 * into:
 *
 * <pre>
 * panel.setStyleName(&quot;gwt-DialogBox&quot;);
 * </pre>
 *
 * In your css add the color of the border, something like:
 *
 * <pre>
 *  .cbg-RP { background-color:#AAAAAA; }
 * </pre>
 *
 * Now by extending your own dialog class on this <code>RoundedDialogBox</code>
 * instead of the original <code>DialogBox</code> you will have rounded
 * corners around your dialog.
 */
public class RoundedPanel extends SimplePanel {

  /**
   * <code>TOPLEFT</code> top left rounded corner
   */
  public final static int TOPLEFT = 1;

  /**
   * <code>TOPRIGHT</code> top right rounded corner
   */
  public final static int TOPRIGHT = 2;

  /**
   * <code>BOTTOMLEFT</code> bottom left rounded corner
   */
  public final static int BOTTOMLEFT = 4;

  /**
   * <code>BOTTOMRIGHT</code> bottom right rounded corner
   */
  public final static int BOTTOMRIGHT = 8;

  /**
   * <code>BOTTOM</code> rounded corners at the top
   */
  public final static int TOP = TOPLEFT | TOPRIGHT;

  /**
   * <code>TOP</code> rounded corners at the bottom
   */
  public final static int BOTTOM = BOTTOMLEFT | BOTTOMRIGHT;

  /**
   * <code>LEFT</code> rounded corners on the left side
   */
  public final static int LEFT = TOPLEFT | BOTTOMLEFT;

  /**
   * <code>RIGHT</code> rounded corners on the right side
   */
  public final static int RIGHT = TOPRIGHT | BOTTOMRIGHT;

  /**
   * <code>ALL</code> rounded corners on all sides
   */
  public final static int ALL = TOP | BOTTOM;

  /**
   * Default border style
   */
  private final static String RPSTYLE = "cbg-RP";

  /**
   * Lookup table for corner border width
   */

  private final static int[][] CORNERBORDER = {
    { 1 }, { 1, 1}, { 1, 1, 1},
    { 1, 1, 1, 1 }, { 1, 1, 1, 2, 1 },
    { 1, 1, 1, 1, 2, 1 },
    { 1, 1, 1, 1, 1, 2, 1 },
    { 1, 1, 1, 1, 1, 2, 2, 1 },
    { 1, 1, 1, 1, 1, 1, 2, 3, 1 }};

  /**
   * Lookup table for corner height
   */
  private final static int[][] CORNERHEIGHT = {
    { 1 }, { 1, 1}, { 1, 1, 1},
    { 1, 1, 1, 1 }, { 2, 1, 1, 1, 1 },
    { 2, 1, 1, 1, 1, 1 },
    { 2, 1, 1, 1, 1, 1, 1 },
    { 2, 1, 1, 1, 1, 1, 1, 1 },
    { 3, 2, 1, 1, 1, 1, 1, 1, 1 }};

  /**
   * Lookup table for corner margin
   */
  private final static int[][] CORNERMARGIN = {
    { 1 }, { 1, 2 }, { 1, 2, 3 },
    { 1, 2, 3, 4 }, { 1, 2, 3, 4, 6 },
    { 1, 2, 3, 4, 5, 7 },
    { 1, 2, 3, 4, 5, 6, 8 },
    { 1, 2, 3, 4, 5, 6, 8, 10 },
    { 1, 2, 3, 4, 5, 6, 7, 9, 12 },
  };

  /**
   * Element array containing all div elements of the top corner div's.
   */
  protected final DivElement[] divt;

  /**
   * Element array containing all div elements of the bottom corner div's.
   */
  protected final DivElement[] divb;

  /**
   * Index of the corner height.
   */
  protected final int cornerHeight;

  /**
   * Index of which corners are set.
   */
  protected final int corners;

  private final DivElement body; // body of widget
  private DivElement divElement; // div element containing widget

  /**
   * Creates a new <code>RoundedPanel</code> with all corners rounded and
   * height of corners 2px. Use <code>setWidget</code> to add the widget.
   */
  public RoundedPanel() {
    this(ALL, 2);
  }

  /**
   * Creates a new <code>RoundedPanel</code> with custom rounding and height
   * of corners 2px but with no widget set. Use <code>setWidget</code> to add
   * widget.
   *
   * Every combination of corners can be set via <code>corners</code>.
   * Use the static constants to set the corners. For example, to create a
   * panel with only rounded corners at the left, use:
   *
   * <code>new RoundedPanel(RoundedPanel.LEFT);</code>
   *
   * @param corners set custom rounded corners
   */
  public RoundedPanel(int corners) {
    this(corners, 2);
  }

  /**
   * Creates a new <code>RoundedPanel</code> with custom rounding and custom
   * height of the corners but with no widget set. Height can be value from
   * 1 to 9. The height for a value 5 or higher is actually larger, e.g. for
   * 5 the height is 6px.
   * Use {@link #setWidget(Widget)} to add widget.
   *
   * @param corners set custom rounded corners
   * @param cornerHeight height index between and including 1 and 9
   * @throws IndexOutOfBoundsException when cornerHeight below 1 or above 9
   */
  public RoundedPanel(int corners, int cornerHeight) {
    super();
    body = getElement().cast();
    if (cornerHeight < 1 || cornerHeight > 9) {
      throw new IndexOutOfBoundsException(
          "RoundedPanel height range is between and including 1 and 9");
    }
    this.cornerHeight = cornerHeight;
    divt = new DivElement[cornerHeight];
    divb = new DivElement[cornerHeight];
    this.corners = corners;
    if (inMask(corners, TOP)) {
      final int ct = corners & TOP;

      for (int i = 0; i < cornerHeight; ++i) {
        divt[i] = addLine(ct, cornerHeight - (i + 1));
      }
    }
    divElement = Document.get().createDivElement();
    body.appendChild(divElement);
    if (inMask(corners, BOTTOM)) {
      final int cb = corners & BOTTOM;

      for (int i = cornerHeight - 1; i >= 0; --i) {
        divb[i] = addLine(cb, cornerHeight - (i + 1));
      }
    }
    setCornerStyleName(RPSTYLE);
  }

  /**
   * Creates a new <code>RoundedPanel</code> with all corners rounded and
   * height 2px on the widget <code>w</code>.
   *
   * @param w widget to add corners to
   */
  public RoundedPanel(Widget w) {
    this(w, ALL);
  }

  /**
   * Creates a new <code>RoundedPanel</code> with custom rounded corners
   * and height 2px on the widget <code>w</code>.
   *
   * @param w widget to add corners to
   * @param corners set custom rounded corners
   */
  public RoundedPanel(Widget w, int corners) {
    this(w, corners, 2);
  }

  /**
   * Creates a new <code>RoundedPanel</code> with custom rounded corners
   * and custom height on widget <code>w</code>.
   *
   * @param w widget to add corners to
   * @param corners set custom rounded corners
   * @param cornerHeight height index between and including 1 and 9
   * @throws IndexOutOfBoundsException when cornerHeight below 1 or above 9
   */
  public RoundedPanel(Widget w, int corners, int cornerHeight) {
    this(corners, cornerHeight);
    setWidget(w);
  }

  /**
   * Set the colors on the rounded borders and adds a border style on the
   * element wrapped around the widget. The width of the border (left and/or
   * right) is the same as the height of the rounding.
   *
   * <p>If NO border style (e.g. lines on the left and/or right side) for the
   * wrapped widget is desired use {@link #setCornerColor(String)}.
   *
   * @param borderColor color of the border
   */
  public void setBorderColor(String borderColor) {
    setCornerColor(borderColor);
    setBorderContainer(borderColor,
        CORNERMARGIN[cornerHeight-1][cornerHeight-1]);
  }

  /**
   * Set the color on the rounded corners. When a different background color for
   * the widget is used, this will display as a block with rounded corners at
   * the top or bottom (depending on the rounding) in the given borderColor.
   *
   * <p>If border style (e.g. lines on the left and/or right side) for the
   * wrapped widget IS desired use {@link #setBorderColor(String)}.
   *
   * @param borderColor border color
   */
  public void setCornerColor(String borderColor) {
    if (null != divt[0]) {
      for (int i = 0; i < cornerHeight; ++i) {
        divt[i].getStyle().setProperty("backgroundColor", borderColor);
      }
    }
    if (null != divb[0]) {
      for (int i = 0; i < cornerHeight; ++i) {
        divb[i].getStyle().setProperty("backgroundColor", borderColor);
      }
    }
  }

  /**
   * Set the CSS style name of the rounded corners div's. Default the CSS style
   * name is <code>cbg-RP</code>. Use it to set the colors of the corner. For
   * example: <code>.cbg-RP { background-color:#c3d9ff; }</code>.
   *
   * A custom style might be needed when the corners are visible only when a
   * panel is selected. Use this method to set the custom style name and add
   * something like the following to the style sheet:
   *
   * <code>.selected .cbg-RP { background-color:#c3d9ff; }</code>
   * Setting the color is also possible via the method
   * {@link #setCornerColor(String)}.
   *
   * @param style CSS style name
   */
  public void setCornerStyleName(String style) {
    if (null != divt[0]) {
      for (int i = 0; i < cornerHeight; ++i) {
        divt[i].setClassName(style);
      }
    }
    if (null != divb[0]) {
      for (int i = 0; i < cornerHeight; ++i) {
        divb[i].setClassName(style);
      }
    }
  }

  /**
   * Overwrite of parent getContainerElement()
   */
  @Override
  protected com.google.gwt.user.client.Element getContainerElement() {
    return divElement.cast();
  }

  /**
   * Convenience method to check if given <code>input</code> is with
   * the <code>mask</code>.
   *
   * @param input input to check
   * @param mask mask to check against
   * @return true if input within mask
   */
  protected boolean inMask(int input, int mask) {
    return (input & mask) > 0;
  }

  /**
   * Set the left and/or right border on the container element.
   *
   * @param borderColor color of the border
   * @param borderWidth width of the border
   */
  protected void setBorderContainer(String borderColor, int borderWidth) {
    setBorder(getContainerElement(), corners, borderColor);
    getContainerElement().getStyle().setProperty(
        "borderWidth", "0 " + borderWidth + "px");
 }

  /**
   * Set the border left and or right style and color attributes on the element.
   *
   * @param elem element to set the style attributes
   * @param corners corners to set the style attributes
   * @param borderColor color of the border
   */
  protected void setBorder(Element elem, int corners, String borderColor) {
    if (inMask(corners, LEFT)) {
      elem.getStyle().setProperty("borderLeftStyle", "solid");
      elem.getStyle().setProperty("borderLeftColor", borderColor);
    }
    if (inMask(corners, RIGHT)) {
      elem.getStyle().setProperty("borderRightStyle", "solid");
      elem.getStyle().setProperty("borderRightColor", borderColor);
    }
  }

  /**
   * Creates div element representing part of the rounded corner.
   *
   * @param corner corner mask to set rounded corner
   * @param heightIndex margin width for line
   */
  private DivElement addLine(int corner, int heightIndex) {
    // margin 4 fields : top right bottom left  => "0 <width>px 0 <width>px"
    String mw = CORNERMARGIN[cornerHeight - 1][heightIndex] + "px ";
    String margin =
        "0 " + (inMask(corner, RIGHT) ? mw : "0 ") +
        "0 " + (inMask(corner, LEFT) ? mw : "0");
    DivElement div = Document.get().createDivElement();

    div.getStyle().setPropertyPx("fontSize", 0);
    div.getStyle().setPropertyPx("height",
        CORNERHEIGHT[cornerHeight - 1][heightIndex]);
    div.getStyle().setProperty("borderWidth",
        "0 " + CORNERBORDER[cornerHeight - 1][heightIndex] + "px");
    div.getStyle().setPropertyPx("lineHeight",
        CORNERHEIGHT[cornerHeight - 1][heightIndex]);
    div.getStyle().setProperty("margin", margin);
    div.setInnerHTML("&nbsp;");
    body.appendChild(div);
    return div;
  }
}


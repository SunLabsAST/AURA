*******************************
** Web Music Explaura README **
*******************************

1) Introduction
2) Application structure
3) 3rd party GWT libraries
4) 


Introduction
****************
State of the application as of end of August 2008. The GWT version used is 1.5RC2.

Application structure
****************

## Swidgets

Each section of the webapp extends the Swidget (SectionWidget) class and is placed in the com.sun.labs.aura.music.wsitm.client.ui.swidget package. The Swidgets must implement methods such as getTokenHeaders() that will allow the main window to determine which swigdet to display on a history change. Another important function is getMenuItem() which must return a MenuItem if the swidget requires that a link to be displayed on the top of the page by the PageHeaderWidget.


## ClientDataManager

The ClientDataManager (cdm) is responsible for storing any cross swidget data. It stores the oracles (suggest boxes for artists and tags), the shared context menus, the user information of a logged in user and the event managers. 


## SharedContextMenus

In order to improve performance, some context menus are shared between all widgets that use the same one. For example, the tags. This is accomplished by storing one instance of the menu in the cdm and setting a reference to the widget being clicked on in the onClick event. The actions needed to be performed will then be carried out in the context of the widget referenced in the menu.


## Event managers

Because all swidgets are loaded simultaneously and widgets contained within them may need to perform an action triggered by an event happening on another swidget, an event managment system was implemented in the cdm. The listeners currently implemented are TagCloudListener, TaggingListener, RatingListener, PlayedListener, LoginListener.

When a widget registers itself with a listener (or contains a widget that does) and is removed by it's parent swidget, it must unregister itself with the listener or this will introduce a memory leak. Widgets containing listening-widgets must implement the HasListeners interface. 


# SteerableTagCloudExternalController

Accessible through the cdm and serves as a bridge between any part of the application and the steerable tag cloud. This is for example used to add tags  to the steerable interface from context menus.


# Steerable recommendations

TODO


3rd party GWT libraries
****************

- Ext GWT, http://mygwt.net (GNU GPL license v3)
- GWT-Ext, http://gwt-ext.com (GNU Lesser General Public Licence (LGPL), v 3.0)
- gwt-fx, http://code.google.com/p/gwt-fx (Apache License 2.0)
- gwt-tk, http://code.google.com/p/gwt-tk (Apache License 2.0)

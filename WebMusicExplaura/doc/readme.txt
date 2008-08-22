*******************************
** Web Music Explaura README **
*******************************

1) Introduction
2) Building info
3) Application structure
4) 3rd party GWT libraries
5) Why use specific types?


Introduction
****************
State of the application as of end of August 2008. The GWT version used is 1.5RC2.


Building info
****************
As of Netbeans 6.5Beta, the build on save feature prevents the "Build" option. I have not found a way to disable to build on save feature on a web project as it is possible to do with a normal app. So to build the project, you need to run "ant" without any arguments from the WebMusicExplaura/ folder.

Furthermore, GWT1.5's compiler requires a big heap and you will get a 'Exception in thread "main" java.lang.OutOfMemoryError: Java heap space' error when building if the following isn't added to nbproject/build-gwt.xml.

Replace 
        <java classpath="${javac.classpath}:${src.dir}" failonerror="true"
              classname="com.google.gwt.dev.GWTCompiler" fork="true">

by
        <java classpath="${javac.classpath}:${src.dir}" failonerror="true"
              classname="com.google.gwt.dev.GWTCompiler" fork="true" maxmemory="1G">


You must redo this each time you open the project in NetBeans.


Application structure
****************

Some notes about the structure...

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

Accessible through the cdm and serves as a bridge between any part of the application and the steerable tag cloud. This is for example used to add tags to the steerable interface from context menus around the app.



3rd party GWT libraries
****************

- Ext GWT, http://mygwt.net (GNU GPL license v3)
- GWT-Ext, http://gwt-ext.com (GNU Lesser General Public Licence (LGPL), v 3.0)
- gwt-fx, http://code.google.com/p/gwt-fx (Apache License 2.0)
- gwt-tk, http://code.google.com/p/gwt-tk (Apache License 2.0)


Why use specific types
****************
"In terms of optimizations for the GWT compiler, it is good to be as specific as possible when specifying the types of your fields in the data object. For example it is common practice to specify java.util.List as a type instead of either ArrayList or Vector. The benefit of using a generalized type is that it allows you to change the underlying implementation without changing the type declaration. The problem is that when you generalize the type it is harder for the GWT compiler to optimize the code, and you often end up with larger JavaScript files. So the rule of thumb it to try to be as specific as possible in your typing."

This was not done in the beginning so there is still a lot of conversion left to do to

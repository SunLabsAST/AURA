<%-- 
    Document   : index
    Created on : Nov 15, 2007, 8:21:40 PM
    Author     : plamere
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>TasteBroker.org </title>
        <link rel="stylesheet" href="styles.css">
        <script type="text/javascript" src="niftycube.js"></script>
        <script type="text/javascript">
            window.onload=function(){
                Nifty("div.nested1","big");
                Nifty("div.nested2","big");
                Nifty("div.nested3","big");
            }
        </script>
    </head>
    <body>
        <div class="nested1">
            <h1>Welcome to TasteBroker.org</h1>
            TasteBroker is a set of experimental web services for generating and processing
            taste data.  TasteBroker currently supplies two types of web services:
            <ul>
                <li> <a href="#generation">APML Generation Web Services</a> - services that generate an <a href="http://www.apml.org">APML</a> 
                representation of your taste data </li>
                <li> <a href="#recommendation">Recommendation Web Services</a> - services that generate recommendations based upon your taste data. </li>
            </ul>
            
            <div class="nested2">
                <h2> <a name="generation">APML Generation Web Services</a> </h2>
                These web services provide an APML representation for a user's taste data based on data supplied from various web destinations.
                
                <div class="nested3">
                    <h3> APML from last.fm</h3>
                    If you are a last.fm user, you can get a representation of your 
                    music listening taste, in the form of an APML file. Simply use the 
                    webservice:
                    
                    <p>
                    
                    http://aura.darkstar.sunlabs.com/apml/music/Your_last.fm_name
                    
                    <p>
                    
                    Some examples:
                    <ul>
                        <li><a href="apml/last.fm/lamere"> Paul's APML File</a>
                        <li><a href="apml/last.fm/rj"> RJ's APML File</a>
                        <li><a href="apml/last.fm/musicmobs"> Toby's APML File</a>
                        <li><a href="apml/last.fm/ocelma"> Oscar's APML File</a>
                    </ul>
                    <p>
                    Try it yourself:
                    <div class="tryit">
                        http://aura.darkstar.sunlabs.com/apml/last.fm/<input type="text" 
                                                                             onchange="window.location = 'apml/last.fm/' + this.value;" />
                    </div>
                    <p>
                    Note that it can take a while to generate your APML file since it requires a number
                    of queries to last.fm to collect your taste data.  We try to cache as much info as we can
                    to make things quick, but if you have eclectic tastes, you might have to wait a minute 
                    or so.
                </div>
                
                <div class="nested3">
                    <h3> APML from Pandora</h3>
                    If you are a Pandora user, you can get a representation of your 
                    music listening taste, in the form of an APML file. Simply use the 
                    webservice:
                    
                    <p>
                    
                    http://aura.darkstar.sunlabs.com/apml/pandora/Your_Pandora_name
                    
                    <p>
                    
                    Some examples:
                    <ul>
                        <li><a href="apml/pandora/paul.lamere"> Paul's APML File</a>
                        <li><a href="apml/pandora/tim"> Tim Westergren's APML File</a>
                        <li><a href="apml/pandora/tconrad"> Tom Conrad's APML File</a>
                    </ul>
                    <p>
                    Try it yourself:
                    <div class="tryit">
                        http://aura.darkstar.sunlabs.com/apml/pandora/<input type="text" 
                                                                             onchange="window.location = 'apml/pandora/' + this.value;" />
                    </div>
                    <p>
                    Note that it can take a while to generate your APML file since it requires a number
                    of queries to Pandora and last.fm to collect your taste data.  We try to cache as much info as we can
                    to make things quick, but if you have eclectic tastes, you might have to wait a minute 
                    or so.
                </div>
                <div class="nested3">
                    <h3> APML from del.icio.us</h3>
                    If you are a del.icio.us user, you can get a representation of your 
                    web browsing taste, in the form of an APML file. Simply use the 
                    webservice:
                    
                    <p>
                    
                    http://aura.darkstar.sunlabs.com/apml/web/Your_delicious_name
                    
                    <p>
                    
                    Some examples:
                    <ul>
                        <li><a href="apml/web/plamere"> Paul's APML File</a>
                        <li><a href="apml/web/toby"> Toby's APML File</a>
                        <li><a href="apml/web/tristanf"> Tristan's APML File</a>
                    </ul>
                    <p>
                    Try it yourself:
                    <div class="tryit">
                        http://aura.darkstar.sunlabs.com/apml/web/<input type="text" 
                                                                         onchange="window.location = 'apml/web/' + this.value;" />
                    </div>
                </div>
            </div>
            <p>
            <p>
            <div class="nested2">
                <h2> <a name="recommendation">Recommendation Web Service </a> </h2>
                This is an experimental web service that provides music recommendations based
                upon the APML representation of your taste.  This web service call returns an APML file with 
                a 'Music-Recommendations' section that lists artist recommendations based upon your listening habits.
                <p>
                Some examples:
                <ul>
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/lamere'> Paul's Recommendations</a></li>
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/musicmobs'> Toby's Recommendations</a></li>
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/ocelma'> Oscar's Recommendations</a></li>
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/pandora/tconrad'> Tom's Recommendations</a></li>
                </ul>
                
                <p>
                The algorithms used to create recommendations are described in <a href="http://blogs.sun.com/plamere/entry/tagomendations_making_recommedations_transparent">
                Tagomendations: Making Recommendations Transparent</a>.
                
                <p>
                The web service takes a number of parameters:
                
                <table width="80%" border="1" cellspacing="2" cellpadding="2">
                    <thead>
                        <tr>
                            <th>Parameter</th>
                            <th>Default</th>
                            <th>Description</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <th>apmlURL</th>
                            <td>None</td>
                            <td>The url of the input apml file</td>
                        </tr>
                        <tr>
                            <th>outputFormat</th>
                            <td>apml</td>
                            <td>The output format. Currently supported formats are: 'apml'. Coming soon: xspf</td>
                        </tr>
                        <tr>
                            <th>alg</th>
                            <td>default</td>
                            <td>The recommendation algorithm to use. Current algorithms are 
                            <ul> 
                                <li> imp1 - recommendations based upon your implicit tastes</li>
                                <li> exp1 - recommendations based upon your explicit listening behavior</li>
                                <li> exp2 - alternative recommendations based upon your explicit listening behavior</li>
                                <li> default - use a well-rounded recommendation algorithim</li>
                            </ul>
                        </tr>
                        <tr>
                            <th>type</th>
                            <td>artist</td>
                            <td>The type of recommendations to generate.  Currently supported types are: 'artist'. Coming soon: track</td>
                        </tr>
                        <tr>
                            <th>num</th>
                            <td>30</td>
                            <td>The number of recommendations to generate.</td>
                        </tr>
                        <tr>
                            <th>profile</th>
                            <td>defaultprofile</td>
                            <td>The APML profile to use when generating recommendations.  If none is specified, the defaultprofile
                                found in the APML is used.
                            </td>
                        </tr>
                    </tbody>
                </table>
                
                <p>
                Some more examples
                <ul>
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/lamere&alg=imp1'> 
                        Paul's recommendations</a> using the implicit-1 recommender 
                    </li>
                    
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/lamere&alg=imp1&type=artist&profile=weekly-music&num=100'> 
                    Paul's recommendations</a> using the implicit-1 recommender, the weekly-music profile, with 100 artists output
                    
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/lamere&alg=exp1&type=artist&profile=weekly-music&num=100'> 
                        Paul's recommendations</a> using the explicit-1 recommender, the weekly-music profile, with 20 artists output
                    </li>
                    
                    <li><a href='RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/lamere&alg=exp2&type=artist&profile=weekly-music&num=100'> 
                        Paul's recommendations</a> using the explicit-2 recommender, the weekly-music profile, with 20 artists output
                    </li>
                    
                </ul>
                    <p>
                    <b>Last.fm users try it yourself:</b>
                    <div class="tryit">
                        http://aura.darkstar.sunlabs.com/apml/last.fm/<input type="text" 
                        onChange="window.location = 'RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/last.fm/' + this.value;" /> 
                    </div>
                    <p>
                        If you are a last.fm user, enter your last.fm user name, and an APML with recommendations will
                        be created for you.
                    <p>
                    <b>Pandora users try it yourself:</b>

                    <div class="tryit">
                        http://aura.darkstar.sunlabs.com/apml/pandora/<input type="text" 
                        onChange="window.location = 'RecommenderService?apmlURL=http://aura.darkstar.sunlabs.com/AttentionProfile/apml/pandora/' + this.value;" /> 
                    </div>
                    <p>
                        If you are a Pandora user, enter your Pandora user name, and an APML with recommendations will
                        be created for you.
            </div>
            <div class="nested2">
                <h2> What is this all about?</h2>
                This is an experiment to explore web services that produce and consume APML.
                <p>
                We can combine this APML generator with other services that process
                APML.  For instance <a href="http://www.cluztr.com/"> Cluztr </a> provides
                some Javascript that will turn an APML file into a tag cloud.  For example, here's
                a tag cloud generated from my APML file:
                
                <p>
                <script type="text/javascript">
                    tagcloud_title = "Paul's web browing interests";
                    apml_url = "http://aura.darkstar.sunlabs.com/AttentionProfile/apml/web/plamere/";
                </script>
                <script language="javascript" type="text/javascript" src="http://www.cluztr.com/api/apml_tag_cloud.js"></script>
                <p>
                
                <p>
                There are also some <a href="apml/stats">usage stats</a>.
            </div>
            <div class="nested2">
                <h2> History </h2>
                <ul>
                    <li> January  14, 2008 - Added webservice for recommendations
                    <li> January  12, 2008 - Fixed up encoding of explicit and implicit concept keys
                    <li> January  10, 2008 - Refactoring to support APML loading and recommendation
                    <li> December  5, 2007 - Added Pandora support
                    <li> December  2, 2007 - timezone, concept normalization fixes
                    <li> November 30, 2007 - added del.icio.us support
                    <li> November 23, 2007 - added support for explicit concepts
                    <li> November 21, 2007 - initial revision
                </ul>
            </div>
            
            <hr>
            <center> 
                <br>TasteBroker.org v.72 - Powered by APML, Cluztr, Del.icio.us, Last.fm, Pandora and Sun Microsystems inc.
                <br> Send comments/feedback or complaints to Paul.Lamere@sun.com
                <br> Don't forget to read <a href="http://blogs.sun.com/plamere">Duke Listens!</a>
            </center>
        </div>
    </body>
</html>
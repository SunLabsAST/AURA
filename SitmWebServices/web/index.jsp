<%-- 
    Document   : index
    Created on : May 23, 2008, 6:40:41 AM
    Author     : plamere
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>SITM Webservices Page</title>
    </head>
    <body>
        <h2>Formal SITM Webservices Documentation</h2>
<a href="doc/api/index.html">Web Services Documentation</a>
         
        <h2>Quick SITM Documentation</h2>
            <ul>
<li> <a href="AddArtist?showDocumentation=true">AddArtist</a>
<li> <a href="AddAttentionData?showDocumentation=true">AddAttentionData</a>
<li> <a href="AddListener?showDocumentation=true">AddListener</a>
<li> <a href="ArtistSearch?showDocumentation=true">ArtistSearch</a>
<li> <a href="ArtistTagSearch?showDocumentation=true">ArtistTagSearch</a>
<li> <a href="FindSimilarArtistTags?showDocumentation=true">FindSimilarArtistTags</a>
<li> <a href="FindSimilarArtists?showDocumentation=true">FindSimilarArtists</a>
<li> <a href="FindSimilarArtistsFromWordCloud?showDocumentation=true">FindSimilarArtistsFromWordCloud</a>
<li> <a href="FindSimilarListeners?showDocumentation=true">FindSimilarListeners</a>
<li> <a href="GetApml?showDocumentation=true">GetApml</a>
<li> <a href="GetArtistTags?showDocumentation=true">GetArtistTags</a>
<li> <a href="GetArtists?showDocumentation=true">GetArtists</a>
<li> <a href="GetAttentionData?showDocumentation=true">GetAttentionData</a>
<li> <a href="GetItems?showDocumentation=true">GetItems</a>
<li> <a href="GetListeners?showDocumentation=true">GetListeners</a>
<li> <a href="GetRecommendationTypes?showDocumentation=true">GetRecommendationTypes</a>
<li> <a href="GetRecommendations?showDocumentation=true">GetRecommendations</a>
<li> <a href="GetStats?showDocumentation=true">GetStats</a>
<li> <a href="GetTags?showDocumentation=true">GetTags</a>
            </ul>
        <h2>Examples</h2>
        <ul>    
            <li> <a href="GetStats"> Gets the system status</a>
            <li> <a href="AddArtist?appKey=SAMPLE_APP_ID&mbaid=688d084b-2b22-44bb-ae69-1a41499eb82f"> Adds an artist to the system</a>
            <li> <a href="AddAttentionData?appKey=SAMPLE_APP_ID&srcKey=lamere&tgtKey=6fe07aa5-fec0-4eca-a456-f29bff451b04&type=PLAYED"> Adds PLAYED attention to Weezer</a>
            <li> <a href="AddAttentionData?appKey=SAMPLE_APP_ID&srcKey=lamere&tgtKey=6fe07aa5-fec0-4eca-a456-f29bff451b04&type=tag&value=nerd+core"> Adds a tag to Weezer</a>
            <li> <a href="FindSimilarArtists?name=weezer"> Artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtists?name=weezer&popularity=HEAD"> Popular artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtists?name=weezer&popularity=HEAD_MID"> Artists Similar to Weezer (no long tail) </a>
            <li> <a href="FindSimilarArtists?name=weezer&popularity=MID"> Mid-popular artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtists?name=weezer&popularity=MID_TAIL"> Unpopular artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtists?name=weezer&popularity=TAIL"> Long tail artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtists?name=beatles"> Artists Similar to The Beatles </a>
            <li> <a href="FindSimilarArtistsFromWordCloud?wordCloud='(indie,1)(punk,1)(emo,.5)'"> Find Artists Similar to Word Cloud (indie, punk, emo)</a>
            <li> <a href="FindSimilarArtistTags?name=metal&max=100"> Similar metal tags </a>
            <li> <a href="FindSimilarArtists?key=aa7a2827-f74b-473c-bd79-03d065835cf7"> Artists Similar to Franz Ferdinand </a>
            <li> <a href="ArtistSearch?name=beat"> Artist search for 'beat' </a>
            <li> <a href="ArtistTagSearch?name=alt"> Tag search for 'alt' </a>
            <li> <a href="GetArtistTags?name=weezer"> Distinctive tags for weezer</a>
            <li> <a href="GetArtistTags?name=weezer&type=frequent"> Frequent tags for weezer</a>
            <li> <a href="GetArtists?max=100"> Get the 100 most popular artists</a>
            <li> <a href="GetListeners?max=100"> Get the 100 most active listeners</a>
            <li> <a href="GetTags?max=100"> Get the 100 most popular tags</a>
            <li> <a href="GetRecommendationTypes"> Get supported recommendation types</a>
            <li> <a href="GetRecommendations?userKey=http://paul.lamere.myopenid.com/"> Default Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userKey=http://paul.lamere.myopenid.com/&alg=SimToUserTagCloud"> SimToUserTagCloud Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userKey=http://paul.lamere.myopenid.com/&alg=SimpleArtist"> SimpleArtist Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userKey=http://paul.lamere.myopenid.com/&alg=SimToRecent"> SimToRecent Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userKey=lamere&alg=CollaborativeFilterer"> CF Recommendations for lamere </a>
            <li> <a href="GetApml?userKey=http://paul.lamere.myopenid.com/"> Gets the APML for lamere (with MBAIDs)</a>
            <li> <a href="GetApml?userKey=http://paul.lamere.myopenid.com/&format=artist"> Gets the APML for lamere (with artistNames)</a>
            <li> <a href="FindSimilarListeners?key=http://paul.lamere.myopenid.com/"> Finds similar listener to lamere</a>
            <li> <a href="GetItems?key=http://paul.lamere.myopenid.com/"> shows listener lamere</a>
            <li> <a href="GetItems?key=6fe07aa5-fec0-4eca-a456-f29bff451b04"> shows artist 'weezer'</a>
            <li> <a href="GetItems?format=compact&key=6fe07aa5-fec0-4eca-a456-f29bff451b04,a74b1b7f-71a5-4011-9441-d0b5e4122711,b10bbbfc-cf9e-42e0-be17-e2c3e1d2600d,9c9f1380-2516-4fc9-a3e6-f9f61941d090,b071f9fa-14b0-4217-8e97-eb41da73f598"> shows top artists</a>
            <li> <a href="GetItems?format=compact&key=6fe07aa5-fec0-4eca-a456-f29bff451b04,1e477f68-c407-4eae-ad01-518528cedc2,ec7c97cc-1d06-4c74-bcb5-6773391f90b,http://static.flickr.com/33/49490624_fecd23a894.jpg,50c213ed-a109-4677-9b7b-d57ef580dd2d,1f8bdf5d-aed1-45aa-bf1f-94af1d7e71c1"> multi-get compact</a>
            <li> <a href="GetItems?key=6fe07aa5-fec0-4eca-a456-f29bff451b04&format=compact"> shows compact artist 'weezer'</a>
            <li> <a href="GetAttentionData?tgtKey=6fe07aa5-fec0-4eca-a456-f29bff451b04"> get attention applied to 'weezer'</a>
            <li> <a href="GetAttentionData?srcKey=lamere&max=500">Get 500 attention data points for user lamere</a>
      </ul>
        
    </body>
</html>

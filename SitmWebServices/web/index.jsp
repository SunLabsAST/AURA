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
        <title>JSP Page</title>
    </head>
    <body>
        <h2>SITM Web Service</h2>
        <ul>    
            <li> <a href="GetStats"> Gets the system status</a>
            <li> <a href="FindSimilarArtist?name=weezer"> Artists Similar to Weezer </a>
            <li> <a href="FindSimilarArtist?name=beatles"> Artists Similar to The Beatles </a>
            <li> <a href="FindSimilarArtistFromWordCloud?wordCloud='(indie,1)(punk,1)(emo,.5)'"> Find Artists Similar to Word Cloud (indie, punk, emo)</a>
            <li> <a href="FindSimilarArtistTags?name=metal&max=100"> Similar metal tags </a>
            <li> <a href="FindSimilarArtist?key=aa7a2827-f74b-473c-bd79-03d065835cf7"> Artists Similar to Franz Ferdinand </a>
            <li> <a href="ArtistSearch?name=beat"> Artist search for 'beat' </a>
            <li> <a href="ArtistTagSearch?name=alt"> Tag search for 'alt' </a>
            <li> <a href="ArtistSocialTags?name=weezer"> Distinctive tags for weezer</a>
            <li> <a href="ArtistSocialTags?name=weezer&type=frequent"> Frequent tags for weezer</a>
            <li> <a href="GetTags?max=100"> Get the 100 most popular tags</a>
            <li> <a href="GetRecommendations?userID=http://paul.lamere.myopenid.com/"> Default Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userID=http://paul.lamere.myopenid.com/&alg=SimToUserTagCloud"> SimToUserTagCloud Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userID=http://paul.lamere.myopenid.com/&alg=SimpleArtist"> SimpleArtist Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userID=http://paul.lamere.myopenid.com/&alg=SimToRecent"> SimToRecent Recommendations for lamere </a>
            <li> <a href="GetRecommendations?userID=lamere&alg=CollaborativeFilterer"> CF Recommendations for lamere </a>
            <li> <a href="GetApml?userID=http://paul.lamere.myopenid.com/"> Gets the APML for lamere (with MBAIDs)</a>
            <li> <a href="GetApml?userID=http://paul.lamere.myopenid.com/&format=artist"> Gets the APML for lamere (with artistNames)</a>
            <li> <a href="FindSimilarListener?userID=http://paul.lamere.myopenid.com/"> Finds similar listener to lamere</a>
            <li> <a href="GetItem?itemID=http://paul.lamere.myopenid.com/"> shows listener lamere</a>
            <li> <a href="GetItem?itemID=6fe07aa5-fec0-4eca-a456-f29bff451b04"> shows artist 'weezer'</a>
            <li> <a href="TagItem?userID=http://paul.lamere.myopenid.com/&itemID=6fe07aa5-fec0-4eca-a456-f29bff451b04&tag=weezercore"> apply a tag to weezer</a>
            <li> <a href="GetListenerTags?userID=http://paul.lamere.myopenid.com/&itemID=6fe07aa5-fec0-4eca-a456-f29bff451b04"> get tags applied to 'weezer'</a>
            <li> <a href="GetAttentionData?src=lamere&max=500">Get 500 attention data points for user lamere</a>
      </ul>
        
    </body>
</html>

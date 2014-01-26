<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib prefix="tag" tagdir="/WEB-INF/tags" %>

<html>
<head>
    <meta content="text/html; charset=utf-8" http-equiv="Content-Type"/>
    <title>Sign in with Twitter example</title>
</head>
<body>
<tag:notloggedin>
    <a href="signin"><img src="./images/Sign-in-with-Twitter-darker.png"/></a>
</tag:notloggedin>
<tag:loggedin>
    <h1>Welcome ${twitter.screenName}</h1>
	View your URL statistics <a href="http://able-inn-471.appspot.com/t/${twitter.screenName}">here</a><br/><br/>
    <a href="./logout">Logout</a>
</tag:loggedin>
</body>
</html>


<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
	String basePath = request.getScheme() /* 协议 */
		+ "://"
		+ request.getServerName()     /* 实际访问ip地址(服务器所在地址) */
		+ ":"
		+ request.getServerPort()     /* 服务器的端口号 */
		+ request.getContextPath()    /* /工程名 */
		+ "/";
%>

<html>
<head>
    <!--写base标签，永远固定相对路径跳转的结果-->
    <base href="<%=basePath%>">
    <meta charset="UTF-8">
    <link href="jquery/bootstrap_3.3.0/css/bootstrap.min.css" type="text/css" rel="stylesheet"/>
    <script type="text/javascript" src="jquery/jquery-1.11.1-min.js"></script>
    <script type="text/javascript" src="jquery/bootstrap_3.3.0/js/bootstrap.min.js"></script>

</head>
<body>
<img src="image/home.png" style="position: relative;top: -10px; left: -10px;"/>
</body>
</html>
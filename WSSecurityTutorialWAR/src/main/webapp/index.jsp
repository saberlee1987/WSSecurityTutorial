<%@page import="org.springframework.web.context.support.WebApplicationContextUtils"%>
<%@page import="org.springframework.context.ApplicationContext" %>
<html>
<%
	ApplicationContext ac = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
	Boolean debug = ac.getBean("debug", Boolean.class);
%>
    <body>
        <div align="center">
            <h1>Status Page</h1>
            <p>If the boolean property debug is valid, the context started correctly.  debug=<%= debug %>.</p>
        </div>
    </body>
</html>

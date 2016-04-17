<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have received a copy of the text describing
    the FLOSS exception, and it is also available here:
    "http://www.silverpeas.org/docs/core/legal/floss_exception.html"

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

--%>
<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>
<%@ page import="jChatBox.Util.*,jChatBox.Chat.*,java.util.*,java.text.SimpleDateFormat" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane" %>
<%@ include file="../checkChat.jsp" %>
<jsp:useBean id="UserProcessor" class="jChatBox.Service.UserProcessor" scope="application" />
<%
	jChatBox.Chat.ChatroomManager ChatroomManager = null;
	String chatroomName = "", chatroomSubject = "", chatroomDate = "";
	String jspDisplay = UserProcessor.execute(request,response,session,application);
	if (jspDisplay != null)
	{
		response.sendRedirect(response.encodeRedirectURL(jspDisplay));
	}
	else
	{
		ChatroomManager = jChatBox.Chat.ChatroomManager.getInstance();
		ChatroomUser cUser = (ChatroomUser) session.getValue(XMLConfig.USERSESSIONID);
		Chatroom chatroom = null;
		if (cUser != null)
		{
			int cID = cUser.getParams().getChatroom();
			try
			{
				chatroom = ChatroomManager.getChatroom(cID);
				chatroomName = chatroom.getParams().getName();
				chatroomSubject = chatroom.getParams().getSubject();
				chatroomDate = (new SimpleDateFormat("yyyyy/MM/dd HH:mm")).format(chatroom.getDate());
			} catch (ChatException ce)
			  {
			  	/** Chatroom not found */
				response.sendRedirect(response.encodeRedirectURL(Conf.JSPUSERLOGIN));
			  }
		}
	}
%>
<html>
<head>
<title><%= chatroomName %></title>
<view:looknfeel/>
<script language="JavaScript"><!--
function ready()
{
	document.chat.msg.focus();
}
function SendMessage()
{
	document.chat.submit();
	document.chat.msg.value = "";
	document.chat.reset();
	document.chat.msg.focus();
}
function chat()
{
	document.chat.todo.value="chat";
	SendMessage();
}
function fct_redirect()
{
	//parent.parent.location.href='<%= response.encodeURL("content.jsp?todo=quit")%>';
	parent.parent.location.href='<%= ((String)request.getAttribute("myComponentURL")) %>content.jsp?todo=quit';
}
//--></script>
</head>
<body bgcolor="#FFFFFF" leftmargin="1" topmargin="1" marginwidth="1" marginheight="1" onLoad="ready()">
<form method="get" action="<%= response.encodeURL("content.jsp") %>" name="chat" target="content" OnSubmit='SendMessage();return false;'>
  <table width="98%" border="0" cellspacing="1" cellpadding="0">
    <tr>
      <td>
        <table width="98%" border="0" cellspacing="1" cellpadding="1" align="center" class="sendArea">
          <tr>
            <td nowrap><span class="txtlibform"><%=resource.getString("chat.Message")%></span>&nbsp;:&nbsp;
              <input type="text" name="msg" size="50" maxlength="120">
              <input type="hidden" name="to" value="ALL">
            </td>
						<td align="left">
<%
						ButtonPane bouton = gef.getButtonPane();
						bouton.addButton(gef.getFormButton(resource.getString("GML.validate"), "javascript:chat()", false));
						out.println(bouton.print());
%>
						</td><input type="hidden" name="todo" value="chat">
						<td width="100%">&nbsp;</td>
          </tr>
        </table>
      </td>
    </tr>
  </table>
</form>
</body>
</html>
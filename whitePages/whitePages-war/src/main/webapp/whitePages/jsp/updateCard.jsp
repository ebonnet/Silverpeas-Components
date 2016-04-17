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
<%@ page import="org.silverpeas.core.contribution.content.form.DataRecord"%>
<%@ page import="org.silverpeas.core.contribution.content.form.Form"%>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext"%>
<%@ page import="org.silverpeas.components.whitepages.model.Card" %>

<%@ include file="checkWhitePages.jsp" %>

<%
	Card card		= (Card) request.getAttribute("card");
	Form			updateForm	= (Form) request.getAttribute("Form");
	PagesContext	context		= (PagesContext) request.getAttribute("context");
	DataRecord		data		= (DataRecord) request.getAttribute("data");
%>


<HTML>
<HEAD>
<TITLE><%=resource.getString("GML.popupTitle")%></TITLE>
<view:looknfeel/>
<%
   updateForm.displayScripts(out, context);
%>
<view:includePlugin name="wysiwyg"/>
<script language="JavaScript">
<!--
	function B_VALIDER_ONCLICK(idCard) {
		if (isCorrectForm())
		{

			document.myForm.action = "<%=routerUrl%>effectiveUpdate?userCardId="+idCard;
			document.myForm.submit();
		}
	}

	function B_ANNULER_ONCLICK(idCard) {
		self.close();
	}
//-->
</script>
</HEAD>

<BODY class="yui-skin-sam">
<view:browseBar path='<%=resource.getString("whitePages.usersList") + " > "+ resource.getString("whitePages.editCard")%>'/>
<view:window popup="true">
<view:frame>

<FORM NAME="myForm" METHOD="POST" ENCTYPE="multipart/form-data">

<%
	updateForm.display(out, context, data);
%>
</FORM>
<%
    ButtonPane buttonPane = gef.getButtonPane();
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.validate"), "javascript:onClick=B_VALIDER_ONCLICK('"+card.getPK().getId()+"');", false));
    buttonPane.addButton((Button) gef.getFormButton(resource.getString("GML.cancel"), "javascript:onClick=B_ANNULER_ONCLICK('"+card.getPK().getId()+"');", false));
    out.println(buttonPane.print());
%>
</view:frame>
</view:window>
</BODY>
</HTML>
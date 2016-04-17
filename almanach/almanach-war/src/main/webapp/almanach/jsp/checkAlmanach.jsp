<%--

    Copyright (C) 2000 - 2013 Silverpeas

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    As a special exception to the terms and conditions of version 3.0 of
    the GPL, you may redistribute this Program in connection with Free/Libre
    Open Source Software ("FLOSS") applications as described in Silverpeas's
    FLOSS exception.  You should have recieved a copy of the text describing
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
<%@ page import="java.io.PrintWriter"%>
<%@ page import="java.io.IOException"%>
<%@ page import="java.io.FileInputStream"%>
<%@ page import="java.io.ObjectInputStream"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.lang.Integer"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.net.URLEncoder"%>

<%@ page import="org.silverpeas.core.util.EncodeHelper"%>

<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>
<%@ page import="org.silverpeas.components.almanach.control.AlmanachSessionController"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>
<%@ page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.util.SettingBundle" %>
<%@ page import="java.util.Date" %>

<%@ page errorPage="../../admin/jsp/errorpage.jsp"%>
<%
	GraphicElementFactory graphicFactory = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
	AlmanachSessionController almanach = (AlmanachSessionController) request.getAttribute("almanach");
	MultiSilverpeasBundle resources = (MultiSilverpeasBundle)request.getAttribute("resources");
	SettingBundle settings = (SettingBundle) request.getAttribute("settings");
	if (almanach == null)
	{
	    // No almanach session controller in the request -> security exception
	    String sessionTimeout = ResourceLocator.getGeneralSettingBundle().getString(
					"sessionTimeout");
	    getServletConfig().getServletContext().getRequestDispatcher(sessionTimeout).forward(request, response);
	    return;
	}

	String m_context			= ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
	
	// Icones diverses
	String addEventSrc			= m_context + "/util/icons/addEvent.gif";
	String printSrc             = m_context + "/util/icons/almanach_to_print.gif";
	String pdfSrc               = m_context + "/util/icons/almanach_to_pdf.gif";
	String calendarPdfSrc       = m_context + "/util/icons/calendrier_to_pdf.gif";
	String okSrc				= m_context + "/util/icons/almanach_ok.gif";

	// Pixels
	String noColorPix           = m_context + "/util/icons/colorPix/1px.gif";

	// Divers
	String arrRight				= m_context + "/util/icons/arrow/arrowRight.gif";
	String arrLeft				= m_context + "/util/icons/arrow/arrowLeft.gif";
	String pdcUtilizationSrc	= m_context + "/pdcPeas/jsp/icons/pdcPeas_paramPdc.gif";

	String[] browseContext = (String[]) request.getAttribute("browseContext");
	String spaceLabel = browseContext[0];
	String componentLabel = browseContext[1];
	String spaceId = browseContext[2];
	String instanceId = browseContext[3];

%>
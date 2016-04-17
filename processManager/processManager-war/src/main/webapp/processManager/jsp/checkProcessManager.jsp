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

<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache");        //HTTP 1.0
response.setDateHeader ("Expires",-1);          //prevents caching
%>

<%@ page isELIgnored="false"%>

<%@ page import="java.net.URLEncoder"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="java.util.Date"%>
<%@ page import="java.util.List"%>
<%@ page import="java.util.Vector"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.StringTokenizer"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.window.Window"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.operationpanes.OperationPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.browsebars.BrowseBar"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.frame.Frame"%>
<%@ page import="org.silverpeas.core.util.MultiSilverpeasBundle"%>

<%@ page import="org.silverpeas.core.workflow.api.instance.ProcessInstance" %>
<%@ page import="org.silverpeas.core.contribution.content.form.PagesContext" %>
<%@ page import="org.silverpeas.core.contribution.content.form.DataRecord" %>
<%@ page import="org.silverpeas.core.workflow.api.error.WorkflowError" %>
<%@ page import="org.silverpeas.core.util.file.FileRepositoryManager" %>
<%@ page import="org.silverpeas.core.workflow.api.model.State" %>
<%@ page import="org.silverpeas.core.workflow.api.model.Action" %>
<%@ page import="org.silverpeas.core.workflow.api.instance.HistoryStep" %>
<%@ page import="org.silverpeas.core.workflow.api.instance.Question" %>
<%@page import="org.silverpeas.core.util.StringUtil"%>
<%@ page import="org.silverpeas.core.contribution.content.form.RecordTemplate" %>
<%@ page import="org.silverpeas.core.contribution.content.form.FieldTemplate" %>
<%@ page import="org.silverpeas.core.workflow.api.model.Item" %>
<%@ page import="org.silverpeas.core.workflow.api.task.Task" %>
<%@ page import="org.silverpeas.core.contribution.content.form.Field" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayLine"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayColumn"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellLink"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.iconpanes.IconPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.icons.Icon"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.tabs.TabbedPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.navigationlist.NavigationList"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttonpanes.ButtonPane"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.board.Board"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.GraphicElementFactory"%>
<%@ page import="org.silverpeas.core.util.URLUtil"%>

<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.Encode"%>
<%@ page import="org.silverpeas.core.util.ResourceLocator"%>
<%@ page import="org.silverpeas.core.workflow.engine.datarecord.ProcessInstanceRowRecord"%>
<%@ page import="org.silverpeas.core.contribution.content.form.field.DateField"%>
<%@ page import="org.silverpeas.processmanager.NamedValue" %>
<%@ page import="org.silverpeas.core.util.LocalizationBundle" %>
<%@ page errorPage="../../admin/jsp/errorpageMain.jsp"%>

<%
GraphicElementFactory gef = (GraphicElementFactory) session.getAttribute("SessionGraphicElementFactory");
String language = (String) request.getAttribute("language");
MultiSilverpeasBundle resource = (MultiSilverpeasBundle)request.getAttribute("resources");

String m_context = ResourceLocator.getGeneralSettingBundle().getString("ApplicationURL");
String[] browseContext = (String[]) request.getAttribute("browseContext");
String spaceLabel = browseContext[0];
String componentLabel = browseContext[1];
String spaceId = browseContext[2];
String componentId = browseContext[3];
String processManagerUrl = browseContext[4];

LocalizationBundle generalMessage = ResourceLocator.getGeneralLocalizationBundle(language);

Window window = gef.getWindow();
BrowseBar browseBar = window.getBrowseBar();
OperationPane operationPane = window.getOperationPane();
Frame frame = gef.getFrame();
TabbedPane tabbedPane = gef.getTabbedPane();
Board board = gef.getBoard();

// Shared attributes
NamedValue[] roles = (NamedValue[]) request.getAttribute("roles");
if (roles == null) roles = new NamedValue[0];

String currentRole  = (String) request.getAttribute("currentRole");
if (currentRole == null) currentRole = "";
%>

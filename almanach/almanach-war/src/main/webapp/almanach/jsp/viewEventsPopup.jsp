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
<%
response.setHeader("Cache-Control","no-store"); //HTTP 1.1
response.setHeader("Pragma","no-cache"); //HTTP 1.0
response.setDateHeader ("Expires",-1); //prevents caching at the proxy server
%>

<%@ page import="org.silverpeas.components.almanach.control.AlmanachCalendarView"%>
<%@ page import="org.silverpeas.components.almanach.control.DisplayableEventOccurrence"%>
<%@ page import="org.silverpeas.components.almanach.model.EventDetail"%>

<%@ page import="java.util.Calendar"%>
<%@ page import="java.util.List"%>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.arraypanes.ArrayCellText" %>
<%@ page import="org.silverpeas.core.web.util.viewgenerator.html.buttons.Button" %>

<%@ taglib uri="http://www.silverpeas.com/tld/viewGenerator" prefix="view"%>

<%@ include file="checkAlmanach.jsp" %>
<%
  AlmanachCalendarView calendarView = (AlmanachCalendarView) request.getAttribute("calendarView");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<title><%=resources.getString("GML.popupTitle")%></title>
<view:looknfeel/>
<script type="text/javascript">
function viewEvent(componentId, id) {
	window.opener.location.href="<%=m_context%>/Ralmanach/"+componentId+"/viewEventContent.jsp?Id="+id;
}
</script>
</head>
<body>
<view:window popup="true">
<view:frame>
<%
	int currentDay = -1;
	Calendar calendar = Calendar.getInstance();
	calendar.setTime(almanach.getCurrentDay());
	calendar.set(Calendar.DAY_OF_YEAR, 1);
	int currentYear = calendar.get(Calendar.YEAR);
	int currentMonth = calendar.get(Calendar.MONTH);

	java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("yyyy/MM/dd");

	ArrayPane arrayPane = graphicFactory.getArrayPane("YearEvents", "ViewYearEventsPOPUP", request, session);
	arrayPane.setVisibleLineNumber(resources.getSetting("almanach.yeareventspopup.arraypagesize", 15));
  	arrayPane.setXHTML(true);
  	arrayPane.setExportData(true);
  	arrayPane.addArrayColumn(resources.getString("GML.month"));
  	arrayPane.addArrayColumn(resources.getString("GML.title"));
  	ArrayColumn column1 = arrayPane.addArrayColumn(resources.getString("GML.dateBegin"));
  	column1.setWidth("100px");
  	ArrayColumn column2 = arrayPane.addArrayColumn(resources.getString("GML.dateEnd"));
  	column2.setWidth("100px");

    List<DisplayableEventOccurrence> occurrences = calendarView.getEvents();
	for (DisplayableEventOccurrence occurrence: occurrences) {
		EventDetail event = occurrence.getEventDetail();
		String startDay = dateFormat.format(occurrence.getStartDate());
		String url = m_context+"/Ralmanach/"+event.getPK().getInstanceId()+"/viewEventContent.jsp?Id="+event.getPK().getId()+"&amp;Date="+dateFormat.format(calendar.getTime());
		String endDay = startDay;
		if (occurrence.getEndDate() != null) {
			endDay = dateFormat.format(occurrence.getEndDate());
		}
		calendar.setTime(occurrence.getStartDate().asDate());

		String title = EncodeHelper.javaStringToHtmlString(event.getTitle());
		String description = null;

		if (StringUtil.isDefined(event.getWysiwyg())) {
			description = event.getWysiwyg();
		}
	    else if (StringUtil.isDefined(event.getDescription())) {
     			 description = EncodeHelper.javaStringToHtmlParagraphe(event.getDescription());
		}

		if (almanach.isAgregationUsed())
		{
			String eventColor = almanach.getAlmanachColor(event.getInstanceId());
			title = "<b><span style=\"color :"+eventColor+"\">"+title+"</span></b>";
			if (StringUtil.isDefined(description)) {
				description = "<span style=\"color :"+eventColor+"\">"+description+"</span>";
			}
		}

		ArrayLine line = arrayPane.addArrayLine();
		ArrayCellText month = line.addArrayCellText(resources.getString("GML.mois"+calendar.get(Calendar.MONTH)));
		month.setCompareOn(Integer.valueOf(calendar.get(Calendar.MONTH)));
		ArrayCellText link = line.addArrayCellText("<a href=\"javascript:viewEvent('"+event.getPK().getInstanceId()+"','"+event.getPK().getId()+"')\">"+title+"</a>");
		link.setCompareOn(title);
		ArrayCellText start = line.addArrayCellText(resources.getOutputDate(startDay));
		start.setCompareOn(startDay);
		ArrayCellText end = line.addArrayCellText(resources.getOutputDate(endDay));
		end.setCompareOn(endDay);
	}

	out.println(arrayPane.print());

	Button button = graphicFactory.getFormButton(resources.getString("GML.close"), "javascript:window.close()", false);
    out.print("<br/><center>"+button.print()+"</center>");
%>
</view:frame>
</view:window>
</body>
</html>
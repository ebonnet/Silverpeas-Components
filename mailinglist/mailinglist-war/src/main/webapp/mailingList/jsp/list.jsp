<%@ page isELIgnored="false"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"%>
<%@ include file="check.jsp"%>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c"%>
<%@ taglib uri="/WEB-INF/fmt.tld" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/viewGenerator.tld" prefix="view"%>
<c:set var="componentId" value="${requestScope.componentId}" />
<c:set var="sessionController">Silverpeas_MailingList_<c:out value="${componentId}" />
</c:set>
<fmt:setLocale value="${sessionScope[sessionController].language}" />
<view:setBundle bundle="${requestScope.resources.multilangBundle}" />
<view:setBundle bundle="${requestScope.resources.iconsBundle}" var="icons" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title><fmt:message key="mailingList.tab.list.title" /></title>
<view:looknfeel />
<c:if test="${requestScope.currentUserIsAdmin}">
  <script type="text/javascript">
    function deleteMessage(){
      if(confirm('<fmt:message key="mailingList.moderation.delete.confirm"/>')){
        document.removeMessage.submit();
      }
    }
  </script>
</c:if>
</head>
<body>
<fmt:message key="mailingList.icons.attachmentSmall" var="attachmentIcon" bundle="${icons}" />
<fmt:message key="mailingList.tab.list.title" var="listTabTitle" />
<fmt:message key="mailingList.tab.activity.title" var="activityTabTitle" />
<view:browseBar link="Main" path="${listTabTitle}" />
<view:window>
  <c:if test="${requestScope.currentUserIsAdmin}">
    <fmt:message key="mailingList.icons.message.delete.alt" var="deleteMessageAltText" />
    <fmt:message key="mailingList.icons.message.delete" var="deleteIconPath" bundle="${icons}" />
    <c:url var="deleteIcon" value="${deleteIconPath}" />
    <view:operationPane>
      <view:operation altText="${deleteMessageAltText}" icon="${deleteIcon}" action="${'javascript: deleteMessage();'}" />
    </view:operationPane>
  </c:if>
  <c:choose>
    <c:when test="${requestScope.currentUserIsAdmin}">
      <c:set var="paginationColspan" value="5" />
    </c:when>
    <c:otherwise>
      <c:set var="paginationColspan" value="4" />
    </c:otherwise>
  </c:choose>
  <c:url var="activityAction" value="/Rmailinglist/${componentId}/Main" />
  <view:tabs>
    <view:tab label="${activityTabTitle}" action="${activityAction}" selected="false" />
    <view:tab label="${listTabTitle}" action="${'#'}" selected="true" />
    <c:if test="${(requestScope.currentUserIsAdmin || requestScope.currentUserIsModerator) && requestScope.currentListIsModerated}">
      <fmt:message key="mailingList.tab.moderation.title" var="moderationTabTitle" />
      <c:url var="moderationAction" value="/Rmailinglist/${componentId}/moderationList/${componentId}" />
      <view:tab label="${moderationTabTitle}" action="${moderationAction}" selected="false" />
    </c:if>
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.users.title" var="usersTabTitle" />
      <c:url var="usersAction" value="/Rmailinglist/${componentId}/users/${componentId}" />
      <view:tab label="${usersTabTitle}" action="${usersAction}" selected="false" />
    </c:if>
    <c:if test="${requestScope.currentUserIsAdmin}">
      <fmt:message key="mailingList.tab.subscribers.title" var="subscribersTabTitle" />
      <c:url var="subscriberssAction" value="/Rmailinglist/${componentId}/subscription/${componentId}/subscription/put" />
      <view:tab label="${subscribersTabTitle}" action="${subscriberssAction}" selected="false" />
    </c:if>
  </view:tabs>
  <view:frame>
    <center><c:if test="${requestScope.currentUserIsAdmin}">
      <form id="removeMessage" name="removeMessage" method="DELETE" action="<c:url value="/Rmailinglist/${componentId}/destination/list/message/delete"/>">
    </c:if>
    <table width="98%" border="0" cellpadding="0" cellspacing="2">
      <tr>
        <td>
        <table id="list" class="tableArrayPane" width="100%" cellspacing="2" cellpadding="2" border="0">
          <tr>
            <c:if test="${requestScope.currentUserIsAdmin}">
              <td class="ArrayColumn" align="center">&nbsp;</td>
            </c:if>
            <td class="ArrayColumn" width="100%"><fmt:message key="mailingList.list.messages.title" /></td>
            <td class="ArrayColumn" align="center" nowrap="nowrap"><a
              href="<c:url value="/Rmailinglist/${componentId}/list/${componentId}">
                <c:param name="orderBy" value="attachmentsSize"/>
                <c:param name="ascendant"><c:choose><c:when test="${requestScope['attachmentsSize'] != null}"><c:out value="${requestScope['attachmentsSize']}" /></c:when><c:otherwise>true</c:otherwise></c:choose></c:param></c:url>"><fmt:message
              key="mailingList.list.attachments.title" /></a></td>
            <td class="ArrayColumn" align="center" nowrap="nowrap"><a
              href="<c:out value="${pageContext.request.contextPath}"/>/Rmailinglist/<c:out value="${componentId}"/>/list/<c:out value="${componentId}"/>?orderBy=sender&ascendant=<c:choose><c:when test="${requestScope['sender'] != null}"><c:out value="${requestScope['sender']}" /></c:when><c:otherwise>true</c:otherwise></c:choose>" /><fmt:message
              key="mailingList.list.sender.title" /></a></td>
            <td class="ArrayColumn" align="center" nowrap="nowrap"><a
              href="<c:out value="${pageContext.request.contextPath}"/>/Rmailinglist/<c:out value="${componentId}"/>/list/<c:out value="${componentId}"/>?orderBy=sentDate&ascendant=<c:choose><c:when test="${requestScope['sentDate'] != null}"><c:out value="${requestScope['sentDate']}" /></c:when><c:otherwise>true</c:otherwise></c:choose>" /><fmt:message
              key="mailingList.list.sentDate.title" /></a></td>
          </tr>
          <c:forEach items="${requestScope.currentMessageList}" var="message" varStatus="messageIndex">
            <c:set var="lineClass" value="ArrayCell" />
            <c:choose>
              <c:when test="${(messageIndex.index%2) == 0}">
                <c:set var="lineClass" value="ArrayCell" />
              </c:when>
              <c:otherwise>
                <c:set var="lineClass" value="ArrayCell" />
              </c:otherwise>
            </c:choose>
            <tr>
              <c:if test="${requestScope.currentUserIsAdmin}">
                <td rowspan="2" align="center" valign="top"><input type="checkbox" name="message" value="<c:out value="${message.id}" />" /></td>
              </c:if>
              <td><b><a href="<c:url value="/Rmailinglist/${componentId}/destination/list/message/${message.id}"/>"><c:out value="${message.title}" /></a></b></td>
              <td align="right"><c:choose>
                <c:when test="${message.attachmentsSize <= 0}">&nbsp;</c:when>
                <c:otherwise>
                  <c:out value="${message.attachmentsSizeToDisplay}" />
                  <img src="<c:url value="${attachmentIcon}" />" />
                </c:otherwise>
              </c:choose></td>
              <td rowspan="2" align="center" valign="top" nowrap="nowrap"><c:out value="${message.sender}" /></td>
              <td valign="top" rowspan="2" valign="top" align="right" nowrap="nowrap"><fmt:formatDate value="${message.sentDate}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
            </tr>
            <tr class="<c:out value="${lineClass}"/>">
              <td valign="top" align="left" style="white-space: normal;"><c:out value="${message.summary}" /></td>
            </tr>
          </c:forEach>
          <tr>
            <c:url var="paginationAction" value="/Rmailinglist/${requestScope['componentId']}/list/${requestScope['componentId']}">
              <c:if test="${param.currentYear != null}">
                <c:param name="currentYear" value="${param.currentYear}" />
              </c:if>
              <c:if test="${param.currentMonth != null}">
                <c:param name="currentYear" value="${params.currentMonth}" />
              </c:if>
              <c:if test="${param.orderBy != null}">
                <c:param name="orderBy" value="${param.orderBy}" />
              </c:if>
              <c:if test="${param.ascendant != null}">
                <c:param name="ascendant" value="${param.ascendant}" />
              </c:if>
            </c:url>
            <td colspan="<c:out value="${paginationColspan}"/>"><view:pagination currentPage="${requestScope.currentPage}" nbPages="${requestScope.nbPages}" action="${paginationAction}"
              pageParam="currentPage" /></td>
          </tr>
        </table>
        </td>
      </tr>
    </table>
    <c:if test="${requestScope.currentUserIsAdmin}">
      </form>
    </c:if></center>
  </view:frame>
</view:window>
</body>
</html>
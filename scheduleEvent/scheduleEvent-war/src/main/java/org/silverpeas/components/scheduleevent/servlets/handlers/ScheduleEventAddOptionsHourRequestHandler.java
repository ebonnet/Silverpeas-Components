/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.silverpeas.components.scheduleevent.servlets.handlers;

import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.silverpeas.components.scheduleevent.control.ScheduleEventSessionController;
import org.silverpeas.components.scheduleevent.service.model.beans.DateOption;
import org.silverpeas.components.scheduleevent.service.model.beans.ScheduleEvent;
import org.silverpeas.core.silvertrace.SilverTrace;

public class ScheduleEventAddOptionsHourRequestHandler implements ScheduleEventRequestHandler {

  @Override
  public String getDestination(String function, ScheduleEventSessionController scheduleeventSC,
      HttpServletRequest request) throws Exception {
    ScheduleEvent current = scheduleeventSC.getCurrentScheduleEvent();
    Set<DateOption> dates = current.getDates();
    Iterator<DateOption> iter = dates.iterator();
    while (iter.hasNext()) {
      DateOption aDate = iter.next();
      String tmpId = formatterTmpId.format(aDate.getDay());
      String hourFromParameters = request.getParameter("hourFor" + tmpId);
      int hour;
      try {
        hour = Integer.parseInt(hourFromParameters);
      } catch (Exception e) {
        SilverTrace.warn("scheduleevent", "ScheduleEventRequestRouter.getDestination",
            "root.MSG_GEN_PARAM_VALUE", "hour is not a int = "
            + hourFromParameters);
        hour = 8; // morning by default
      }
      aDate.setHour(hour);
    }
    scheduleeventSC.setCurrentScheduleEvent(current);
    request.setAttribute(CURRENT_SCHEDULE_EVENT, current);
    return "form/notify.jsp";
  }

}

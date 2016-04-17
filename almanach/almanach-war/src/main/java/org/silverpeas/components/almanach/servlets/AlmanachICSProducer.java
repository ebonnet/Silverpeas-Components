/*
 * Copyright (C) 2000 - 2015 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have received a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "https://www.silverpeas.org/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.almanach.servlets;

import org.silverpeas.core.annotation.RequestScoped;
import org.silverpeas.core.annotation.Service;
import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.importexport.ExporterProvider;
import org.silverpeas.core.importexport.ical.ExportableCalendar;
import org.silverpeas.core.admin.service.AdminController;
import org.silverpeas.core.admin.user.model.UserFull;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.components.almanach.service.AlmanachException;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.components.almanach.service.CalendarEventEncoder;
import org.silverpeas.core.util.ServiceProvider;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import static org.silverpeas.core.importexport.ExportDescriptor.withWriter;

/**
 * A producer of an ICS resource from a given almanach.
 */
@Path("almanach/ics/{almanachId}")
@Service
@RequestScoped
public class AlmanachICSProducer {

  @QueryParam("userId")
  private String userId;
  @QueryParam("login")
  private String login;
  @QueryParam("password")
  private String password;
  @Inject
  private AdminController adminController;

  /**
   * Gets the almanach content specified by its identifier in the ICS format. The credence
   * parameters of the user tempting to access the almanach are passed as query parameters in the
   * almanach URL. If the user has not enough right to acccess the almanach, then a 403 error is
   * returned (access denied). If the almanach getting failed, then a 503 error is returned (service
   * unavailable).
   *
   * @param almanachId the unique identifier of the almanach to get.
   * @return the iCal almanach representation
   */
  @GET
  @Produces("text/calendar")
  public String getICS(@PathParam("almanachId") String almanachId) {
    StringWriter writer = new StringWriter();

    // Check login/pwd must be an identified user
    UserFull user = adminController.getUserFull(userId);
    if (user != null && user.getLogin().equals(login)
        && user.getPassword().equals(password) && adminController.isComponentAvailable(almanachId,
        userId)) {
      CalendarEventEncoder encoder = new CalendarEventEncoder();
      try {
        List<EventDetail> allEventDetails = getAllEvents(almanachId);
        List<CalendarEvent> allEvents = encoder.encode(allEventDetails);

        Exporter<ExportableCalendar> iCalExporter = ExporterProvider.getICalExporter();
        iCalExporter.export(withWriter(writer), ExportableCalendar.with(allEvents));
      } catch (Exception ex) {
        throw new WebApplicationException(ex, Status.SERVICE_UNAVAILABLE);
      }
    } else {
      throw new WebApplicationException(Status.FORBIDDEN);
    }

    return writer.toString();
  }

  /**
   * Gets all events of the underlying almanach.
   *
   * @param almanachId the almanach identifier
   * @return a list with the details of the events registered in the almanach.
   * @throws AlmanachException if an error occurs while getting the list of events.
   */
  public List<EventDetail> getAllEvents(final String almanachId) throws AlmanachException {
    EventPK pk = new EventPK("", null, almanachId);
    AlmanachService almanachService = ServiceProvider.getService(AlmanachService.class);
    return new ArrayList<>(almanachService.getAllEvents(pk));
  }
}

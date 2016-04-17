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
package org.silverpeas.components.almanach.control;

import org.silverpeas.core.calendar.CalendarEvent;
import org.silverpeas.core.importexport.ExportException;
import org.silverpeas.core.importexport.Exporter;
import org.silverpeas.core.importexport.ExporterProvider;
import org.silverpeas.core.importexport.ical.ExportableCalendar;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.model.PdcPosition;
import org.silverpeas.core.util.URLUtil;
import org.silverpeas.core.webapi.pdc.PdcClassificationEntity;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.web.mvc.util.AlertUser;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInstLight;
import org.apache.commons.io.FileUtils;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.core.contribution.attachment.model.SimpleDocumentPK;
import org.silverpeas.core.web.calendar.CalendarViewType;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventOccurrence;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.components.almanach.model.PeriodicityException;
import org.silverpeas.components.almanach.service.AlmanachBadParamException;
import org.silverpeas.components.almanach.service.AlmanachException;
import org.silverpeas.components.almanach.service.AlmanachNoSuchFindEventException;
import org.silverpeas.components.almanach.service.AlmanachRuntimeException;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.components.almanach.service.CalendarEventEncoder;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.date.period.PeriodType;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.file.FileRepositoryManager;
import org.silverpeas.core.util.file.FileServerUtils;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.Pair;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygException;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.silverpeas.core.importexport.ExportDescriptor.withWriter;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.NONE_CLASSIFICATION;
import static org.silverpeas.core.pdc.pdc.model.PdcClassification.aPdcClassificationOfContent;
import static org.silverpeas.core.cache.service.VolatileCacheServiceProvider
    .getSessionVolatileResourceCacheService;
import static org.silverpeas.core.web.calendar.CalendarViewType.*;
import static org.silverpeas.core.util.DateUtil.parse;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * The AlmanachSessionController provides features to handle almanachs and theirs events. A such
 * object wraps in fact the current almanach in the user session; in others words, the almanach on
 * which the user works currently. As the almanach is displayed in a given window time, the
 * AlmanachSessionController instance maintains the current opened window time and provides a way
 * to
 * move this window front or back in the time. The window time depends on the view mode choosen by
 * the user: it can be a monthly view, a weekly view, and so on.
 */
public class AlmanachSessionController extends AbstractComponentSessionController {

  private AlmanachService almanachService;
  private Calendar currentDay = Calendar.getInstance();
  private EventDetail currentEvent;
  private static final String AE_MSG1 = "almanach.ASC_NoSuchFindEvent";
  // Almanach Agregation
  private List<String> agregateAlmanachsIds = new ArrayList<>();
  private static final String ALMANACHS_IN_SUBSPACES = "0";
  private static final String ALMANACHS_IN_SPACE_AND_SUBSPACES = "1";
  private static final String ALL_ALMANACHS = "2";
  private static final String ACCESS_ALL = "0";
  private static final String ACCESS_SPACE = "1";
  private static final String ACCESS_NONE = "3";
  private static final String ICS_PREFIX = "almanach";
  private static final String DEFAULT_VIEW_PARAMETER = "defaultView";
  private Map<String, String> colors = null;
  private CalendarViewType viewMode;

  /**
   * Constructs a new AlmanachSessionController instance.
   * @param mainSessionCtrl the main session controller of the user.
   * @param context the context of the almanach component.
   */
  public AlmanachSessionController(MainSessionController mainSessionCtrl,
      ComponentContext context) {
    super(mainSessionCtrl, context, "org.silverpeas.almanach.multilang.almanach",
        "org.silverpeas.almanach.settings.almanachIcons",
        "org.silverpeas.almanach.settings.almanachSettings");
    String defaultView = getComponentParameterValue(DEFAULT_VIEW_PARAMETER);
    if (defaultView.isEmpty()) {
      viewMode = MONTHLY; // backward compatibility with previous versions of the Almanach
    } else {
      viewMode = CalendarViewType.valueOf(defaultView);
    }
  }

  /**
   * Gets the current day in the current window in time.
   * @return the current day.
   */
  public Date getCurrentDay() {
    return currentDay.getTime();
  }

  /**
   * Sets explicitly the new current day.
   * @param date the date of the new current day.
   */
  public void setCurrentDay(Date date) {
    currentDay.setTime(date);
  }

  /**
   * Gets the current event, selected by the user.
   * @return the detail about the current selected event or null if no event is selected.
   */
  public EventDetail getCurrentEvent() {
    return currentEvent;
  }

  /**
   * Sets the current event the user has selected.
   * @param event the detail of the current selected event.
   */
  public void setCurrentEvent(EventDetail event) {
    this.currentEvent = event;
  }

  /**
   * Moves the window in time to the next calendar view according to the current view mode.
   */
  public void nextView() {
    switch (viewMode) {
      case MONTHLY:
        currentDay.add(Calendar.MONTH, 1);
        break;
      case WEEKLY:
        currentDay.add(Calendar.WEEK_OF_MONTH, 1);
        break;
    }

  }

  /**
   * Moves the window in time to the previous calendar view according to the current view mode.
   */
  public void previousView() {
    switch (viewMode) {
      case MONTHLY:
        currentDay.add(Calendar.MONTH, -1);
        break;
      case WEEKLY:
        currentDay.add(Calendar.WEEK_OF_MONTH, -1);
        break;
    }
  }

  /**
   * Moves the window in time in a such way the current day is now today.
   */
  public void today() {
    currentDay = Calendar.getInstance();
  }

  /**
   * Sets the current view mode of the almanach rendering.
   * @param viewMode the view mode (monthly, weekly, ...).
   */
  public void setViewMode(final CalendarViewType viewMode) {
    this.viewMode = viewMode;
  }

  /**
   * Gets all events of the underlying almanach.
   * @return a list with the details of the events registered in the almanach.
   * @throws AlmanachException if an error occurs while getting the list of events.
   */
  public List<EventDetail> getAllEvents() throws AlmanachException {
    EventPK pk = new EventPK("", getSpaceId(), getComponentId());
    return new ArrayList<>(getAlmanachService().getAllEvents(pk));
  }

  /**
   * Gets all events resulting of the agregation of the current almanach with others'.
   * @return a list with the details of the all events in the agregation of several almanachs. If
   * the agregation for the current almanach isn't activated, then only the events of the almanach
   * are returned.
   * @throws AlmanachException if an error occurs while getting the list of events.
   */
  protected List<EventDetail> getAllAgregationEvents() throws AlmanachException {
    if (isAgregationUsed()) {
      return getAllEvents(getAgregateAlmanachIds());
    }
    return getAllEvents();
  }

  /**
   * Gets the count of almanachs agregated with the undermying one.
   * @return the number of agregated almanachs.
   */
  public int getAgregatedAlmanachsCount() {
    return getAgregateAlmanachIds().size();
  }

  /**
   * Gets the events of the specified almanachs.
   * @param instanceIds the identifiers of the almanachs.
   * @return a list with the details of the events in the specified almanachs.
   * @throws AlmanachException if an error occurs while getting the list of events.
   */
  private List<EventDetail> getAllEvents(final List<String> instanceIds) throws AlmanachException {
    EventPK pk = new EventPK("", getSpaceId(), getComponentId());
    return new ArrayList<>(
        getAlmanachService().getAllEvents(pk, instanceIds.toArray(new String[instanceIds.size()])));
  }

  /**
   * Gets the detail of the event identified by the specified identifier.
   * @param id the unique identifier of the event to get.
   * @return the detail of the event.
   * @throws AlmanachException if an error occurs while getting the detail of the event.
   * @throws AlmanachNoSuchFindEventException if no event exists with a such identifier.
   */
  public EventDetail getEventDetail(final String id)
      throws AlmanachException, AlmanachNoSuchFindEventException {
    EventDetail detail =
        getAlmanachService().getEventDetail(new EventPK(id, getSpaceId(), getComponentId()));
    if (detail != null) {
      return detail;
    }
    throw new AlmanachNoSuchFindEventException(AE_MSG1);
  }

  /**
   * Removes the event identified by the specified identifier.
   * @param id the identifier of the event to remove.
   * @throws AlmanachException if an error occurs while removing the event.
   */
  public void removeEvent(final String id) throws AlmanachException, WysiwygException {
    SilverTrace
        .info("almanach", "AlmanachSessionController.removeEvent()", "root.MSG_GEN_ENTER_METHOD");
    EventPK pk = new EventPK(id, getSpaceId(), getComponentId());
    // remove event from DB
    EventDetail event = getAlmanachService().getEventDetail(pk);
    getAlmanachService().removeEvent(pk);
    // remove attachments from filesystem
    List<SimpleDocument> documents = AttachmentServiceProvider.getAttachmentService().
        listDocumentsByForeignKey(pk, null);
    for (SimpleDocument document : documents) {
      AttachmentServiceProvider.getAttachmentService().deleteAttachment(document);
    }
    // Delete the Wysiwyg if exists

    if (WysiwygController.haveGotWysiwyg(getComponentId(), id, event.getLanguage())) {
      WysiwygController.deleteWysiwygAttachments(getComponentId(), id);
    }
    SilverTrace
        .info("almanach", "AlmanachSessionController.removeEvent()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Removes just an occurrence of the specified event. The occurrence is identified by its start
   * date.
   * @param eventDetail the detail of the event to which the occurrence belongs.
   * @param startDate the start date of the event occurrence.
   * @throws ParseException if an error occurs while parsing date infomation.
   * @throws AlmanachException if an error occurs while removing the occurrence of the event.
   */
  public void removeOccurenceEvent(EventDetail eventDetail, String startDate)
      throws ParseException, AlmanachException {


    PeriodicityException periodicityException = new PeriodicityException();
    periodicityException
        .setPeriodicityId(Integer.parseInt(eventDetail.getPeriodicity().getPK().getId()));
    periodicityException.setBeginDateException(parse(startDate));
    periodicityException.setEndDateException(parse(startDate));

    // add exception periodicity in DB
    getAlmanachService().addPeriodicityException(periodicityException);

  }

  public void prepareNewEvent(EventDetail event) {
    String volatileId =
        getSessionVolatileResourceCacheService().newVolatileIntegerIdentifierAsString();
    EventPK pk = new EventPK(volatileId);
    pk.setComponentName(getComponentId());
    event.setPK(pk);
    getSessionVolatileResourceCacheService().addComponentResource(event);
  }

  /**
   * Adds the specified event into the underlying almanach.
   * @param eventDetail the detail of the event to add.
   * @param uploadedFiles the files uploaded in the aim to be attached to the event.
   * @throws AlmanachBadParamException if the event detail isn't well defined.
   * @throws AlmanachException if an error occurs while adding the event.
   * @throws WysiwygException if an error occurs while parsing the WYSIWYG content of the event.
   */
  public EventPK addEvent(EventDetail eventDetail, Collection<UploadedFile> uploadedFiles)
      throws AlmanachBadParamException, AlmanachException, WysiwygException {
    return addEvent(eventDetail, uploadedFiles, PdcClassificationEntity.undefinedClassification());
  }

  /**
   * Adds the specified event into the underlying almanach.
   * @param eventDetail the detail of the event to add.
   * @param uploadedFiles the files uploaded in the aim to be attached to the event.
   * @throws AlmanachBadParamException if the event detail isn't well defined.
   * @throws AlmanachException if an error occurs while adding the event.
   * @throws WysiwygException if an error occurs while parsing the WYSIWYG content of the event.
   */
  public EventPK addEvent(EventDetail eventDetail, Collection<UploadedFile> uploadedFiles,
      PdcClassificationEntity classification) throws AlmanachBadParamException, AlmanachException,
      WysiwygException {
    ForeignPK volatileAttachmentSourcePK = new ForeignPK(eventDetail.getId(), getComponentId());
    EventPK eventPK = new EventPK("", "useless", getComponentId());
    eventDetail.setPK(eventPK);
    eventDetail.setDelegatorId(getUserId());

    PdcClassification withClassification = NONE_CLASSIFICATION;
    if (!classification.isUndefined()) {
      List<PdcPosition> pdcPositions = classification.getPdcPositions();
      withClassification = aPdcClassificationOfContent(eventDetail.getId(), eventDetail.
          getInstanceId()).withPositions(pdcPositions);
    }
    // Add the event
    String eventId = getAlmanachService().addEvent(eventDetail, uploadedFiles, withClassification);
    eventPK.setId(eventId);
    Date startDate = eventDetail.getStartDate();
    // currentDay
    if (startDate != null) {
      setCurrentDay(startDate);
    }

    // Attaching all documents linked to volatile news to the persisted news
    List<SimpleDocumentPK> movedDocumentPks = AttachmentServiceProvider.getAttachmentService()
        .moveAllDocuments(volatileAttachmentSourcePK, eventPK);
    if (!movedDocumentPks.isEmpty()) {
      // Change images path in wysiwyg
      WysiwygController.wysiwygPlaceHaveChanged(getComponentId(),
          volatileAttachmentSourcePK.getId(), getComponentId(), eventId);
    }

    SilverTrace
        .info("almanach", "AlmanachSessionController.addEvent()", "root.MSG_GEN_EXIT_METHOD");
    return eventPK;
  }

  /**
   * Updates the specified event into the underlying almanach.
   * @param eventDetail the detail of the event to update.
   * @throws AlmanachBadParamException if the event detail isn't well defined.
   * @throws AlmanachException if an error occurs while updating the event.
   * @throws WysiwygException if an error occurs while parsing the WYSIWYG content of the event.
   */
  public void updateEvent(EventDetail eventDetail)
      throws AlmanachBadParamException, AlmanachException, WysiwygException {
    SilverTrace
        .info("almanach", "AlmanachSessionController.updateEvent()", "root.MSG_GEN_ENTER_METHOD");
    eventDetail.getPK().setSpace(getSpaceId());
    eventDetail.getPK().setComponentName(getComponentId());
    // Update event
    getAlmanachService().updateEvent(eventDetail);
    Date startDate = eventDetail.getStartDate();
    // currentDay
    if (startDate != null) {
      setCurrentDay(startDate);
    }
    // Update the Wysiwyg if exists, create one otherwise
    if (isDefined(eventDetail.getWysiwyg())) {
      WysiwygController
          .updateFileAndAttachment(eventDetail.getDescription(getLanguage()), getComponentId(),
              eventDetail.getId(), getUserId(), getLanguage());
    } else {
      WysiwygController
          .createFileAndAttachment(eventDetail.getDescription(getLanguage()), eventDetail.getPK(),
              getUserId(), getLanguage());
    }
    SilverTrace
        .info("almanach", "AlmanachSessionController.updateEvent()", "root.MSG_GEN_EXIT_METHOD");
  }

  /**
   * Gets the remote business object for handling almanachs and events.
   * @return the remote business object.
   * @throws AlmanachException if an error occurs while getting the remote object.
   */
  protected AlmanachService getAlmanachService() throws AlmanachException {
    if (almanachService == null) {
      try {
        almanachService = AlmanachService.get();
      } catch (Exception e) {
        throw new AlmanachException("AlmanachSessionControl.getAlmanachService()",
            SilverpeasException.ERROR, "IoC error cannot retrieve AlmanachService from container", e);
      }
    }
    return almanachService;
  }

  /**
   * Builds a PDF document with the events of the underlying almanach and that satisfy the
   * specified
   * criteria key.
   * @param mode the criteria key.
   * @return the content of the PDF document as a String.
   */
  public String buildPdf(final String mode) {
    String name = "almanach" + System.currentTimeMillis() + ".pdf";
    try {
      AlmanachPdfGenerator.buildPdf(name, this, mode);
    } catch (AlmanachRuntimeException ex) {
      SilverTrace
          .warn("almanach", "AlmanachSessionController.buildPdf()", "almanach.MSG_BUILD_PDF_FAIL",
              ex);
      return null;
    }
    return FileServerUtils.getUrlToTempDir(name);
  }

  /**
   * Is this almanach instance is parameterized to use the classification plan (PdC) to classify
   * the
   * events on it.
   */
  public boolean isPdcUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("usepdc"));
  }

  /**
   * Is the weekend is taken in charge by the current underlying almanach?
   * @return true if the weekend should be displayed for the current almanach, false otherwise.
   */
  public boolean isWeekendNotVisible() {
    return StringUtil.getBooleanValue(getComponentParameterValue("weekendNotVisible"));
  }

  /**
   * Is RSS information exists for the events in the current underlying almanach?
   * @return true if the RSS stream is supported for the current almanach, false otherwise.
   */
  private boolean isUseRss() {
    return StringUtil.getBooleanValue(getComponentParameterValue("rss"));
  }

  /*
   * (non-Javadoc) @see
   * org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController#getRSSUrl ()
   */
  @Override
  public String getRSSUrl() {
    if (isUseRss()) {
      return super.getRSSUrl();
    }
    return null;
  }

  /**
   * Is the agregation is activated for the current underlying almanach?
   * @return true if the agregation is used for the current almanach, false otherwise.
   */
  public boolean isAgregationUsed() {
    return StringUtil.getBooleanValue(getComponentParameterValue("useAgregation"));
  }

  /**
   * Gets policy currently in use to access to the data of the current almanach.
   * @return the policy identifier.
   */
  private String getAccessPolicy() {
    String param = getComponentParameterValue("directAccess");
    if (!isDefined(param)) {
      return ACCESS_ALL;
    }
    return param;
  }

  /**
   * Gets the others almanach instances that are accessible from the current underlying almanach
   * instance.
   * @return a list of DTO carrying information about the others almanach instances.
   */
  public List<AlmanachDTO> getAccessibleInstances() {
    List<AlmanachDTO> accessibleInstances = new ArrayList<>();

    if (ACCESS_NONE.equals(getAccessPolicy())) {
      return null;
    }

    boolean inCurrentSpace = true;
    boolean inAllSpaces = ACCESS_ALL.equals(getAccessPolicy());

    // Get almanachIds
    String[] instanceIds = getOrganisationController()
        .getAllComponentIdsRecur(getSpaceId(), getUserId(), getComponentRootName(), inCurrentSpace,
            inAllSpaces);


    if (instanceIds.length > 1) {
      for (String instanceId : instanceIds) {

        ComponentInstLight almanachInst =
            getOrganisationController().getComponentInstLight(instanceId);

        boolean keepIt;
        if (ACCESS_SPACE.equals(getAccessPolicy())) {
          keepIt = almanachInst.getDomainFatherId().equals(getSpaceId());
        } else {
          keepIt = true;
        }

        if (keepIt) {
          SpaceInstLight si = getOrganisationController().getSpaceInstLightById(almanachInst.
              getDomainFatherId());
          String url = URLUtil.getApplicationURL() + URLUtil.getURL(null, instanceId);
          AlmanachDTO almanach = new AlmanachDTO().setInstanceId(instanceId).setLabel(almanachInst.
              getLabel()).setSpaceId(si.getName()).setUrl(url);
          accessibleInstances.add(almanach);
        }
      }
    }
    return accessibleInstances;
  }

  /**
   * Gets the identifier of the specified event as a Silverpeas object (an object that have a
   * content that can be managed in Silverpeas).
   * @param eventId the identifier of the event.
   * @return the identifier of the Silverpeas object that represents the specified event.
   * @throws AlmanachBadParamException if parameter is invalid; it doesn't represent an event
   * identifier.
   * @throws AlmanachException if the operation fail.
   */
  public int getSilverObjectId(final String eventId)
      throws AlmanachBadParamException, AlmanachException {
    return getAlmanachService().getSilverObjectId(new EventPK(eventId, getSpaceId(), getComponentId()));
  }

  /**
   * Get the color of the almanach
   * @param instanceId
   * @return color of almanach
   * @author dlesimple
   */
  public String getAlmanachColor(final String instanceId) {
    //if (colors == null) {
    colors = new HashMap<String, String>();
    colors.put(getComponentId(), getAlmanachColor(0));
    List<AlmanachDTO> almanachs = getAggregatedAlmanachs();
    if (almanachs != null) {
      for (AlmanachDTO almanach : almanachs) {
        colors.put(almanach.getInstanceId(), almanach.getColor());
      }
    }
    //}
    return colors.get(instanceId);
  }

  /**
   * Gets the almanachs that can be aggregated with the curren t underlying one.
   * @return a list of AlmanachDTO instances, each of them carrying some data about an almanach.
   */
  public List<AlmanachDTO> getAggregatedAlmanachs() {
    List<AlmanachDTO> aggregatedAlmanachs = new ArrayList<>();

    String agregationMode =
        getSettings().getString("almanachAgregationMode", ALMANACHS_IN_SUBSPACES);

    String[] instanceIds;
    boolean inCurrentSpace = false;
    boolean inAllSpaces = false;
    if (agregationMode.equals(ALMANACHS_IN_SPACE_AND_SUBSPACES)) {
      inCurrentSpace = true;
    } else if (agregationMode.equals(ALL_ALMANACHS)) {
      inCurrentSpace = true;
      inAllSpaces = true;
    }
    instanceIds = getOrganisationController()
        .getAllComponentIdsRecur(getSpaceId(), getUserId(), getComponentRootName(), inCurrentSpace,
            inAllSpaces);
    for (int i = 0; i < instanceIds.length; i++) {
      String instanceId = instanceIds[i];
      if (!instanceId.equals(getComponentId())) {
        ComponentInstLight almanachInst =
            getOrganisationController().getComponentInstLight(instanceId);
        AlmanachDTO almanach = new AlmanachDTO().setInstanceId(instanceId).
            setAggregated(isAlmanachAgregated(instanceId)).setColor(getAlmanachColor(i + 1)).
            setLabel(almanachInst.getLabel());
        aggregatedAlmanachs.add(almanach);
      }
    }

    return aggregatedAlmanachs;
  }

  /**
   * Is the specified almanach is agregated with the current underlying one.
   * @param almanachId the unique identifier of the almanach instance.
   * @return boolean true if the almanach is currently agregated with the current one.
   */
  public boolean isAlmanachAgregated(final String almanachId) {
    for (String anAlmanachId : getAgregateAlmanachIds()) {
      if (anAlmanachId.equals(almanachId)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Clears the list of the agregated almanachs.
   */
  private void clearAgregatedAlmanachs() {
    getAgregateAlmanachIds().clear();
  }

  /**
   * Updates the list of the agregated almanachs with the specified ones.
   * @param instanceIds the identifier of the new agregated almanachs.
   */
  public void updateAgregatedAlmanachs(final String[] instanceIds) {
    clearAgregatedAlmanachs();
    if (instanceIds != null && instanceIds.length > 0) {
      getAgregateAlmanachIds().addAll(Arrays.asList(instanceIds));
    }
  }

  /**
   * Gets the color with which the events in an almanach should be rendered.
   * @param position in the array of supported colors. 0 is for the current almanach, other
   * positions are for the agregated almanachs.
   * @return the HTML/CSS code of the color.
   * @author dlesimple
   */
  private String getAlmanachColor(int position) {
    String almanachColor = getSettings().getString("almanachColor" + position, "");
    return almanachColor;
  }

  /**
   * @param eventId
   * @return
   * @throws
   * @throws AlmanachException
   * @throws AlmanachNoSuchFindEventException
   */
  public String initAlertUser(final String eventId)
      throws AlmanachException, AlmanachNoSuchFindEventException {
    AlertUser sel = getAlertUser();
    sel.resetAll();
    sel.setHostSpaceName(getSpaceLabel());
    sel.setHostComponentId(getComponentId());
    Pair<String, String> hostComponentName = new Pair<>(getComponentLabel(), null);
    sel.setHostComponentName(hostComponentName);
    sel.setNotificationMetaData(getAlertNotificationEvent(eventId));
    // l'url de nav vers alertUserPeas et demandée à AlertUser et retournée
    return AlertUser.getAlertUserURL();
  }

  private synchronized NotificationMetaData getAlertNotificationEvent(final String eventId)
      throws AlmanachException {
    // création des données ...
    EventPK eventPK = new EventPK(eventId, getSpaceId(), getComponentId());
    String senderName = getUserDetail().getDisplayedName();
    EventDetail eventDetail = getAlmanachService().getEventDetail(eventPK);

    // recherche de l’emplacement de l’évènement
    String htmlPath = getAlmanachService().getHTMLPath(eventPK);

    // création des notifications
    LocalizationBundle message =
        ResourceLocator.getLocalizationBundle("org.silverpeas.almanach.multilang.almanach",
            DisplayI18NHelper.getDefaultLanguage());
    String subject = getNotificationSubject(message);
    String body = getNotificationBody(eventDetail, htmlPath, message, senderName);
    NotificationMetaData notifMetaData =
        new NotificationMetaData(NotificationParameters.NORMAL, subject, body);

    for (String language : DisplayI18NHelper.getLanguages()) {
      // création des messages ...
      message =
          ResourceLocator.getLocalizationBundle("org.silverpeas.almanach.multilang.almanach",
              language);

      subject = getNotificationSubject(message);
      body = getNotificationBody(eventDetail, htmlPath, message, senderName);
      notifMetaData.addLanguage(language, subject, body);

      String url = getObjectUrl(eventDetail);
      Link link = new Link(url, getNotificationLinkLabel(message));
      notifMetaData.setLink(link, language);
    }
    notifMetaData.setComponentId(eventPK.getInstanceId());
    notifMetaData.setSender(getUserId());
    notifMetaData.displayReceiversInFooter();

    return notifMetaData;
  }

  private String getNotificationSubject(final LocalizationBundle message) {
    return message.getString("notifSubject");
  }

  private String getNotificationBody(final EventDetail eventDetail, final String htmlPath,
      final LocalizationBundle message, final String senderName) {
    StringBuilder messageText = new StringBuilder();
    messageText.append(senderName).append(" ");
    messageText.append(message.getString("notifInfo")).append(" ");
    messageText.append(eventDetail.getName()).append(" ");
    messageText.append(message.getString("notifInfo2")).append("\n\n");
    messageText.append(message.getString("path")).append(" : ").append(htmlPath);
    return messageText.toString();
  }

  private String getNotificationLinkLabel(final LocalizationBundle message) {
    return message.getString("notifLinkLabel");
  }

  private String getObjectUrl(EventDetail eventDetail) {
    return URLUtil.getURL(null, getComponentId()) + eventDetail.getURL();
  }

  @Override
  public void close() {
    if (almanachService != null) {
      almanachService = null;
    }
  }

  /**
   * Update event occurence (cas particulier de modification d'une occurence d'événement
   * périodique)
   * @param event
   * @param dateDebutIteration
   * @param dateFinIteration
   * @throws AlmanachBadParamException
   * @throws AlmanachException
   * @throws WysiwygException
   * @throws ParseException
   */
  public void updateEventOccurence(final EventDetail event, final String dateDebutIteration,
      final String dateFinIteration)
      throws AlmanachBadParamException, AlmanachException, WysiwygException, ParseException {

    // Supprime l'occurence : exception dans la série
    removeOccurenceEvent(event, dateDebutIteration);
    // Ajoute un nouvel événement indépendant
    event.setPeriodicity(null);
    addEvent(event, null);

  }

  /**
   * Gets a view in time of the current underlying almanach. The view depends on the current
   * selected view mode and the current selected window in time.
   * @return an AlmanachCalendarView instance.
   * @throws AlmanachException if an error occurs while getting the calendar view.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the almanach cannot be
   * found.
   */
  public AlmanachCalendarView getAlmanachCalendarView()
      throws AlmanachException, AlmanachNoSuchFindEventException {
    AlmanachCalendarView view;
    switch (viewMode) {
      case YEARLY:
        view = getYearlyAlmanachCalendarView();
        break;
      case MONTHLY:
        view = getMonthlyAlmanachCalendarView();
        break;
      case WEEKLY:
        view = getWeekyAlmanachCalendarView();
        break;
      case NEXT_EVENTS:
        view = getAlmanachCalendarViewOnTheNextEvents(isAgregationUsed());
        break;
      default:
        throw new UnsupportedOperationException(
            "The calendar view mode " + viewMode + " isn't yet supported by the almanach");

    }
    return view;
  }

  /**
   * Gets a view in the current year of the current underlying almanach.
   * @return an AlmanachCalendarView instance.
   * @throws AlmanachException if an error occurs while getting the calendar view.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the almanach cannot be
   * found.
   */
  public AlmanachCalendarView getYearlyAlmanachCalendarView()
      throws AlmanachException, AlmanachNoSuchFindEventException {
    AlmanachDTO almanachDTO = getAlmanachDTO(isAgregationUsed());
    AlmanachCalendarView view =
        new AlmanachCalendarView(almanachDTO, currentDay.getTime(), YEARLY, getLanguage());
    if (isWeekendNotVisible()) {
      view.unsetWeekendVisible();
    }
    view.setEvents(listCurrentYearEvents(getAggregationAlmanachIds()));
    return view;
  }

  /**
   * Gets a view in the current month of the current underlying almanach.
   * @return an AlmanachCalendarView instance.
   * @throws AlmanachException if an error occurs while getting the calendar view.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the almanach cannot be
   * found.
   */
  public AlmanachCalendarView getMonthlyAlmanachCalendarView()
      throws AlmanachException, AlmanachNoSuchFindEventException {

    AlmanachDTO almanachDTO = getAlmanachDTO(isAgregationUsed());
    AlmanachCalendarView view =
        new AlmanachCalendarView(almanachDTO, currentDay.getTime(), MONTHLY, getLanguage());
    if (isWeekendNotVisible()) {
      view.unsetWeekendVisible();
    }
    view.setEvents(listCurrentMonthEvents(getAggregationAlmanachIds()));
    return view;
  }

  /**
   * Gets a view in the current week of the current underlying almanach.
   * @return an AlmanachCalendarView instance.
   * @throws AlmanachException if an error occurs while getting the calendar view.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the almanach cannot be
   * found.
   */
  public AlmanachCalendarView getWeekyAlmanachCalendarView()
      throws AlmanachException, AlmanachNoSuchFindEventException {

    AlmanachDTO almanachDTO = getAlmanachDTO(isAgregationUsed());
    AlmanachCalendarView view =
        new AlmanachCalendarView(almanachDTO, currentDay.getTime(), WEEKLY, getLanguage());
    if (isWeekendNotVisible()) {
      view.unsetWeekendVisible();
    }
    view.setEvents(listCurrentWeekEvents(getAggregationAlmanachIds()));
    return view;
  }

  /**
   * Gets a view on the next events that will occur and that are defined in the current underlying
   * almanach.
   * @param aggregated is the calendar view should contains also the events of aggregated
   * almanachs?
   * @return an AlmanachCalendarView instance.
   * @throws AlmanachException if an error occurs while getting the calendar view.
   * @throws AlmanachNoSuchFindEventException if a detail about an event in the almanach cannot be
   * found.
   */
  public AlmanachCalendarView getAlmanachCalendarViewOnTheNextEvents(boolean aggregated)
      throws AlmanachException, AlmanachNoSuchFindEventException {
    AlmanachDTO almanachDTO = getAlmanachDTO(aggregated);
    AlmanachCalendarView view =
        new AlmanachCalendarView(almanachDTO, currentDay.getTime(), NEXT_EVENTS, getLanguage());

    if (isWeekendNotVisible()) {
      view.unsetWeekendVisible();
    }
    if (aggregated) {
      view.setEvents(listNextEvents(getAggregationAlmanachIds()));
    } else {
      view.setEvents(listNextEvents(getComponentId()));
    }
    view.setLabel(getString("almanach.nextEvents"));
    return view;
  }

  /**
   * Gets the URL of the ICS representation of the current almamach.
   * @return the URL of the almanach ICS.
   */
  public String getAlmanachICSURL() {
    return "/services/almanach/ics/" + getComponentId() + "?userId=" + getUserId() + "&amp;login=" +
        getUserDetail().getLogin() + "&amp;password=" +
        getOrganisationController().getUserFull(getUserId()).getPassword();
  }

  /**
   * Exports the current almanach in iCal format. The iCal file is generated into the temporary
   * directory. If there is no events to export, a NoDataToExportException exception is then
   * thrown.
   * @return the iCal file name into which is generated the current alamanch.
   * @throws ExportException if an error occurs while exporting the almanach in iCal format. The
   * errors can come from a failure on getting the events to export, the fact there is no events to
   * export (empty almanach) or the failure of the export process itself.
   * @throws IOException if an error occurs while creating or opening the file into which the
   * export
   * will be done. Such errors can be come from a forbidden write granting, and so on.
   */
  public String exportToICal() throws ExportException, IOException {
    String icsFileName = ICS_PREFIX + "-" + getComponentId() + ".ics";
    String icsFilePath = FileRepositoryManager.getTemporaryPath() + icsFileName;
    List<CalendarEvent> eventsToExport;
    try {
      eventsToExport = asCalendarEvents(getAllEvents());
    } catch (Exception ex) {
      SilverTrace.error("almanach", getClass().getSimpleName() + ".exportToICal()",
          "almanach.EXE_GET_ALL_EVENTS_FAIL", ex);
      throw new ExportException(ex.getMessage(), ex);
    }
    Exporter<ExportableCalendar> iCalExporter = ExporterProvider.getICalExporter();
    FileWriter fileWriter = new FileWriter(icsFilePath);
    try {
      iCalExporter.export(withWriter(fileWriter), ExportableCalendar.with(eventsToExport));
    } catch (ExportException ex) {
      File fileToDelete = new File(icsFilePath);
      if (fileToDelete.exists()) {
        FileUtils.deleteQuietly(fileToDelete);
      }
      throw ex;
    }

    return icsFileName;
  }

  /**
   * Gets the occurrences of the events defined in the specified almanachs and in the current
   * selected year.
   * @param almanachIds the identifier of the almanachs the events belongs to.
   * @return a list of event occurrences decorated with rendering features.
   * @throws AlmanachException if an error occurs while getting the list of event occurrences.
   * @throws AlmanachNoSuchFindEventException if the detail about an event cannot be found.
   */
  private List<DisplayableEventOccurrence> listCurrentYearEvents(String... almanachIds)
      throws AlmanachException {
    List<EventOccurrence> occurrencesInYear = getAlmanachService().getEventOccurrencesInPeriod(
        Period.from(currentDay.getTime(), PeriodType.year, getLanguage()), almanachIds);
    return DisplayableEventOccurrence.decorate(occurrencesInYear);
  }

  /**
   * Gets the occurrences of the events defined in the specified almanachs and in the current
   * selected month.
   * @param almanachIds the identifier of the almanachs the events belongs to.
   * @return a list of event occurrences decorated with rendering features.
   * @throws AlmanachException if an error occurs while getting the list of event occurrences.
   * @throws AlmanachNoSuchFindEventException if the detail about an event cannot be found.
   */
  private List<DisplayableEventOccurrence> listCurrentMonthEvents(String... almanachIds)
      throws AlmanachException {
    List<EventOccurrence> occurrencesInMonth = getAlmanachService().
        getEventOccurrencesInPeriod(
            Period.from(currentDay.getTime(), PeriodType.month, getLanguage()), almanachIds);
    return DisplayableEventOccurrence.decorate(occurrencesInMonth);
  }

  /**
   * Gets the occurrences of the events defined in the specified almanachs and in the current
   * selected week.
   * @param almanachIds the identifier of the almanachs the events belongs to.
   * @return a list of event occurrences decorated with rendering features.
   * @throws AlmanachException if an error occurs while getting the list of event occurrences.
   * @throws AlmanachNoSuchFindEventException if the detail about an event cannot be found.
   */
  private List<DisplayableEventOccurrence> listCurrentWeekEvents(String... almanachIds)
      throws AlmanachException {
    List<EventOccurrence> occurrencesInWeek = getAlmanachService().getEventOccurrencesInPeriod(
        Period.from(currentDay.getTime(), PeriodType.week, getLanguage()), almanachIds);
    return DisplayableEventOccurrence.decorate(occurrencesInWeek);
  }

  /**
   * Lists the occurrences of the next events defined in the specified almanachs.
   * @param almanachIds the identifier of the almanachs the events belongs to.
   * @return an ordered list of event occurrences decorated with rendering features.
   * @throws AlmanachException if an error occurs while getting the list of event occurrences.
   * @throws AlmanachNoSuchFindEventException if the detail about an event cannot be found.
   */
  private List<DisplayableEventOccurrence> listNextEvents(String... almanachIds)
      throws AlmanachException {
    List<EventOccurrence> nextOccurrences = getAlmanachService().getNextEventOccurrences(almanachIds);
    return DisplayableEventOccurrence.decorate(nextOccurrences);
  }

  /**
   * Gets a DTO of the almanach to pass to the view.
   * @param aggregated is the DTO should transfer data about an aggregated almanach?
   * @return a DTO of the current almanach.
   */
  private AlmanachDTO getAlmanachDTO(boolean aggregated) {
    return new AlmanachDTO().setColor(getAlmanachColor(getComponentId())).
        setInstanceId(getComponentId()).setLabel(getComponentLabel()).setAggregated(aggregated)
        .setUrl(getComponentUrl());
  }

  /**
   * Converts the specified details on almanach events into a calendar event.
   * @param eventDetails details about some events in one or several almanachs.
   * @return the calendar events corresponding to the almanach events.
   */
  private List<CalendarEvent> asCalendarEvents(final List<EventDetail> eventDetails) {
    CalendarEventEncoder encoder = new CalendarEventEncoder();
    return encoder.encode(eventDetails);
  }

  /**
   * Gets the identifier of the almanachs that compound the current aggregated almanach. Among
   * them,
   * the identifier of the current almanach is also provided.
   * @return an array of almanach identifiers.
   */
  private String[] getAggregationAlmanachIds() {
    String[] almanachIds = new String[getAgregateAlmanachIds().size() + 1];
    almanachIds = getAgregateAlmanachIds().toArray(almanachIds);
    almanachIds[almanachIds.length - 1] = getComponentId();
    return almanachIds;
  }

  private List<String> getAgregateAlmanachIds() {
    return agregateAlmanachsIds;
  }

}

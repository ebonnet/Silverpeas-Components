/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.almanach.service;

import org.silverpeas.core.admin.component.model.ComponentInstLight;
import org.silverpeas.core.admin.space.SpaceInst;
import org.silverpeas.core.index.indexing.model.IndexEntryKey;
import org.silverpeas.core.persistence.jdbc.bean.IdPK;
import org.silverpeas.core.persistence.jdbc.bean.PersistenceException;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAO;
import org.silverpeas.core.persistence.jdbc.bean.SilverpeasBeanDAOFactory;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.PeriodList;
import net.fortuna.ical4j.model.Property;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.CalScale;
import net.fortuna.ical4j.model.property.ExDate;
import net.fortuna.ical4j.model.property.RRule;
import org.silverpeas.core.contribution.attachment.AttachmentServiceProvider;
import org.silverpeas.core.contribution.attachment.model.SimpleDocument;
import org.silverpeas.components.almanach.AlmanachContentManager;
import org.silverpeas.components.almanach.model.EventDAO;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventOccurrence;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.components.almanach.model.Periodicity;
import org.silverpeas.components.almanach.model.PeriodicityException;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.pdc.PdcServiceProvider;
import org.silverpeas.core.pdc.pdc.model.PdcClassification;
import org.silverpeas.core.pdc.pdc.service.PdcClassificationService;
import org.silverpeas.core.io.upload.UploadedFile;
import org.silverpeas.core.util.CollectionUtil;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.contribution.content.wysiwyg.service.WysiwygController;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.transaction.Transactional;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static org.silverpeas.core.util.DateUtil.*;
import static org.silverpeas.core.util.StringUtil.isDefined;

/**
 * CDI bean to manage the almanach application
 */
@Singleton
@Transactional(Transactional.TxType.SUPPORTS)
public class DefaultAlmanachService implements AlmanachService {

  private static final SettingBundle settings =
      ResourceLocator.getSettingBundle("org.silverpeas.almanach.settings.almanachSettings");
  private AlmanachContentManager almanachContentManager = null;
  private SilverpeasBeanDAO<Periodicity> eventPeriodicityDAO = null;
  private SilverpeasBeanDAO<PeriodicityException> periodicityExceptionDAO = null;
  private EventDAO eventDAO = new EventDAO();

  @Inject
  private OrganizationController organizationController;

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getAllEvents(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
  public Collection<EventDetail> getAllEvents(EventPK pk) {

    try {
      Collection<EventDetail> events = getEventDAO().findAllEvents(pk.getInstanceId());
      return events;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getAllEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL", e);
    }
  }

  /**
   * Get all events
   * @param pk the event primary key
   * @param instanceIds array of instanceId
   * @return Collection of Events
   */
  @Override
  public Collection<EventDetail> getAllEvents(EventPK pk, String[] instanceIds) {

    try {
      String[] almanachIds = Arrays.copyOf(instanceIds, instanceIds.length + 1);
      almanachIds[instanceIds.length] = pk.getInstanceId();
      Collection<EventDetail> events = getEventDAO().findAllEvents(almanachIds);
      return events;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getAllEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getEvents(java.util.Collection)
   */
  @Override
  public Collection<EventDetail> getEvents(Collection<EventPK> pks) {

    try {
      Collection<EventDetail> events = getEventDAO().findAllEventsByPK(pks);
      return events;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getEvents()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_EVENTS_FAIL", e);
    }
  }

  /**
   * Get Event Detail
   * @param pk
   * @return the corresponding event.
   */
  @Override
  public EventDetail getEventDetail(EventPK pk) {

    try {
      return getEventDAO().findEventByPK(pk);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getEventDetail()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_EVENT_DETAIL_FAIL", e);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * addEvent(com.stratelia.webactiv.almanach.model.EventDetail)
   */
  @Override
  public String addEvent(EventDetail event, Collection<UploadedFile> uploadedFiles) {
    return addEvent(event, uploadedFiles, PdcClassification.NONE_CLASSIFICATION);
  }

  @Override
  public String addEvent(EventDetail event, Collection<UploadedFile> uploadedFiles,
      PdcClassification classification) {

    checkEventDates(event);
    Connection connection = null;
    try {
      connection = DBUtil.openConnection();
      String id = getEventDAO().addEvent(connection, event);
      event.setPK(new EventPK(id, event.getPK()));

      // manage periodicity
      if (event.getPeriodicity() != null) {
        Periodicity periodicity = event.getPeriodicity();
        periodicity.setEventId(Integer.parseInt(id));
        // Add the periodicity
        addPeriodicity(periodicity);
      }
      createSilverContent(connection, event, event.getCreatorId());
      if (!classification.isEmpty()) {
        PdcClassificationService service = PdcServiceProvider.getPdcClassificationService();
        classification.ofContent(event.getId());
        service.classifyContent(event, classification);
      }
      WysiwygController.createUnindexedFileAndAttachment(event.getDescription(event.getLanguage()),
          event.getPK(), event.getDelegatorId(), event.getLanguage());
      // Attach uploaded files
      if (CollectionUtil.isNotEmpty(uploadedFiles)) {
        for (UploadedFile uploadedFile : uploadedFiles) {
          // Register attachment
          uploadedFile.registerAttachment(event.getPK(), event.getLanguage(), false);
        }
      }
      createIndex(event);
      return id;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.addEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_ADD_EVENT_FAIL", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /**
   * updateEvent() update the event entry, specified by the pk, in the database
   */
  @Override
  public void updateEvent(EventDetail event) {

    checkEventDates(event);
    try {
      getEventDAO().updateEvent(event);

      Periodicity previousPeriodicity = getPeriodicity(event.getPK().getId());
      Periodicity currentPeriodicity = event.getPeriodicity();
      if (previousPeriodicity == null) {
        if (currentPeriodicity != null) {

          // Add the periodicity
          currentPeriodicity.setEventId(Integer.parseInt(event.getPK().getId()));
          addPeriodicity(currentPeriodicity);
        }
      } else {// lastPeriodicity != null
        if (currentPeriodicity == null) {
          // Remove the periodicity and Exceptions
          removePeriodicity(previousPeriodicity);
        } else {
          // Update the periodicity
          currentPeriodicity.setPK(previousPeriodicity.getPK());
          currentPeriodicity.setEventId(Integer.parseInt(event.getPK().getId()));
          updatePeriodicity(currentPeriodicity);
        }
      }

      createIndex(event);
      updateSilverContentVisibility(event);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.updateEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_UPDATE_EVENT_FAIL", e);
    }
  }

  /**
   * removeEvent() remove the Event entry specified by the pk
   */
  @Override
  public void removeEvent(EventPK pk) {

    Connection connection = null;
    try {
      connection = DBUtil.openConnection();
      // remove periodicity and periodicity exceptions
      Periodicity periodicity = getPeriodicity(pk.getId());
      if (periodicity != null) {
        removeAllPeriodicityException(periodicity.getPK().getId());
        removePeriodicity(periodicity);
      }
      getEventDAO().removeEvent(connection, pk);
      deleteIndex(pk);
      deleteSilverContent(connection, pk);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.removeEvent()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_REMOVE_EVENT_FAIL", e);
    } finally {
      DBUtil.close(connection);
    }
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * createIndex(com.stratelia.webactiv.almanach.model.EventDetail)
   */
  @Override
  public void createIndex(EventDetail detail) {

    try {
      FullIndexEntry indexEntry;
      indexEntry =
          new FullIndexEntry(detail.getPK().getComponentName(), "Event", detail.getPK().getId());
      indexEntry.setTitle(detail.getTitle());
      indexEntry = updateIndexEntryWithWysiwygContent(indexEntry, detail);
      indexEntry.setCreationUser(detail.getDelegatorId());
      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (Exception e) {
      SilverLogger.getLogger(this).error("Index creation failure", e);
    }
  }

  /**
   * Update Index Entry
   * @param indexEntry the index entry
   * @param eventDetail the event detail
   * @return FullIndexEntry
   */
  private FullIndexEntry updateIndexEntryWithWysiwygContent(FullIndexEntry indexEntry,
      EventDetail eventDetail) {
    EventPK eventPK = eventDetail.getPK();
    if (eventPK != null) {

      WysiwygController.addToIndex(indexEntry, new ForeignPK(eventPK), eventDetail.getLanguage());
    }

    return indexEntry;
  }

  /**
   * @param eventPK the event primary key
   */
  private void deleteIndex(EventPK eventPK) {

    IndexEntryKey indexEntry =
        new IndexEntryKey(eventPK.getComponentName(), "Event", eventPK.getId());
    IndexEngineProxy.removeIndexEntry(indexEntry);
  }

  /**
   * @return
   */
  private SilverpeasBeanDAO<Periodicity> getEventPeriodicityDAO() {
    if (eventPeriodicityDAO == null) {
      try {
        eventPeriodicityDAO =
            SilverpeasBeanDAOFactory.getDAO("org.silverpeas.components.almanach.model.Periodicity");
      } catch (PersistenceException pe) {
        throw new AlmanachRuntimeException("DefaultAlmanachService.getEventPeriodicityDAO()",
            SilverpeasRuntimeException.ERROR, "almanach.EX_PERSISTENCE_PERIODICITY", pe);
      }
    }
    return eventPeriodicityDAO;
  }

  /**
   * @param periodicity the periodicity identifier
   */
  private void addPeriodicity(Periodicity periodicity) {


    try {
      IdPK pk = new IdPK();
      periodicity.setPK(pk);
      getEventPeriodicityDAO().add(periodicity);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.addPeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_ADD_PERIODICITY", e);
    }
  }

  /**
   * @param eventId the event identifier
   * @return periodicity
   */
  private Periodicity getPeriodicity(String eventId) {

    Periodicity periodicity = null;
    try {
      IdPK pk = new IdPK();
      Collection<?> list = getEventPeriodicityDAO().findByWhereClause(pk, "eventId = " + eventId);
      if (list != null && list.size() > 0) {
        periodicity = (Periodicity) list.iterator().next();
      }
      return periodicity;
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getPeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_GET_PERIODICITY", e);
    }
  }

  private void removePeriodicity(Periodicity periodicity) {


    try {
      IdPK pk = new IdPK();
      pk.setId(periodicity.getPK().getId());
      getEventPeriodicityDAO().remove(pk);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.removePeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_REMOVE_PERIODICITY", e);
    }
  }

  private void updatePeriodicity(Periodicity periodicity) {


    try {
      getEventPeriodicityDAO().update(periodicity);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.updatePeriodicity()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_UPDATE_PERIODICITY", e);
    }
  }

  /**
   * @return
   */
  private SilverpeasBeanDAO<PeriodicityException> getPeriodicityExceptionDAO() {
    if (periodicityExceptionDAO == null) {
      try {
        periodicityExceptionDAO = SilverpeasBeanDAOFactory
            .getDAO("org.silverpeas.components.almanach.model.PeriodicityException");
      } catch (PersistenceException pe) {
        throw new AlmanachRuntimeException("DefaultAlmanachService.getPeriodicityExceptionDAO()",
            SilverpeasRuntimeException.ERROR, "almanach.EX_PERSISTENCE_PERIODICITY_EXCEPTION", pe);
      }
    }
    return periodicityExceptionDAO;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * addPeriodicityException (com.stratelia.webactiv.almanach.model.PeriodicityException)
   */
  @Override
  public void addPeriodicityException(PeriodicityException periodicityException) {
    try {
      IdPK pk = new IdPK();
      periodicityException.setPK(pk);
      getPeriodicityExceptionDAO().add(periodicityException);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.addPeriodicityException()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_ADD_PERIODICITY_EXCEPTION", e);
    }
  }

  /**
   * @param periodicityId the periodicity identifier
   * @return
   */
  public Collection<PeriodicityException> getListPeriodicityException(String periodicityId) {

    try {
      IdPK pk = new IdPK();
      return getPeriodicityExceptionDAO().findByWhereClause(pk, "periodicityId = " + periodicityId);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getListPeriodicityException()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_GET_PERIODICITY_EXCEPTION", e);
    }
  }

  /**
   * @param periodicityId the periodicity identifier
   */
  public void removeAllPeriodicityException(String periodicityId) {

    try {
      IdPK pk = new IdPK();
      getPeriodicityExceptionDAO().removeWhere(pk, "periodicityId = " + periodicityId);
    } catch (PersistenceException e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.removeAllPeriodicityException()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_REMOVE_PERIODICITY_EXCEPTION", e);
    }
  }

  @Override
  public Calendar getICal4jCalendar(Collection<EventDetail> events, String language) {

    Calendar calendarAlmanach = new Calendar();
    calendarAlmanach.getProperties().add(CalScale.GREGORIAN);
    for (EventDetail evtDetail : events) {
      Periodicity periodicity = evtDetail.getPeriodicity();
      ExDate exceptionDates = null;
      if (periodicity != null) {
        evtDetail.setPeriodicity(periodicity);
        exceptionDates = generateExceptionDate(periodicity);
      }
      VEvent eventIcal4jCalendar = evtDetail.icalConversion(exceptionDates);
      calendarAlmanach.getComponents().add(eventIcal4jCalendar);
    }
    return calendarAlmanach;
  }

  public RRule generateRecurrenceRule(Periodicity periodicity) {
    return periodicity.generateRecurrenceRule();
  }

  public ExDate generateExceptionDate(Periodicity periodicity) {
    // Exceptions de périodicité
    Collection<PeriodicityException> listException =
        getListPeriodicityException(periodicity.getPK().getId());
    PeriodicityException periodicityException;
    DateList dateList = new DateList();
    java.util.Calendar calDateException = java.util.Calendar.getInstance();
    java.util.Calendar calDateFinException = java.util.Calendar.getInstance();
    for (final PeriodicityException aListException : listException) {
      periodicityException = aListException;
      calDateException.setTime(periodicityException.getBeginDateException());
      calDateFinException.setTime(periodicityException.getEndDateException());
      while (calDateException.before(calDateFinException) ||
          calDateException.equals(calDateFinException)) {
        dateList.add(new DateTime(calDateException.getTime()));
        calDateException.add(java.util.Calendar.DATE, 1);
      }
    }
    return new ExDate(dateList);
  }

  /**
   * @param calendarAlmanach
   * @param currentDay
   * @param spaceId the space identifier
   * @param instanceId the component instance identifier
   * @return
   */
  @Override
  public Collection<EventDetail> getListRecurrentEvent(Calendar calendarAlmanach,
      java.util.Calendar currentDay, String spaceId, String instanceId, boolean yearScope) {
    java.util.Calendar today = currentDay;

    // transformation des VEvent du Calendar ical4j en EventDetail
    boolean isYear = false;
    if (currentDay == null) {
      today = java.util.Calendar.getInstance();
      isYear = true;
    }

    java.util.Calendar firstDayMonth = java.util.Calendar.getInstance();
    firstDayMonth.set(java.util.Calendar.YEAR, today.get(java.util.Calendar.YEAR));
    firstDayMonth.set(java.util.Calendar.MONTH, today.get(java.util.Calendar.MONTH));
    firstDayMonth.set(java.util.Calendar.DATE, 1);
    firstDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    firstDayMonth.set(java.util.Calendar.MINUTE, 0);
    firstDayMonth.set(java.util.Calendar.SECOND, 0);
    firstDayMonth.set(java.util.Calendar.MILLISECOND, 0);
    if (yearScope) {
      firstDayMonth.set(java.util.Calendar.MONTH, 0);
    }

    java.util.Calendar lastDayMonth = java.util.Calendar.getInstance();
    lastDayMonth.setTime(firstDayMonth.getTime());
    if (yearScope) {
      lastDayMonth.add(java.util.Calendar.YEAR, 1);
    } else {
      if (isYear) {
        lastDayMonth.add(java.util.Calendar.YEAR, 1);
      } else {
        lastDayMonth.add(java.util.Calendar.MONTH, 1);
      }
    }
    lastDayMonth.set(java.util.Calendar.HOUR_OF_DAY, 0);
    lastDayMonth.set(java.util.Calendar.MINUTE, 0);
    lastDayMonth.set(java.util.Calendar.SECOND, 0);
    lastDayMonth.set(java.util.Calendar.MILLISECOND, 0);

    Period monthPeriod =
        new Period(new DateTime(firstDayMonth.getTime()), new DateTime(lastDayMonth.getTime()));

    ComponentList componentList = calendarAlmanach.getComponents(Component.VEVENT);
    Iterator<VEvent> itVEvent = componentList.iterator();

    List<EventDetail> events = new ArrayList<EventDetail>();
    while (itVEvent.hasNext()) {
      VEvent eventIcal4jCalendar = itVEvent.next();
      String idEvent = eventIcal4jCalendar.getProperties().getProperty(Property.UID).getValue();
      // Récupère l'événement
      EventDetail evtDetail = getEventDetail(new EventPK(idEvent, spaceId, instanceId));

      PeriodList periodList = eventIcal4jCalendar.calculateRecurrenceSet(monthPeriod);
      Iterator<Period> itPeriod = periodList.iterator();
      while (itPeriod.hasNext()) {
        Period recurrencePeriod = itPeriod.next();
        // Modification des dates de l'EventDetail
        EventDetail copy = new EventDetail(evtDetail.getPK(), evtDetail.getTitle(),
            new Date(recurrencePeriod.getStart().getTime()),
            new Date(recurrencePeriod.getEnd().getTime()));
        copy.setPriority(evtDetail.getPriority());
        copy.setNameDescription(evtDetail.getNameDescription());
        copy.setStartHour(evtDetail.getStartHour());
        copy.setEndHour(evtDetail.getEndHour());
        copy.setPlace(evtDetail.getPlace());
        copy.setEventUrl(evtDetail.getEventUrl());
        events.add(copy);
      }
    }
    // sort event on begin date asc
    Collections.sort(events, new Comparator<EventDetail>() {
      @Override
      public int compare(EventDetail event1, EventDetail event2) {
        return (event1.getStartDate().compareTo(event2.getStartDate()));
      }
    });
    return events;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getSilverObjectId(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
  public int getSilverObjectId(EventPK eventPK) {

    int silverObjectId;
    EventDetail detail;
    try {
      silverObjectId = getAlmanachContentManager()
          .getSilverObjectId(eventPK.getId(), eventPK.getComponentName());
      if (silverObjectId == -1) {
        detail = getEventDetail(eventPK);
        silverObjectId = createSilverContent(null, detail, detail.getDelegatorId());
      }
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getSilverObjectId()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
    return silverObjectId;
  }

  /**
   * @param con the database connection
   * @param eventDetail the event detail
   * @param creatorId the creator identifier
   * @return
   */
  private int createSilverContent(Connection con, EventDetail eventDetail, String creatorId) {

    try {
      return getAlmanachContentManager().createSilverContent(con, eventDetail, creatorId);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.createSilverContent()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param con the database connection
   * @param eventPK the event primary key
   */
  private void deleteSilverContent(Connection con, EventPK eventPK) {

    try {
      getAlmanachContentManager().deleteSilverContent(con, eventPK);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @param eventDetail the event detail
   */
  private void updateSilverContentVisibility(EventDetail eventDetail) {
    try {
      getAlmanachContentManager().updateSilverContentVisibility(eventDetail);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.deleteSilverContent()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * @return
   */
  private AlmanachContentManager getAlmanachContentManager() {
    if (almanachContentManager == null) {
      almanachContentManager = new AlmanachContentManager();
    }
    return almanachContentManager;
  }

  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getAttachments(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
  public Collection<SimpleDocument> getAttachments(EventPK eventPK) {

    try {
      Collection<SimpleDocument> attachmentList = AttachmentServiceProvider.getAttachmentService().
          listDocumentsByForeignKey(eventPK, null);

      return attachmentList;
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getAttachments()",
          SilverpeasRuntimeException.ERROR, "almanach.EX_IMPOSSIBLE_DOBTENIR_LES_FICHIERSJOINTS",
          e);
    }
  }


  /*
   * (non-Javadoc)
   * @see com.stratelia.webactiv.almanach.control.ejb.AlmanachBmBusinessSkeleton#
   * getHTMLPath(com.stratelia.webactiv.almanach.model.EventPK)
   */
  @Override
  public String getHTMLPath(EventPK eventPK) {
    String htmlPath = "";
    try {
      htmlPath =
          getSpacesPath(eventPK.getInstanceId()) + getComponentLabel(eventPK.getInstanceId());
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getHTMLPath()",
          SilverpeasRuntimeException.ERROR,
          "gallery.EX_IMPOSSIBLE_DOBTENIR_LES_EMPLACEMENTS_DE_LA_PUBLICATION", e);
    }
    return htmlPath;
  }

  /**
   * @param componentId the component identifier
   * @return
   */
  private String getSpacesPath(String componentId) {
    String spacesPath = "";
    List<SpaceInst> spaces = getOrganizationController().getSpacePathToComponent(componentId);
    Iterator<SpaceInst> iSpaces = spaces.iterator();
    SpaceInst spaceInst;
    while (iSpaces.hasNext()) {
      spaceInst = iSpaces.next();
      spacesPath += spaceInst.getName();
      spacesPath += " > ";
    }
    return spacesPath;
  }

  /**
   * @param componentId the component identifier
   * @return
   */
  private String getComponentLabel(String componentId) {
    ComponentInstLight component = getOrganizationController().getComponentInstLight(componentId);
    String componentLabel = "";
    if (component != null) {
      componentLabel = component.getLabel();
    }
    return componentLabel;
  }

  /**
   * @return
   */
  private OrganizationController getOrganizationController() {
    return organizationController;
  }

  @Override
  public List<EventOccurrence> getEventOccurrencesInPeriod(
      org.silverpeas.core.date.period.Period period,
      String... almanachIds) {
    try {
      Collection<EventDetail> events = getEventDAO().findAllEventsInPeriod(period, almanachIds);
      EventOccurrenceGenerator occurrenceGenerator =
          EventOccurrenceGeneratorProvider.getEventOccurrenceGenerator();
      return occurrenceGenerator.generateOccurrencesInPeriod(period, new ArrayList<>(events));
    } catch (Exception e) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getEventOccurrencesInPeriod()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL", e);
    }
  }

  @Override
  public List<EventOccurrence> getNextEventOccurrences(String... almanachIds) {
    List<EventOccurrence> occurrences;
    try {
      org.silverpeas.core.date.Date today = today();
      java.util.Calendar endDate = java.util.Calendar.getInstance();
      String upToDay = null;
      int numberOfMonths = settings.getInteger("almanach.nextEvents.windowtime", 0);
      if (numberOfMonths > 0) {
        endDate.add(java.util.Calendar.MONTH, numberOfMonths);
        upToDay = date2SQLDate(endDate.getTime());
      }
      Collection<EventDetail> events =
          getEventDAO().findAllEventsInRange(date2SQLDate(today), upToDay, almanachIds);

      EventOccurrenceGenerator occurrenceGenerator =
          EventOccurrenceGeneratorProvider.getEventOccurrenceGenerator();
      if (numberOfMonths > 0) {
        org.silverpeas.core.date.Date endDay = new org.silverpeas.core.date.Date(endDate.getTime());
        occurrences = occurrenceGenerator
            .generateOccurrencesInRange(today, endDay, new ArrayList<EventDetail>(events));
      } else {
        occurrences =
            occurrenceGenerator.generateOccurrencesFrom(today, new ArrayList<EventDetail>(events));
      }
    } catch (Exception ex) {
      throw new AlmanachRuntimeException("DefaultAlmanachService.getEventOccurrencesInWeek()",
          SilverpeasRuntimeException.ERROR, "almanach.EXE_GET_ALL_EVENTS_FAIL", ex);
    }
    return occurrences;
  }

  protected EventDAO getEventDAO() {
    return this.eventDAO;
  }

  protected org.silverpeas.core.date.Date today() {
    return org.silverpeas.core.date.Date.today();
  }

  private void checkEventDates(final EventDetail event) {
    if (event.getEndDate().before(event.getStartDate())) {
      throw new IllegalArgumentException("The event ends before its start!");
    }
    if (event.getStartDate().equals(event.getEndDate()) && isDefined(event.getEndHour()) &&
        isDefined(event.getStartHour())) {
      int endHour = extractHour(event.getEndHour());
      int endMinute = extractMinutes(event.getEndHour());
      int startHour = extractHour(event.getStartHour());
      int startMinute = extractMinutes(event.getStartHour());
      if (startHour > endHour || (startHour == endHour && startMinute > endMinute)) {
        throw new IllegalArgumentException("The event ends before its start!");
      }
    }
  }
}

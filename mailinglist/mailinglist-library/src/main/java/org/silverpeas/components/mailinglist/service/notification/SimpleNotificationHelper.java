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
package org.silverpeas.components.mailinglist.service.notification;

import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.notification.user.client.GroupRecipient;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.calendar.service.CalendarRuntimeException;
import org.silverpeas.core.calendar.service.SilverpeasCalendar;
import org.silverpeas.core.calendar.model.ToDoHeader;
import org.silverpeas.components.mailinglist.service.model.beans.Attachment;
import org.silverpeas.components.mailinglist.service.model.beans.ExternalUser;
import org.silverpeas.components.mailinglist.service.model.beans.InternalGroupSubscriber;
import org.silverpeas.components.mailinglist.service.model.beans.InternalUser;
import org.silverpeas.components.mailinglist.service.model.beans.InternalUserSubscriber;
import org.silverpeas.components.mailinglist.service.model.beans.MailingList;
import org.silverpeas.components.mailinglist.service.model.beans.Message;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.mail.MailSending;
import org.silverpeas.core.mail.ReceiverMailAddressSet;
import org.silverpeas.core.mail.engine.SmtpConfiguration;
import org.silverpeas.core.exception.SilverpeasException;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.silverpeas.core.mail.MailAddress.eMail;
import static org.silverpeas.core.mail.MailContent.of;

/**
 * Utility class to send notifications.
 * @author Emmanuel Hugonnet
 */
public class SimpleNotificationHelper implements NotificationHelper {

  public static final int BATCH_SIZE = 10;

  @Inject
  private NotificationSender notificationSender;
  @Inject
  private NotificationFormatter notificationFormatter;
  private SmtpConfiguration smtpConfig = SmtpConfiguration.fromDefaultSettings();
  private boolean externalThread = true;
  @Inject
  private SilverpeasCalendar calendarBm;
  @Inject
  private OrganizationController controller;

  public void notifyModerators(Message message, MailingList list)
      throws NotificationManagerException {
    Set<String> userIds = getModeratorsIds(list);
    notifyInternals(message, list, userIds, null, true);
  }

  public void notifyUsers(Message message, MailingList list) throws NotificationManagerException {
    Set<String> userIds = getUsersIds(list);
    Set<String> groupIds = getGroupIds(list);
    notifyInternals(message, list, userIds, groupIds, false);
  }

  public void notifyUsersAndModerators(Message message, MailingList list)
      throws NotificationManagerException {
    Set<String> userIds = getUsersIds(list);
    Set<String> moderatorIds = getModeratorsIds(list);
    userIds.addAll(moderatorIds);
    Collection<String> groupIds = getGroupIds(list);
    notifyInternals(message, list, userIds, groupIds, false);
  }

  public void notifyInternals(Message message, MailingList list, Collection<String> userIds,
      Collection<String> groupIds, boolean moderate) throws NotificationManagerException {
    String defaultTitle = notificationFormatter
        .formatTitle(message, list.getName(), DisplayI18NHelper.getDefaultLanguage(), moderate);
    NotificationMetaData metadata = new NotificationMetaData();
    metadata.setAnswerAllowed(false);
    metadata.setDate(message.getSentDate());
    metadata.setSource(list.getSubscribedAddress());
    metadata.setSender(list.getSubscribedAddress());
    metadata.setComponentId(message.getComponentId());
    String link = notificationFormatter.prepareUrl(message, moderate);
    for (String lang : DisplayI18NHelper.getLanguages()) {
      String title = notificationFormatter.formatTitle(message, list.getName(), lang, moderate);
      String content = notificationFormatter.formatMessage(message, lang, moderate);
      metadata.addLanguage(lang, title, content);
    }
    metadata.setTitle(defaultTitle);
    if (message.getSummary() != null) {
      metadata.setContent(message.getSummary());
    } else {
      metadata.setContent("");
    }
    metadata.setLink(link);
    for (String userId : userIds) {
      metadata.addUserRecipient(new UserRecipient(userId));
    }
    if (groupIds != null && !groupIds.isEmpty()) {
      for (String groupId : groupIds) {
        metadata.addGroupRecipient(new GroupRecipient(groupId));
      }
    }
    notificationSender.notifyUser(metadata);
    try {
      if (moderate) {
        createTask(message, defaultTitle, userIds);
      }
    } catch (CalendarRuntimeException | UnsupportedEncodingException e) {
      throw new NotificationManagerException("NotificationHelperImpl", SilverpeasException.ERROR,
          "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES", e);
    }
  }

  /**
   * @param message
   * @param list
   * @throws AddressException
   * @throws MessagingException
   */
  public void notifyExternals(Message message, MailingList list) throws MessagingException {
    MailSending mail = MailSending.from(eMail(list.getSubscribedAddress()));
    String subject = notificationFormatter
        .formatTitle(message, list.getName(), DisplayI18NHelper.getDefaultLanguage(), false);
    mail.withSubject(subject);

    if (!message.getAttachments().isEmpty()) {
      Multipart multiPart = new MimeMultipart();
      for (Attachment attachment : message.getAttachments()) {
        MimeBodyPart part = new MimeBodyPart();
        DataSource source = new FileDataSource(attachment.getPath());
        part.setDataHandler(new DataHandler(source));
        part.setFileName(attachment.getFileName());
        multiPart.addBodyPart(part);
      }
      MimeBodyPart body = new MimeBodyPart();
      body.setContent(message.getBody(), message.getContentType());
      multiPart.addBodyPart(body);
      mail.withContent(multiPart);
    } else {
      mail.withContent(of(message.getBody()).withContentType(message.getContentType()));
    }
    sendMail(mail, list.getExternalSubscribers());
  }

  protected void sendMail(MailSending mail, Collection<ExternalUser> externalUsers)
      throws MessagingException {
    ReceiverMailAddressSet receivers =
        ReceiverMailAddressSet.ofRecipientType(ReceiverMailAddressSet.MailRecipientType.BCC)
            .withReceiversBatchSizeOf(BATCH_SIZE);
    for (ExternalUser externalUser : externalUsers) {
      receivers.add(eMail(externalUser.getEmail()));
    }
    mail.to(receivers);

    if (isExternalThread()) {
      mail.send();
    } else {
      mail.sendSynchronously();
    }
  }

  public NotificationSender getNotificationSender() {
    return notificationSender;
  }

  public void setNotificationSender(NotificationSender notificationSender) {
    this.notificationSender = notificationSender;
  }

  public NotificationFormatter getNotificationFormatter() {
    return notificationFormatter;
  }

  public void setNotificationFormatter(NotificationFormatter notificationFormatter) {
    this.notificationFormatter = notificationFormatter;
  }

  public boolean isExternalThread() {
    return externalThread;
  }

  public void setExternalThread(boolean externalThread) {
    this.externalThread = externalThread;
  }

  public Set<String> getModeratorsIds(MailingList list) {
    int size = list.getModerators().size();
    Set<String> result = new HashSet<>(size);
    for (InternalUser user : list.getModerators()) {
      result.add(user.getId());
    }
    return result;
  }

  public Set<String> getUsersIds(MailingList list) {
    int size = list.getInternalSubscribers().size();
    Set<String> result = new HashSet<>(size);
    for (InternalUserSubscriber subscriber : list.getInternalSubscribers()) {
      result.add(subscriber.getExternalId());
    }
    return result;
  }

  public Set<String> getGroupIds(MailingList list) {
    int size = list.getGroupSubscribers().size();
    Set<String> result = new HashSet<>(size);
    for (final InternalGroupSubscriber internalGroupSubscriber : list.getGroupSubscribers()) {
      result.add((internalGroupSubscriber).getExternalId());
    }
    return result;
  }

  public OrganizationController getOrganisationController() {
    return controller;
  }

  public SilverpeasCalendar getCalendarBm() {
    return calendarBm;
  }

  public SmtpConfiguration getSmtpConfig() {
    return smtpConfig;
  }

  public void setSmtpConfig(SmtpConfiguration smtpConfig) {
    this.smtpConfig = smtpConfig;
  }

  protected void createTask(Message message, String title, Collection<String> userIds)
      throws CalendarRuntimeException, UnsupportedEncodingException {
    ToDoHeader todo = new ToDoHeader();
    todo.setDelegatorId(message.getComponentId());
    todo.setComponentId(message.getComponentId());
    todo.setName(title);
    todo.setStartDate(message.getSentDate());
    todo.setDescription(message.getId());
    String todoId = getCalendarBm().addToDo(todo);
    todo.setId(todoId);
    todo.setExternalId(
        "message/" + message.getId() + "?todoId=" + URLEncoder.encode(todoId, "UTF-8"));
    getCalendarBm().updateToDo(todo);
    getCalendarBm().setToDoAttendees(todoId, userIds.toArray(new String[userIds.size()]));
  }

  @Override
  public void notify(Message message, MailingList list)
      throws NotificationManagerException, MessagingException {
    if (list.isNotify() || list.isModerated()) {
      if (message.isModerated()) {
        if (list.isModerated() && !list.isNotify()) {
          notifyUsers(message, list);
        } else {
          notifyUsersAndModerators(message, list);
          notifyExternals(message, list);
        }
      } else {
        notifyModerators(message, list);
      }
    }
  }

}

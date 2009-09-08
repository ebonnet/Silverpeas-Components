package com.silverpeas.mailinglist.service.notification;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.silverpeas.mailinglist.service.model.beans.Attachment;
import com.silverpeas.mailinglist.service.model.beans.ExternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalGroupSubscriber;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.InternalUserSubscriber;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.silverpeas.mailinglist.service.model.beans.Message;
import com.silverpeas.mailinglist.service.util.MailSender;
import com.stratelia.silverpeas.notificationManager.NotificationManagerException;
import com.stratelia.silverpeas.notificationManager.NotificationMetaData;
import com.stratelia.silverpeas.notificationManager.NotificationSender;
import com.stratelia.silverpeas.silvertrace.SilverTrace;
import com.stratelia.webactiv.beans.admin.OrganizationController;
import com.stratelia.webactiv.calendar.control.CalendarBm;
import com.stratelia.webactiv.calendar.control.CalendarException;
import com.stratelia.webactiv.calendar.model.ToDoHeader;
import com.stratelia.webactiv.util.exception.SilverpeasException;

/**
 * Utility class to send notifications.
 * 
 * @author Emmanuel Hugonnet
 * @version $revision$
 */
public class NotificationHelperImpl implements NotificationHelper {

  private NotificationSender notificationSender;

  private NotificationFormatter notificationFormatter;

  private Session session;

  private SmtpConfiguration smtpConfig;

  private boolean externalThread = true;

  private CalendarBm calendarBm;

  private OrganizationController controller;

  public void notifyModerators(Message message, MailingList list)
      throws NotificationManagerException {
    Set<String> userIds = getModeratorsIds(list);
    notifyInternals(message, list, userIds, null, true);
  }

  public void notifyUsers(Message message, MailingList list)
      throws NotificationManagerException {
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

  public void notifyInternals(Message message, MailingList list,
      Collection<String> userIds, Collection<String> groupIds, boolean moderate)
      throws NotificationManagerException {
    String title = notificationFormatter.formatTitle(message, list.getName(),
        moderate);
    NotificationMetaData metadata = new NotificationMetaData();
    metadata.setAnswerAllowed(false);
    metadata.setDate(message.getSentDate());
    metadata.setSource(list.getSubscribedAddress());
    metadata.setSender(list.getSubscribedAddress());
    metadata.setComponentId(message.getComponentId());
    if (message.getSummary() != null) {
      metadata.setContent(message.getSummary());
    } else {
      metadata.setContent("");
    }
    metadata.setLink(notificationFormatter.prepareUrl("", message, moderate));
    metadata.setTitle(title);
    metadata.setUserRecipients(userIds);
    if (groupIds != null && !groupIds.isEmpty()) {
      metadata.setGroupRecipients(groupIds);
    }
    notificationSender.notifyUser(metadata);
    try {
      if (moderate) {
        createTask(message, title, userIds);
      }
    } catch (CalendarException e) {
      throw new NotificationManagerException("NotificationHelperImpl",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES",
          e);
    } catch (RemoteException e) {
      throw new NotificationManagerException("NotificationHelperImpl",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES",
          e);
    } catch (UnsupportedEncodingException e) {
      throw new NotificationManagerException("NotificationHelperImpl",
          SilverpeasException.ERROR, "calendar.MSG_CANT_CHANGE_TODO_ATTENDEES",
          e);
    }
  }

  /**
   * 
   * @param message
   * @param list
   * @throws AddressException
   * @throws MessagingException
   */
  public void notifyExternals(Message message, MailingList list)
      throws AddressException, MessagingException {
    SilverTrace.debug("mailingList", this.getClass().getName(),
        "mailinglist.notification.external.start");
    MimeMessage mail = new MimeMessage(session);
    mail.setFrom(new InternetAddress(list.getSubscribedAddress()));
    mail.setSubject(notificationFormatter.formatTitle(message, list.getName(),
        false));
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
      mail.setContent(multiPart);
    } else {
      mail.setContent(message.getBody(), message.getContentType());
    }
    SilverTrace.debug("mailingList", this.getClass().getName(),
        "mailinglist.notification.external.mail", mail.getSubject());
    sendMail(mail, list.getExternalSubscribers());
  }

  protected void sendMail(MimeMessage mail,
      Collection<ExternalUser> externalUsers) throws MessagingException {
    MailSender sender = new MailSender(session, mail, smtpConfig, externalUsers);
    if (isExternalThread()) {
      new Thread(sender).start();
    } else {
      sender.run();
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

  public void setNotificationFormatter(
      NotificationFormatter notificationFormatter) {
    this.notificationFormatter = notificationFormatter;
  }

  public Session getSession() {
    return session;
  }

  public void setSession(Session session) {
    this.session = session;
  }

  public boolean isExternalThread() {
    return externalThread;
  }

  public void setExternalThread(boolean externalThread) {
    this.externalThread = externalThread;
  }

  public Set<String> getModeratorsIds(MailingList list) {
    int size = list.getModerators().size();
    Set<String> result = new HashSet<String>(size);
    for (InternalUser user : list.getModerators()) {
      result.add(user.getId());
    }
    return result;
  }

  public Set<String> getUsersIds(MailingList list) {
    int size = list.getInternalSubscribers().size();
    Set<String> result = new HashSet<String>(size);
    for (InternalUserSubscriber subscriber : list.getInternalSubscribers()) {
      result.add(subscriber.getExternalId());
    }
    return result;
  }

  public Set<String> getGroupIds(MailingList list) {
    int size = list.getGroupSubscribers().size();
    Set<String> result = new HashSet<String>(size);
    Iterator<InternalGroupSubscriber> iter = list.getGroupSubscribers()
        .iterator();
    while (iter.hasNext()) {
      result.add(((InternalGroupSubscriber) iter.next()).getExternalId());
    }
    return result;
  }

  public OrganizationController getController() {
    return controller;
  }

  public void setController(OrganizationController controller) {
    this.controller = controller;
  }

  public CalendarBm getCalendarBm() {
    return calendarBm;
  }

  public void setCalendarBm(CalendarBm calendarBm) {
    this.calendarBm = calendarBm;
  }

  public SmtpConfiguration getSmtpConfig() {
    return smtpConfig;
  }

  public void setSmtpConfig(SmtpConfiguration smtpConfig) {
    this.smtpConfig = smtpConfig;
  }

  protected void createTask(Message message, String title,
      Collection<String> userIds) throws RemoteException, CalendarException,
      UnsupportedEncodingException {
    ToDoHeader todo = new ToDoHeader();
    todo.setDelegatorId(message.getComponentId());
    todo.setComponentId(message.getComponentId());
    todo.setName(title);
    todo.setStartDate(message.getSentDate());
    todo.setDescription(message.getId());
    String todoId = getCalendarBm().addToDo(todo);
    todo.setId(todoId);
    todo.setExternalId("message/" + message.getId() + "?todoId="
        + URLEncoder.encode(todoId, "UTF-8"));
    getCalendarBm().updateToDo(todo);
    getCalendarBm().setToDoAttendees(todoId,
        (String[]) userIds.toArray(new String[userIds.size()]));
  }

  @Override
  public void notify(Message message, MailingList list)
      throws NotificationManagerException, AddressException, MessagingException {
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
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
package org.silverpeas.components.blog.notification;

import org.silverpeas.components.blog.model.Category;
import org.silverpeas.components.blog.model.PostDetail;
import org.silverpeas.core.comment.model.Comment;
import org.silverpeas.core.notification.user.builder.AbstractTemplateUserNotificationBuilder;
import org.silverpeas.core.notification.user.UserSubscriptionNotificationBehavior;
import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.DateUtil;

import java.util.Collection;
import java.util.MissingResourceException;

/**
 * The centralization of the construction of the blog notifications
 * @author Yohann Chastagnier
 */
public class BlogUserNotification extends AbstractTemplateUserNotificationBuilder<PostDetail>
    implements UserSubscriptionNotificationBehavior {

  private final UserDetail userDetail;
  private final String componentInstanceId;
  private final Comment comment;
  private final String fileName;
  private final NotifAction action;
  private final String senderId;
  private final Collection<String> newSubscribers;

  public BlogUserNotification(final String componentInstanceId, final PostDetail postDetail,
      final UserDetail userDetail) {
    this(componentInstanceId, postDetail, null, null, userDetail.getId(), null, userDetail);
  }

  public BlogUserNotification(final String componentInstanceId, final PostDetail postDetail,
      final Comment comment, final String type, final String senderId,
      final Collection<String> newSubscribers) {
    this(componentInstanceId, postDetail, comment, type, senderId, newSubscribers, null);
  }

  private BlogUserNotification(final String componentInstanceId, final PostDetail postDetail,
      final Comment comment, final String type, final String senderId,
      final Collection<String> newSubscribers, final UserDetail userDetail) {
    super(postDetail, null, null);
    this.componentInstanceId = componentInstanceId;
    this.comment = comment;
    if ("create".equals(type)) {
      fileName = "blogNotificationSubscriptionCreate";
      action = NotifAction.CREATE;
    } else if ("update".equals(type)) {
      fileName = "blogNotificationSubscriptionUpdate";
      action = NotifAction.UPDATE;
    } else {
      fileName = "blogNotification";
      action = NotifAction.REPORT;
    }
    this.senderId = senderId;
    this.newSubscribers = newSubscribers;
    this.userDetail = userDetail;
  }

  @Override
  protected String getBundleSubjectKey() {
    if (action.equals(NotifAction.REPORT)) {
      return "blog.notifSubject";
    }
    return "blog.subjectSubscription";
  }

  @Override
  protected String getFileName() {
    return fileName;
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    return newSubscribers;
  }

  @Override
  protected boolean stopWhenNoUserToNotify() {
    return !NotifAction.REPORT.equals(action);
  }

  @Override
  protected void perform(final PostDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected void performTemplateData(final String language, final PostDetail resource,
      final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("blog", resource);
    template.setAttribute("blogName", resource.getPublication().getName(language));
    template.setAttribute("blogDate", DateUtil.getOutputDate(resource.getDateEvent(), language));
    template.setAttribute("comment", comment);
    String commentMessage = null;
    if (comment != null) {
      commentMessage = comment.getMessage();
    }
    template.setAttribute("commentMessage", commentMessage);
    final Category categorie = resource.getCategory();
    String categorieName = null;
    if (categorie != null) {
      categorieName = categorie.getName(language);
    }
    template.setAttribute("blogCategorie", categorieName);
    template.setAttribute("senderName", (userDetail != null ? userDetail.getDisplayedName() : ""));
  }

  @Override
  protected void performNotificationResource(final String language, final PostDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceName(resource.getPublication().getName(language));
  }

  @Override
  protected String getTemplatePath() {
    return "blog";
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return componentInstanceId;
  }

  @Override
  protected String getSender() {
    return senderId;
  }

  @Override
  protected String getMultilangPropertyFile() {
    return "org.silverpeas.blog.multilang.blogBundle";
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "blog.notifPostLinkLabel";
  }
}

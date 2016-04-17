/*
 * Copyright (C) 2000 - 2014 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception. You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.silverpeas.components.questionreply.service.notification;

import org.silverpeas.components.questionreply.QuestionReplyException;
import org.silverpeas.components.questionreply.model.Question;
import org.silverpeas.components.questionreply.model.Reply;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.notification.user.client.NotificationManagerException;
import org.silverpeas.core.notification.user.client.NotificationMetaData;
import org.silverpeas.core.notification.user.client.NotificationParameters;
import org.silverpeas.core.notification.user.client.NotificationSender;
import org.silverpeas.core.notification.user.client.UserRecipient;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.Link;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.exception.SilverpeasException;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;

/**
 * @author ehugonnet
 */
public class SubscriptionNotifier extends Notifier {

  private final Reply reply;
  private final Question question;
  final NotificationSender notificationSender;
  private static final String BUNDLE_NAME =
      "org.silverpeas.questionReply.multilang.questionReplyBundle";

  public SubscriptionNotifier(UserDetail sender, Question question, Reply reply) {
    super(sender);
    this.reply = reply;
    this.question = question;
    this.notificationSender = new NotificationSender(question.getInstanceId());
  }

  @Override
  public void sendNotification(Collection<UserRecipient> recipients) throws QuestionReplyException {
    if (recipients != null && !recipients.isEmpty()) {
      try {
        // Get default resource bundle
        LocalizationBundle message = ResourceLocator.getLocalizationBundle(BUNDLE_NAME,
            DisplayI18NHelper.getDefaultLanguage());
        Map<String, SilverpeasTemplate> templates = new HashMap<>();
        String translation;
        try {
          translation = message.getString("questionReply.subscription.title");
        } catch (MissingResourceException ex) {
          translation = "Réponse à : %1$s";
        }
        NotificationMetaData notifMetaData = new NotificationMetaData(NotificationParameters.NORMAL,
            String.format(translation, question.getTitle()), templates, "reply_subscription");
        List<String> languages = DisplayI18NHelper.getLanguages();
        for (String language : languages) {
          message = ResourceLocator.getLocalizationBundle(BUNDLE_NAME, language);
          SilverpeasTemplate template = loadTemplate();
          template.setAttribute("userName", getSendername());
          template.setAttribute("QuestionDetail", question);
          template.setAttribute("questionTitle", question.getTitle());
          template.setAttribute("replyTitle", reply.getTitle());
          template.setAttribute("replyContent", reply.loadWysiwygContent());
          template.setAttribute("silverpeasURL", question._getPermalink());
          templates.put(language, template);
          notifMetaData.addLanguage(language, String
              .format(translation, question.getTitle()), "");

          Link link = new Link(question._getPermalink(), message.getString("questionReply.notifLinkLabel"));
          notifMetaData.setLink(link, language);
        }
        notifMetaData.addUserRecipients(recipients);
        notifMetaData.setSender(sender.getId());

        notificationSender.notifyUser(notifMetaData);
      } catch (NotificationManagerException e) {
        throw new QuestionReplyException("QuestionReplySessionController.notify()",
            SilverpeasException.ERROR, "questionReply.EX_NOTIFICATION_MANAGER_FAILED", "", e);
      }
    }
  }
}

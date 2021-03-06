/*
 * Copyright (C) 2000 - 2013 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * Open Source Software ("FLOSS") applications as described in Silverpeas's
 * FLOSS exception.  You should have recieved a copy of the text describing
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
package org.silverpeas.components.kmelia.notification;

import org.silverpeas.core.notification.user.model.NotificationResourceData;
import org.silverpeas.core.notification.user.client.constant.NotifAction;
import org.silverpeas.core.admin.ObjectType;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.components.kmelia.service.KmeliaHelper;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.template.SilverpeasTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.MissingResourceException;

/**
 * @author Yohann Chastagnier
 */
public class KmeliaTopicUserNotification extends AbstractKmeliaUserNotification<NodeDetail> {

  private final NodePK nodePK;
  private final NodeDetail fatherDetail;
  private final String alertType;
  private final NotifAction action;

  public KmeliaTopicUserNotification(final NodePK nodePK, final NodePK fatherPK, final String alertType) {
    super(null, null, "notificationCreateTopic");
    this.nodePK = nodePK;
    this.alertType = alertType;
    try {
      setResource(getNodeBm().getHeader(nodePK));
      if (fatherPK != null) {
        action = NotifAction.CREATE;
        fatherDetail = getNodeBm().getHeader(fatherPK);
      } else {
        action = NotifAction.UPDATE;
        fatherDetail = null;
      }
    } catch (final Exception e) {
      throw new KmeliaRuntimeException("DefaultKmeliaService.topicCreationAlert()",
          SilverpeasRuntimeException.ERROR,
          "kmelia.EX_IMPOSSIBLE_DALERTER_POUR_MANIPULATION_THEME", e);
    }
  }

  @Override
  protected String getBundleSubjectKey() {
    return "kmelia.NewTopic";
  }

  @Override
  protected Collection<String> getUserIdsToNotify() {
    boolean haveRights = getResource().haveRights();
    int rightsDependOn = getResource().getRightsDependsOn();
    if (fatherDetail != null) {
      // Case of creation only
      haveRights = fatherDetail.haveRights();
      rightsDependOn = fatherDetail.getRightsDependsOn();
    }

    final String[] users;
    if (!haveRights) {
      if ("All".equals(alertType)) {
        final UserDetail[] userDetails = getOrganisationController().getAllUsers(getComponentInstanceId());
        if (userDetails != null) {
          users = new String[userDetails.length];
          int i = 0;
          for (final UserDetail userDetail : userDetails) {
            users[i++] = userDetail.getId();
          }
        } else {
          users = null;
        }
      } else if ("Publisher".equals(alertType)) {
        // Get the list of all publishers and admin
        final List<String> profileNames = new ArrayList<String>();
        profileNames.add("admin");
        profileNames.add("publisher");
        profileNames.add("writer");
        users = getOrganisationController().getUsersIdsByRoleNames(getComponentInstanceId(), profileNames);
      } else {
        users = null;
      }
    } else {
      final List<String> profileNames = new ArrayList<String>();
      profileNames.add("admin");
      profileNames.add("publisher");
      profileNames.add("writer");

      if (alertType.equals("All")) {
        profileNames.add("user");
        users =
            getOrganisationController().getUsersIdsByRoleNames(getComponentInstanceId(),
                String.valueOf(rightsDependOn), ObjectType.NODE, profileNames);
      } else if (alertType.equals("Publisher")) {
        users =
            getOrganisationController().getUsersIdsByRoleNames(getComponentInstanceId(),
                String.valueOf(rightsDependOn), ObjectType.NODE, profileNames);
      } else {
        users = null;
      }
    }

    if (users == null) {
      return null;
    }
    return Arrays.asList(users);
  }

  @Override
  protected void perform(final NodeDetail resource) {
    super.perform(resource);
    getNotificationMetaData().displayReceiversInFooter();
  }

  @Override
  protected void performTemplateData(final String language, final NodeDetail resource, final SilverpeasTemplate template) {
    String title;
    try {
      title = getBundle(language).getString(getBundleSubjectKey());
    } catch (MissingResourceException ex) {
      title = getTitle();
    }
    getNotificationMetaData().addLanguage(language, title, "");
    template.setAttribute("path", getHTMLNodePath(resource.getFatherPK(), language));
    template.setAttribute("topic", resource);
    template.setAttribute("topicName", resource.getName(language));
    template.setAttribute("topicDescription", resource.getDescription(language));
    template.setAttribute("senderName", "");
  }

  @Override
  protected void performNotificationResource(final String language, final NodeDetail resource,
      final NotificationResourceData notificationResourceData) {
    notificationResourceData.setResourceId(resource.getId());
    notificationResourceData.setResourceType(resource.getType());
    notificationResourceData.setResourceName(resource.getName(language));
    notificationResourceData.setResourceDescription(resource.getDescription(language));
  }

  @Override
  protected String getResourceURL(final NodeDetail resource) {
    return KmeliaHelper.getNodeUrl(resource);
  }

  @Override
  protected NotifAction getAction() {
    return action;
  }

  @Override
  protected String getComponentInstanceId() {
    return nodePK.getInstanceId();
  }

  @Override
  protected String getSender() {
    return getResource().getCreatorId();
  }

  @Override
  protected String getContributionAccessLinkLabelBundleKey() {
    return "kmelia.notifTopicLinkLabel";
  }
}

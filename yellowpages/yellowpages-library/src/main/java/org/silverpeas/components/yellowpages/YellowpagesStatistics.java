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
package org.silverpeas.components.yellowpages;

import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.core.contact.model.ContactDetail;
import org.silverpeas.core.node.service.NodeService;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.components.yellowpages.model.TopicDetail;
import org.silverpeas.components.yellowpages.model.UserContact;
import org.silverpeas.components.yellowpages.service.YellowpagesService;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.admin.service.OrganizationControllerProvider;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Class declaration
 * @author
 */
@Singleton
@Named("yellowpages" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class YellowpagesStatistics implements ComponentStatisticsProvider {

  private NodeService nodeService = NodeService.get();

  /**
   * Method declaration
   * @param spaceId
   * @param componentId
   * @throws Exception
   */
  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<NodeDetail> nodes = getNodeService().getAllNodes(new NodePK("useless", componentId));
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(nodes.size());
    if (nodes != null && !nodes.isEmpty()) {
      Collection<UserContact> c = getContacts("0", spaceId, componentId);
      for (UserContact contact : c) {
        ContactDetail detail = contact.getContact();
        UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
        myCouple.setUserId(detail.getCreatorId());
        myCouple.setCountVolume(1);
        myArrayList.add(myCouple);
      }
    }

    return myArrayList;
  }

  /**
   * Method declaration
   * @return instance of {@link YellowpagesService}.
   */
  private YellowpagesService getYellowPagesService() {
    return YellowpagesService.get();
  }

  /**
   * @param topicId the topic identifier
   * @param spaceId the space identifier
   * @param componentId the component instance identifier
   * @return a collection of user contact
   * @throws Exception
   */
  private Collection<UserContact> getContacts(String topicId, String spaceId, String componentId)
      throws Exception {
    Collection<UserContact> c = new ArrayList<>();
    if (topicId == null) {
      return c;
    }

    OrganizationController organisationController =
        OrganizationControllerProvider.getOrganisationController();

    if (topicId.startsWith("group_")) {
      int nbUsers =
          organisationController.getAllSubUsersNumber(topicId.substring("group_".length()));
      for (int n = 0; n < nbUsers; n++) {
        ContactDetail detail =
            new ContactDetail("useless", "useless", "useless", "useless", "useless", "useless",
                "useless", new Date(), "0");
        UserContact contact = new UserContact();
        contact.setContact(detail);
        c.add(contact);
      }
    } else {
      TopicDetail topic;
      try {
        topic = getYellowPagesService().goTo(new NodePK(topicId, componentId), "0");
        if (topic != null) {
          c.addAll(topic.getContactDetails());
        }
      } catch (Exception ex) {
        topic = null;

      }
      // treatment of the nodes of current topic
      if (topic != null) {
        Collection<NodeDetail> subTopics = topic.getNodeDetail().getChildrenDetails();
        for (NodeDetail node : subTopics) {
          if (!(node.getNodePK().isRoot() || node.getNodePK().isTrash() ||
              node.getNodePK().isUnclassed())) {
            c.addAll(getContacts(node.getNodePK().getId(), spaceId, componentId));
          }
        }
      }
    }
    return c;
  }

  private NodeService getNodeService() {
    return nodeService;
  }
}

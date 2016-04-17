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
package org.silverpeas.components.blog.service;

import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.silverpeas.core.contribution.model.ContributionIdentifier;
import org.silverpeas.core.notification.system.JMSResourceEventListener;
import org.silverpeas.core.contribution.content.wysiwyg.WysiwygContent;
import org.silverpeas.core.contribution.content.wysiwyg.notification.WysiwygEvent;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;

/**
 * A listener of events about the change in WYSIWYG contents that are related to a blog.
 * @author mmoquillon
 */
@MessageDriven(name = "BlogWysiwygEventListener", activationConfig = {
    @ActivationConfigProperty(propertyName = "destinationLookup",
        propertyValue = "topic/wysiwyg"),
    @ActivationConfigProperty(propertyName = "destinationType",
        propertyValue = "javax.jms.Topic"),
    @ActivationConfigProperty(propertyName = "acknowledgeMode",
        propertyValue = "Auto-acknowledge")})
public class BlogWysiwygEventListener extends JMSResourceEventListener<WysiwygEvent> {

  @Inject
  private BlogService blogService;

  @Override
  protected Class<WysiwygEvent> getResourceEventClass() {
    return WysiwygEvent.class;
  }

  @Override
  public void onUpdate(final WysiwygEvent event) throws Exception {
    updatePublication(event.getTransition().getAfter());
  }

  @Override
  public void onCreation(final WysiwygEvent event) throws Exception {
    updatePublication(event.getTransition().getAfter());
  }

  private void updatePublication(final WysiwygContent content) {
    if (isAboutBlogPost(content)) {
      ContributionIdentifier id = content.getContributionId();
      blogService.externalElementsOfPublicationHaveChanged(new PublicationPK(id.getLocalId(),
          id.getComponentInstanceId()), content.getAuthorId());
    }
  }

  private boolean isAboutBlogPost(WysiwygContent content) {
    return !content.getContributionId().getLocalId().startsWith("Node") &&
        content.getContributionId().getComponentInstanceId().startsWith("blog");
  }
}

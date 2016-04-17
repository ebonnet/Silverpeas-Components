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
package org.silverpeas.components.almanach;


import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.core.web.index.components.ComponentIndexation;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.contribution.attachment.AttachmentService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Collection;

@Singleton
@Named("almanach" + ComponentIndexation.QUALIFIER_SUFFIX)
public class AlmanachIndexer implements ComponentIndexation {

  @Inject
  private AlmanachService almanach;
  @Inject
  private AttachmentService attachmentService;

  @Override
  public void index(ComponentInst inst) throws Exception {

    EventPK pk = new EventPK("", inst.getSpaceId(), inst.getId());
    Collection<EventDetail> allEvents = almanach.getAllEvents(pk);
    for (EventDetail event : allEvents) {
      almanach.createIndex(event);
      attachmentService.indexAllDocuments(event.getPK(), null, null);
    }
  }

}
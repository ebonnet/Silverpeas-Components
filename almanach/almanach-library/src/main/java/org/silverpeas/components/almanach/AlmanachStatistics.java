/*
 * Copyright (C) 2000 - 2016 Silverpeas
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
package org.silverpeas.components.almanach;

import org.silverpeas.core.silverstatistics.volume.service.ComponentStatisticsProvider;
import org.silverpeas.core.silverstatistics.volume.model.UserIdCountVolumeCouple;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.components.almanach.service.AlmanachRuntimeException;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class declaration
 * @author
 */
@Singleton
@Named("almanach" + ComponentStatisticsProvider.QUALIFIER_SUFFIX)
public class AlmanachStatistics implements ComponentStatisticsProvider {

  @Inject
  private AlmanachService almanachService = null;

  @Override
  public Collection<UserIdCountVolumeCouple> getVolume(String spaceId, String componentId)
      throws Exception {
    Collection<EventDetail> events = getEvents(spaceId, componentId);
    List<UserIdCountVolumeCouple> myArrayList = new ArrayList<>(events.size());
    for (EventDetail detail : events) {
      UserIdCountVolumeCouple myCouple = new UserIdCountVolumeCouple();
      myCouple.setUserId(detail.getDelegatorId());
      myCouple.setCountVolume(1);
      myArrayList.add(myCouple);
    }
    return myArrayList;
  }

  private AlmanachService getAlmanachService() throws Exception {
    if (almanachService == null) {
      throw new AlmanachRuntimeException("almanach", SilverpeasRuntimeException.ERROR,
          "AlmanachStatistics.getAlmanachService", "CDI bootstrap error");
    }
    return almanachService;
  }

  public Collection<EventDetail> getEvents(String spaceId, String componentId) throws Exception {
    return getAlmanachService().getAllEvents(new EventPK("", spaceId, componentId));
  }

}

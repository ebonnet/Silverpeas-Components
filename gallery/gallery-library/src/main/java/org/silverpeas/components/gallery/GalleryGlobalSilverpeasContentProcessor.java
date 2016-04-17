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
package org.silverpeas.components.gallery;

import org.silverpeas.core.contribution.contentcontainer.content.DefaultGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import org.silverpeas.core.contribution.contentcontainer.content.IGlobalSilverContentProcessor;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentInterface;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.components.gallery.constant.MediaResolution;
import org.silverpeas.components.gallery.model.Media;

import javax.inject.Named;

import static org.silverpeas.core.contribution.contentcontainer.content.IGlobalSilverContentProcessor
    .PROCESSOR_NAME_SUFFIX;

@Named("gallery" + PROCESSOR_NAME_SUFFIX)
public class GalleryGlobalSilverpeasContentProcessor extends DefaultGlobalSilverContentProcessor
    implements IGlobalSilverContentProcessor {

  @Override
  public GlobalSilverContent getGlobalSilverContent(SilverContentInterface sci,
      UserDetail creatorDetail, String location) {
    GlobalSilverContent gsc = super.getGlobalSilverContent(sci, creatorDetail, location);
    Media media = (Media) sci;
    gsc.setThumbnailURL(media.getApplicationThumbnailUrl(MediaResolution.TINY));
    return gsc;
  }

}
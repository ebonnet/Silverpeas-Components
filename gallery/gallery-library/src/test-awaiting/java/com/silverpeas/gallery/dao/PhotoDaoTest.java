/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have recieved a copy of the
 * text describing the FLOSS exception, and it is also available here:
 * "http://www.silverpeas.org/docs/core/legal/floss_exception.html"
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package com.silverpeas.gallery.dao;

import com.silverpeas.gallery.BaseGalleryTest;
import com.silverpeas.gallery.constant.MediaMimeType;
import com.silverpeas.gallery.model.Media;
import com.silverpeas.gallery.model.MediaPK;
import com.silverpeas.gallery.model.MediaWithStatus;
import com.silverpeas.gallery.model.Photo;
import com.silverpeas.gallery.socialNetwork.SocialInformationGallery;
import org.silverpeas.core.socialnetwork.model.SocialInformation;
import com.stratelia.webactiv.beans.admin.UserDetail;
import org.silverpeas.core.cache.service.CacheServiceProvider;
import org.silverpeas.core.util.DateUtil;
import org.junit.Test;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.media.Definition;

import java.sql.Connection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class PhotoDaoTest extends BaseGalleryTest {

  @Override
  public String getDataSetPath() {
    return "com/silverpeas/gallery/dao/photo_dataset.xml";
  }

  @Override
  protected void verifyDataBeforeTest() throws Exception {
    // Skip
  }

  @Test
  public void testGetAllPhotosIdByUserid() throws Exception {
    Connection connexion = getConnection();
    String userid = "1";
    try {
      Photo ciel = PhotoDAO.getPhoto(connexion, "0");
      Photo fleur = PhotoDAO.getPhoto(connexion, "3");
      Photo mer = PhotoDAO.getPhoto(connexion, "4");
      SocialInformation socialCiel =
          new SocialInformationGallery(new MediaWithStatus(ciel.getPhoto(), true));
      SocialInformation socialFleur =
          new SocialInformationGallery(new MediaWithStatus(fleur.getPhoto(), false));
      SocialInformation socialmer1 =
          new SocialInformationGallery(new MediaWithStatus(mer.getPhoto(), true));
      SocialInformation socialmer2 =
          new SocialInformationGallery(new MediaWithStatus(mer.getPhoto(), false));
      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");
      List<SocialInformation> photos =
          MediaDAO.getAllMediaIdByUserId(userid, Period.from(begin, end));
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(4));
      assertThat(photos.get(0), equalTo(socialmer1));
      assertThat(photos.get(0).isUpdated(), equalTo(true));
      assertThat(photos.get(1), equalTo(socialmer2));
      assertThat(photos.get(1).isUpdated(), equalTo(false));
      assertThat(photos.get(2), equalTo(socialFleur));
      assertThat(photos.get(3), equalTo(socialCiel));
    } finally {
      connexion.close();
    }
  }

  @Test
  public void testGetPhotoDetail() throws Exception {
    Connection connexion = getConnection();
    Date createFlowerDate = Timestamp.valueOf("2010-06-15 00:00:00.0");
    Date updateFlowerDate = Timestamp.valueOf("2010-06-16 00:00:00.0");
    String fleurId = "3";
    MediaPK mediaPK = new MediaPK(fleurId, GALLERY1);
    Photo fleur = prepareExpectedPhoto(createFlowerDate, updateFlowerDate, mediaPK);
    try {
      Photo photo = PhotoDAO.getPhoto(connexion, fleurId);
      assertThat(photo, notNullValue());
      assertThat(photo.getTitle(), equalTo(fleur.getTitle()));
      assertThat(photo.getId(), equalTo(mediaPK.getId()));
      assertThat(photo.getDescription(), equalTo(fleur.getDescription()));
      assertThat(photo.getCreationDate(), equalTo(createFlowerDate));
      assertThat(photo.getLastUpdateDate(), equalTo(updateFlowerDate));
      assertThat(photo, equalTo(fleur));

    } finally {
      connexion.close();
    }
  }

  private Photo prepareExpectedPhoto(Date createFlowerDate, Date updateFlowerDate, MediaPK mediaPK) {
    Photo fleur = new Photo();
    fleur.setTitle("fleur");
    fleur.setDescription("tulipe");
    fleur.setCreationDate(createFlowerDate);
    fleur.setLastUpdateDate(updateFlowerDate);
    fleur.setAuthor(null);
    fleur.setDownloadAuthorized(false);
    fleur.setMediaPK(mediaPK);
    fleur.setCreatorId("1");
    fleur.setLastUpdatedBy("0");
    fleur.setDefinition(Definition.of(110, 110));
    fleur.setFileName("fleur.jpg");
    fleur.setFileSize(5146);
    fleur.setFileMimeType(MediaMimeType.PNG);
    return fleur;
  }
  @Test
  public void testgetSocialInformationsList() throws Exception {
    Connection connexion = getConnection();
    List<String> availableList = new ArrayList<String>();
    availableList.add(GALLERY0);
    availableList.add(GALLERY1);
    List<String> listOfuserId = new ArrayList<String>();
    listOfuserId.add("1");
    try {
      Photo ciel = PhotoDAO.getPhoto(connexion, "0");
      Photo fleur = PhotoDAO.getPhoto(connexion, "3");
      SocialInformation socialCiel =
          new SocialInformationGallery(new MediaWithStatus(ciel, true));
      SocialInformation socialFleur =
          new SocialInformationGallery(new MediaWithStatus(fleur, false));

      Date begin = DateUtil.parse("2010/05/01");
      Date end = DateUtil.parse("2010/08/31");

      List<SocialInformation> photos = MediaDAO.getSocialInformationListOfMyContacts(listOfuserId, null, Period.from(begin, end));
      assertThat(photos, notNullValue());
      photos = MediaDAO.getSocialInformationListOfMyContacts(listOfuserId, availableList, Period.from(begin, end));
      assertThat(photos, notNullValue());
      assertThat(photos, hasSize(4));
      assertThat(photos.get(3), equalTo(socialCiel));
      assertThat(photos.get(2), equalTo(socialFleur));
    } finally {
      connexion.close();
    }
  }

  @Test
  public void testGetPhotoPathList() throws Exception {
    Connection con = getConnection();
    try {
      String mediaIdToPerform = "5";
      Media media = new Photo();
      media.setId(mediaIdToPerform);
      media.setComponentInstanceId(GALLERY1);

      Collection<String> pathList = MediaDAO.getAlbumIdsOf(media);
      assertThat(pathList, notNullValue());
      assertThat(pathList, hasSize(1));
      assertThat(pathList, contains("2"));
    } finally {
      con.close();
    }
  }
}

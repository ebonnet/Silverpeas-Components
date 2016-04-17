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
package org.silverpeas.components.gallery.service;

import org.silverpeas.core.socialnetwork.model.SocialInformation;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.node.model.NodePK;
import org.silverpeas.components.gallery.delegate.GalleryPasteDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataCreateDelegate;
import org.silverpeas.components.gallery.delegate.MediaDataUpdateDelegate;
import org.silverpeas.components.gallery.model.AlbumDetail;
import org.silverpeas.components.gallery.model.Media;
import org.silverpeas.components.gallery.model.MediaCriteria;
import org.silverpeas.components.gallery.model.MediaPK;
import org.silverpeas.components.gallery.model.Order;
import org.silverpeas.components.gallery.model.OrderRow;
import org.silverpeas.components.gallery.model.Photo;
import org.silverpeas.components.gallery.process.GalleryProcessExecutionContext;
import org.silverpeas.core.index.search.model.QueryDescription;
import org.silverpeas.core.date.period.Period;
import org.silverpeas.core.process.util.ProcessList;

import java.io.File;
import java.util.Collection;
import java.util.List;

public interface GalleryService {

  public AlbumDetail getAlbum(NodePK nodePK);

  public AlbumDetail getAlbum(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  public NodePK createAlbum(AlbumDetail album, NodePK nodePK);

  public void updateAlbum(AlbumDetail album);

  public void deleteAlbum(UserDetail user, String componentInstanceId, NodePK nodePK);

  public Collection<AlbumDetail> getAllAlbums(String instanceId);

  public void removeMediaFromAllAlbums(Media media);

  public void addMediaToAlbums(Media media, String... albums);

  public Photo getPhoto(MediaPK mediaPK);

  public Media getMedia(MediaPK mediaPK);

  public Media getMedia(MediaPK mediaPK, MediaCriteria.VISIBILITY visibility);

  public Collection<Photo> getAllPhotos(NodePK nodePK);

  public Collection<Photo> getAllPhotos(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  public Collection<Media> getAllMedia(NodePK nodePK);

  public Collection<Media> getAllMedia(NodePK nodePK, MediaCriteria.VISIBILITY visibility);

  public Collection<Media> getAllMedia(String instanceId);

  public Collection<Media> getAllMedia(String instanceId, MediaCriteria.VISIBILITY visibility);

  public void paste(UserDetail user, String componentInstanceId, GalleryPasteDelegate delegate);

  public void importFromRepository(UserDetail user, String componentInstanceId, File repository,
      boolean watermark, String watermarkHD, String watermarkOther, MediaDataCreateDelegate delegate);

  public Media createMedia(UserDetail user, String componentInstanceId, boolean watermark,
      String watermarkHD, String watermarkOther, MediaDataCreateDelegate delegate);

  public void updateMedia(UserDetail user, String componentInstanceId, Collection<String> mediaIds,
      String albumId, MediaDataUpdateDelegate delegate);

  public void updateMedia(UserDetail user, String componentInstanceId, Media media,
      boolean watermark, String watermarkHD, String watermarkOther,
      MediaDataUpdateDelegate delegate);

  public void deleteMedia(UserDetail user, String componentInstanceId, Collection<String> mediaIds);

  public List<Media> getLastRegisteredMedia(String instanceId);

  public Collection<Media> getAllMediaThatWillBeNotVisible(int nbDays);

  public Collection<Media> getNotVisible(String instanceId);

  public Collection<NodeDetail> getPath(NodePK nodePK);

  public Collection<String> getAlbumIdsOf(Media media);

  public String getHTMLNodePath(NodePK nodePK);

  public void indexGallery(final UserDetail user, String instanceId);

  public int getSilverObjectId(MediaPK mediaPK);

  public Collection<Media> search(QueryDescription query);

  public String createOrder(Collection<String> basket, String userId, String instanceId);

  public Order getOrder(String orderId, String instanceId);

  public List<Order> getAllOrders(String userId, String instanceId);

  public void updateOrderRow(OrderRow row);

  public void updateOrder(Order order);

  public List<Order> getAllOrderToDelete(int nbDays);

  public void deleteOrders(List<Order> orders);

  /**
   * get my list of SocialInformationGallery according to options and number of Item and the first
   * Index
   *
   * @return: List <SocialInformation>
   * @param userId
   * @param period
   * @return
   */
  public List<SocialInformation> getAllMediaByUserId(String userId, Period period);

  /**
   * get list of SocialInformationGallery of my contacts according to options and number of Item and
   * the first Index
   *
   * @param listOfUserId
   * @param availableComponent
   * @param period
   * @return List<SocialInformation>
   */
  public List<SocialInformation> getSocialInformationListOfMyContacts(List<String> listOfUserId,
      List<String> availableComponent, Period period);

  public void sortAlbums(List<NodePK> albumIds);

  /**
   * Executes a process list
   *
   * @param processList
   * @param processExecutionContext
   * @throws Exception
   */
  public void executeProcessList(ProcessList<GalleryProcessExecutionContext> processList,
      GalleryProcessExecutionContext processExecutionContext);
}

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
 * FLOSS exception.  You should have received a copy of the text describing
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

package com.stratelia.silverpeas.chat;

import jChatBox.Chat.Chatroom;
import jChatBox.Chat.ChatroomManager;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.silverpeas.core.contribution.contentcontainer.content.ContentInterface;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManager;
import org.silverpeas.core.contribution.contentcontainer.content.ContentManagerException;
import org.silverpeas.core.contribution.contentcontainer.content.SilverContentVisibility;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.util.exception.SilverpeasException;

/**
 * The chat implementation of ContentInterface.
 */
public class ChatContentManager implements ContentInterface {
  /**
   * Find all the SilverContent with the given list of SilverContentId
   * @param ids list of silverContentId to retrieve
   * @param peasId the id of the instance
   * @param userId the id of the user who wants to retrieve silverContent
   * @param userRoles the roles of the user
   * @return a List of SilverContent
   */
  public List getSilverContentById(List ids, String peasId, String userId,
      List userRoles) {
    if (getContentManager() == null)
      return new ArrayList();

    return getHeaders(makePKArray(ids), peasId);
  }

  public int getSilverObjectId(String chatRoomId, String peasId)
      throws ChatException {

    try {
      return getContentManager().getSilverContentId(chatRoomId, peasId);
    } catch (Exception e) {
      throw new ChatException("ChatContentManager.getSilverObjectId()",
          SilverpeasException.ERROR,
          "chat.EX_IMPOSSIBLE_DOBTENIR_LE_SILVEROBJECTID", e);
    }
  }

  /**
   * add a new content. It is registered to contentManager service
   * @param con a Connection
   * @param chatRoom the content to register
   * @return the unique silverObjectId which identified the new content
   */
  public int createSilverContent(Connection con, ChatRoomDetail chatRoom)
      throws ContentManagerException {
    SilverContentVisibility scv = new SilverContentVisibility();
    return getContentManager().addSilverContent(con, chatRoom.getId(),
        chatRoom.getInstanceId(), chatRoom.getCreatorId(), scv);
  }

  /**
   * delete a content. It is registered to contentManager service
   * @param con a Connection
   * @param chatRoomId the identifiant of the content to unregister
   * @param componentId the identifiant of the component instance where the content to unregister is
   */
  public void deleteSilverContent(Connection con, String chatRoomId,
      String componentId) throws ContentManagerException {
    int contentId = getContentManager().getSilverContentId(chatRoomId,
        componentId);
    if (contentId != -1) {
      getContentManager().removeSilverContent(con, contentId, componentId);
    }
  }

  /**
   * return a list of room ids according to a list of silverContentId
   * @param idList a list of silverContentId
   * @return a list of String representing room ids
   */
  private ArrayList makePKArray(List idList) {
    ArrayList roomIds = new ArrayList();
    Iterator iter = idList.iterator();
    String id = null;

    // for each silverContentId, we get the corresponding roomId
    while (iter.hasNext()) {
      int contentId = ((Integer) iter.next()).intValue();
      try {
        id = getContentManager().getInternalContentId(contentId);
        roomIds.add(id);
      } catch (ClassCastException ignored) {
        // ignore unknown item
      } catch (ContentManagerException ignored) {
        // ignore unknown item
      }
    }
    return roomIds;
  }

  /**
   * return a list of silverContent according to a list of publicationPK
   * @param ids a list of room ids
   * @param peasId the component Id
   * @return a list of ChatRoomDetail
   */
  private List getHeaders(List ids, String peasId) {
    Iterator iter = ids.iterator();
    ArrayList headers = new ArrayList();
    int roomId = -1;
    ChatroomManager chatroomManager = ChatroomManager.getInstance();
    Chatroom aChatroom = null;
    ChatRoomDetail roomDetail = null;

    while (iter.hasNext()) {
      roomId = Integer.parseInt((String) iter.next());

      try {
        aChatroom = chatroomManager.getChatroom(roomId);
        if (aChatroom != null) {
          roomDetail = new ChatRoomDetail(peasId, String.valueOf(roomId),
              aChatroom.getParams().getName(), aChatroom.getParams()
              .getSubject(), null);
          headers.add(roomDetail);
        }
      } catch (jChatBox.Chat.ChatException ex) {
        SilverTrace.error("chat", "ChatContentManager.getHeaders()",
            "chat.MSG_ERR_GENERAL", null, ex);
      }
    }

    return headers;
  }

  private ContentManager getContentManager() {
    if (contentManager == null) {
      try {
        contentManager = new ContentManager();
      } catch (Exception e) {
        SilverLogger.getLogger(this).error(e.getMessage(), e);
      }
    }
    return contentManager;
  }

  private ContentManager contentManager = null;
}
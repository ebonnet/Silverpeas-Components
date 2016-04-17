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

package com.stratelia.silverpeas.pdcPeas.servlets;

import org.silverpeas.core.contribution.contentcontainer.content.GlobalSilverContent;
import com.stratelia.silverpeas.pdcPeas.control.PdcSearchUserWrapperSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;
import org.silverpeas.core.web.mvc.route.ComponentRequestRouter;
import org.silverpeas.components.whitepages.control.CardManager;
import org.silverpeas.components.whitepages.model.Card;
import org.silverpeas.core.web.http.HttpRequest;

import java.util.List;

/**
 * A simple wrapper for the userpanel.
 */
public class PdcSearchUserWrapper
    extends ComponentRequestRouter<PdcSearchUserWrapperSessionController> {

  private static final long serialVersionUID = 3997262296536961121L;

  /**
   * Returns a new session controller
   */
  public PdcSearchUserWrapperSessionController createComponentSessionController(
      MainSessionController mainSessionCtrl, ComponentContext componentContext) {
    return new PdcSearchUserWrapperSessionController(mainSessionCtrl,
        componentContext);
  }

  /**
   * Returns the base name for the session controller of this router.
   */
  public String getSessionControlBeanName() {
    return "pdcSearchUserWrapper";
  }

  /**
   * Do the requested function and return the destination url.
   */
  public String getDestination(String function,
      PdcSearchUserWrapperSessionController pdcSearchUserWrapperScc, HttpRequest request) {

    try {
      if ("Open".equals(function)) {
        pdcSearchUserWrapperScc.setFormName(request.getParameter("formName"));
        pdcSearchUserWrapperScc.setElementId(request.getParameter("elementId"));
        pdcSearchUserWrapperScc.setElementName(request
            .getParameter("elementName"));
        pdcSearchUserWrapperScc.setSelectedUserIds(request
            .getParameter("selectedUsers"));
        // selection par défaut d'éléments
        pdcSearchUserWrapperScc.initPdcSearchUser();
        return "/RpdcSearch/jsp/ToSearchToSelect?ComponentName=whitePages&ReturnURL=/RpdcSearchUserWrapper/jsp/Close";
      } else if (function.equals("Close")) {
        pdcSearchUserWrapperScc.getUserSelection();
        request.setAttribute("formName", pdcSearchUserWrapperScc.getFormName());
        request.setAttribute("elementId", pdcSearchUserWrapperScc
            .getElementId());
        request.setAttribute("elementName", pdcSearchUserWrapperScc
            .getElementName());

        List<GlobalSilverContent> users = pdcSearchUserWrapperScc.getSelectedUsers();
        if (users != null) {
          StringBuffer ids = new StringBuffer("");
          StringBuffer names = new StringBuffer("");
          CardManager cardM = CardManager.getInstance();

          for (GlobalSilverContent gsc : users) {
            String userCardId = gsc.getId();

            Card card = cardM.getCard(Long.parseLong(userCardId));
            String userId = card.getUserId();

            ids.append(userCardId);
            ids.append("-");
            ids.append(userId);
            ids.append(",");

            names.append(gsc.getName());
            names.append(",");
          }

          if (!ids.toString().equals("")) {
            ids = ids.deleteCharAt(ids.length() - 1);
            names = names.deleteCharAt(names.length() - 1);

          }

          request.setAttribute("userIds", ids.toString());
          request.setAttribute("userNames", names.toString());
        } else {
          request.setAttribute("userIds", "");
          request.setAttribute("userNames", "");
        }
        return "/pdcPeas/jsp/closeWrapper.jsp";
      } else {
        return "/RpdcSearch/jsp/" + function;
      }
    } catch (Exception e) {
      request.setAttribute("javax.servlet.jsp.jspException", e);
      return "/admin/jsp/errorpageMain.jsp";
    }
  }
}

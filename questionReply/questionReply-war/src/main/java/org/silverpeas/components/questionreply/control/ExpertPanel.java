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

package org.silverpeas.components.questionreply.control;

import org.silverpeas.core.web.panel.PanelLine;
import org.silverpeas.core.web.panel.PanelProvider;
import org.silverpeas.core.web.panel.PanelSearchEdit;
import org.silverpeas.core.web.panel.PanelSearchToken;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.util.LocalizationBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpertPanel extends PanelProvider {
  protected static final int FILTER_LASTNAME = 0;
  protected static final int FILTER_FIRSTNAME = 1;

  protected static final int COL_LASTNAME = 0;
  protected static final int COL_FIRSTNAME = 1;

  protected Map<String, UserDetail> allUserDetails = new HashMap<>();

  public ExpertPanel(String language, Collection<UserDetail> allExperts) {
    initAll(language, allExperts);
  }

  public void initAll(String lang, Collection<UserDetail> allExperts) {
    String[] filters = new String[2];
    setAllExperts(allExperts);
    // Set the language
    this.language = lang;
    LocalizationBundle message = ResourceLocator.getGeneralLocalizationBundle(language);
    // Set the resource locator for columns header
    messages = ResourceLocator.getLocalizationBundle(
        "org.silverpeas.questionReply.multilang.questionReplyBundle", language);
    // Set the Page name
    pageName = messages.getString("questionReply.experts");
    pageSubTitle = messages.getString("questionReply.experts");
    // Set column headers
    columnHeaders = new String[2];
    columnHeaders[COL_LASTNAME] = message.getString("GML.lastName");
    columnHeaders[COL_FIRSTNAME] = message.getString("GML.firstName");
    // Build search tokens
    searchTokens = new PanelSearchToken[2];
    searchTokens[FILTER_LASTNAME] = new PanelSearchEdit(0, message.getString("GML.lastName"), "");
    searchTokens[FILTER_FIRSTNAME] = new PanelSearchEdit(1, message.getString("GML.firstName"), "");
    // Set filters and get Ids
    filters[FILTER_FIRSTNAME] = "";
    filters[FILTER_LASTNAME] = "";
    refresh(filters);
  }

  public void refresh(String[] filters) {
    List<String> currentIds = new ArrayList<String>();
    for (UserDetail user : allUserDetails.values()) {
      boolean keepit = true;
      if (StringUtil.isDefined(filters[FILTER_FIRSTNAME])) {
        if ((user.getFirstName() == null) ||
            (filters[FILTER_FIRSTNAME].length() > user.getFirstName().length()) ||
            (!user.getFirstName().substring(0, filters[FILTER_FIRSTNAME].length())
                .equalsIgnoreCase(filters[FILTER_FIRSTNAME]))) {
          keepit = false;
        }
      }

      if (StringUtil.isDefined(filters[FILTER_LASTNAME])) {
        keepit = !(user.getLastName() == null ||
            filters[FILTER_LASTNAME].length() > user.getLastName().length() ||
            !user.getLastName().substring(0, filters[FILTER_LASTNAME].length())
                .equalsIgnoreCase(filters[FILTER_LASTNAME]));
      }
      if (keepit) {
        currentIds.add(user.getId());
      }
    }
    ids = currentIds.toArray(new String[currentIds.size()]);

    // Set search tokens values
    ((PanelSearchEdit) searchTokens[FILTER_FIRSTNAME]).m_Text =
        getSureString(filters[FILTER_FIRSTNAME]);
    ((PanelSearchEdit) searchTokens[FILTER_LASTNAME]).m_Text =
        getSureString(filters[FILTER_LASTNAME]);
    verifIndexes();
  }

  public void setAllExperts(Collection<UserDetail> allExperts) {
    if (allExperts != null) {
      allUserDetails.clear();
      for (UserDetail user : allExperts) {
        if (user != null) {
          allUserDetails.put(user.getId(), user);
        }
      }
    }
  }

  public PanelLine getElementInfos(String id) {
    UserDetail theUser = allUserDetails.get(id);

    String[] theValues = new String[2];
    theValues[COL_LASTNAME] = theUser.getLastName();
    theValues[COL_FIRSTNAME] = theUser.getFirstName();
    return new PanelLine(theUser.getId(), theValues, false);
  }
}

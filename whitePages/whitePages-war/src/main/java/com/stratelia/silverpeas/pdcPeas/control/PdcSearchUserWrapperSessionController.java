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
package com.stratelia.silverpeas.pdcPeas.control;

import org.silverpeas.core.pdc.pdc.service.Pdc;

import java.util.List;

import org.silverpeas.core.web.mvc.controller.AbstractComponentSessionController;
import org.silverpeas.core.web.mvc.controller.ComponentContext;
import org.silverpeas.core.web.mvc.controller.MainSessionController;

/**
 * A simple wrapper to the userpanel.
 * @author Didier Wenzek
 */
public class PdcSearchUserWrapperSessionController extends AbstractComponentSessionController {

  private Pdc pdc = null;

  public Pdc getPdc() {
    if (pdc == null) {
      pdc = new Pdc();
    }
    return pdc;
  }

  /**
   * Standard Session Controller Constructeur
   * @param mainSessionCtrl The full work session.
   * @param componentContext The context of this component session.
   */
  public PdcSearchUserWrapperSessionController(MainSessionController mainSessionCtrl,
      ComponentContext componentContext) {
    super(mainSessionCtrl, componentContext, "org.silverpeas.pdcPeas.multilang.pdcBundle",
        "org.silverpeas.pdcPeas.settings.pdcPeasIcons");
  }

  /**
   * Returns the HTML form name whose user element must be set.
   */
  public String getFormName() {
    return formName;
  }

  /**
   * Returns the HTML input where the selected user id must be set.
   */
  public String getElementId() {
    return elementId;
  }

  /**
   * Returns the HTML input where the selected user name must be set.
   */
  public String getElementName() {
    return elementName;
  }

  /**
   * Returns the selected user (if any).
   */
  public List getSelectedUsers() {
    return selectedUsers;
  }

  /**
   * Set the HTML form name whose user element must be set.
   */
  public void setFormName(String formName) {
    this.formName = formName;
  }

  /**
   * Set the HTML input where the selected user id must be set.
   */
  public void setElementId(String elementId) {
    this.elementId = elementId;
  }

  /**
   * Set the HTML input where the selected user name must be set.
   */
  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  /**
   * Set the selected user (if any).
   */
  public void setSelectedUserIds(String selectedUserIds) {
  }

  /**
   * Init the user panel.
   */
  public void initPdcSearchUser() {
  }

  /**
   * Reads the selection made with the user panel.
   */
  public void getUserSelection() {
    // get selected objects from PDC
    selectedUsers = getPdc().getSelectedSilverContents();
  }

  /**
   * The HTML form name whose user element must be set.
   */
  private String formName = null;
  /**
   * The HTML input where the selected user id must be set.
   */
  private String elementId = null;
  /**
   * The HTML input where the selected user name must be set.
   */
  private String elementName = null;
  /**
   * The selected user (if any).
   */
  private List selectedUsers = null;
}

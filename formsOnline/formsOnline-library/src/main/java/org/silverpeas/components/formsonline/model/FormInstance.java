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
package org.silverpeas.components.formsonline.model;

import org.silverpeas.core.contribution.model.SilverpeasContent;
import org.silverpeas.core.contribution.content.form.Form;
import org.silverpeas.core.admin.user.model.UserDetail;

import javax.persistence.Transient;
import java.util.Date;

public class FormInstance implements SilverpeasContent {
  public final static int STATE_UNREAD = 1;
  public final static int STATE_READ = 2;
  public final static int STATE_VALIDATED = 3;
  public final static int STATE_REFUSED = 4;
  public final static int STATE_ARCHIVED = 5;

  private String id;
  private int formId = -1;;
  private int state = -1;;
  private String creatorId = null;
  private Date creationDate = new Date();
  private String validatorId = null;
  private Date validationDate = null;
  private String comments = "";
  private String instanceId = null;

  @Transient
  protected FormDetail form;

  @Transient
  private boolean validationEnabled = false;

  @Transient
  private Form formWithData;

  @Override
  public String getId() {
    return id;
  }

  public int getIdAsInt() {
    return Integer.parseInt(getId());
  }

  @Override
  public String getComponentInstanceId() {
    return instanceId;
  }

  @Override
  public String getSilverpeasContentId() {
    return "";
  }

  /**
   * @param id the id to set
   */
  public void setId(int id) {
    this.id = Integer.toString(id);
  }

  /**
   * @return the formId
   */
  public int getFormId() {
    return formId;
  }

  /**
   * @param formId the formId to set
   */
  public void setFormId(int formId) {
    this.formId = formId;
  }

  /**
   * @return the state
   */
  public int getState() {
    return state;
  }

  /**
   * @param state the state to set
   */
  public void setState(int state) {
    this.state = state;
  }

  /**
   * @return the creatorId
   */
  public String getCreatorId() {
    return creatorId;
  }

  /**
   * @param creatorId the creatorId to set
   */
  public void setCreatorId(String creatorId) {
    this.creatorId = creatorId;
  }

  /**
   * @return the creationDate
   */
  public Date getCreationDate() {
    return creationDate != null ? new Date(creationDate.getTime()) : creationDate;
  }

  @Override
  public String getTitle() {
    return null;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public String getContributionType() {
    return null;
  }

  @Override
  public boolean canBeAccessedBy(final UserDetail user) {
    return false;
  }

  /**
   * @param creationDate the creationDate to set
   */
  public void setCreationDate(Date creationDate) {
    this.creationDate = (creationDate != null ? new Date(creationDate.getTime()) : creationDate);
  }

  /**
   * @return the validatorId
   */
  public String getValidatorId() {
    return validatorId;
  }

  /**
   * @param validatorId the validatorId to set
   */
  public void setValidatorId(String validatorId) {
    this.validatorId = validatorId;
  }

  /**
   * @return the validationDate
   */
  public Date getValidationDate() {
    return validationDate != null ? new Date(validationDate.getTime()) : validationDate;
  }

  /**
   * @param validationDate the validationDate to set
   */
  public void setValidationDate(Date validationDate) {
    this.validationDate =
        (validationDate != null ? new Date(validationDate.getTime()) : validationDate);
  }

  /**
   * @return the comments
   */
  public String getComments() {
    return comments;
  }

  /**
   * @param comments the comments to set
   */
  public void setComments(String comments) {
    this.comments = comments;
  }

  /**
   * @param instanceId the instanceId to set
   */
  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  public FormDetail getForm() {
    return form;
  }

  public void setForm(final FormDetail form) {
    this.form = form;
  }

  public boolean isRead() {
    return getState() == STATE_READ;
  }

  public boolean isValidated() {
    return getState() == STATE_VALIDATED;
  }

  public boolean isDenied() {
    return getState() == STATE_REFUSED;
  }

  public boolean isArchived() {
    return getState() == STATE_ARCHIVED;
  }

  public boolean isCanBeValidated() {
    return !isValidated() && !isDenied() && !isArchived();
  }

  public UserDetail getCreator() {
    return UserDetail.getById(getCreatorId());
  }

  public UserDetail getValidator() {
    return UserDetail.getById(getValidatorId());
  }

  public FormPK getFormPK() {
    return new FormPK(Integer.toString(getFormId()), getComponentInstanceId());
  }

  public boolean isValidationEnabled() {
    return validationEnabled;
  }

  public void setValidationEnabled(final boolean validationEnabled) {
    this.validationEnabled = validationEnabled;
  }

  public Form getFormWithData() {
    return formWithData;
  }

  public void setFormWithData(final Form formWithData) {
    this.formWithData = formWithData;
  }
}
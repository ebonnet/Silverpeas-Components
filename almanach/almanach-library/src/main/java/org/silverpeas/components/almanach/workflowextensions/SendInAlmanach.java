/*
 * Copyright (C) 2000 - 2016 Silverpeas
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * As a special exception to the terms and conditions of version 3.0 of the GPL, you may
 * redistribute this Program in connection with Free/Libre Open Source Software ("FLOSS")
 * applications as described in Silverpeas's FLOSS exception. You should have received a copy of the
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
package org.silverpeas.components.almanach.workflowextensions;

import org.silverpeas.core.contribution.content.form.DataRecordUtil;
import org.silverpeas.core.workflow.api.WorkflowException;
import org.silverpeas.core.workflow.api.model.Parameter;
import org.silverpeas.core.workflow.external.impl.ExternalActionImpl;
import org.silverpeas.core.silvertrace.SilverTrace;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.components.almanach.service.AlmanachRuntimeException;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.core.admin.service.OrganizationController;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;

import javax.inject.Inject;
import java.text.ParseException;
import java.util.Date;

/**
 * The aim of this class is to provide a new workflow extension in order to create an almanach event
 * from the process manager application. You must fill the following mandatory trigger parameters :
 * <ul>
 * <li>almanachId</li>
 * <li>name</li>
 * <li>startDate</li>
 * </ul>
 * To get more information you can also fill other trigger parameters : description, startHour,
 * endDate, endHour, place, link, priority<br/>
 * Watch {@link AlmanachTriggerParam} to get more trigger parameter information
 * @author ebonnet
 */
public class SendInAlmanach extends ExternalActionImpl {

  private String role = "unknown";
  private final String ADMIN_ID = "0";

  @Inject
  private OrganizationController organizationController;

  public SendInAlmanach() {
  }

  @Override
  public void execute() {
    setRole(getEvent().getUserRoleName());

    Parameter almanachParam =
        getTriggerParameter(AlmanachTriggerParam.APPLICATION_ID.getParameterName());

    Date startDate = getEventDate(AlmanachTriggerParam.START_DATE);

    // Check workflow export target parameter is valid
    if (isMandatoryTriggerParamValid(almanachParam, startDate)) {

      // Set event detail data
      EventDetail event = new EventDetail();
      event.setPK(new EventPK("", "useless", almanachParam.getValue()));
      event.setDelegatorId(getBestUserId());
      event.setName(getFolderValueFromTriggerParam(AlmanachTriggerParam.EVENT_NAME));
      event.setDescription(getFolderValueFromTriggerParam(AlmanachTriggerParam.EVENT_DESCRIPTION));
      event.setStartDate(startDate);
      String startHour = getFolderValueFromTriggerParam(AlmanachTriggerParam.START_HOUR);
      if (StringUtil.isValidHour(startHour)) {
        event.setStartHour(startHour);
      }
      Date endDate = getEventDate(AlmanachTriggerParam.END_DATE);
      if (endDate != null) {
        event.setEndDate(getEventDate(AlmanachTriggerParam.END_DATE));
      } else {
        // End date must be set to start date if not defined
        event.setEndDate(startDate);
      }
      String endHour = getFolderValueFromTriggerParam(AlmanachTriggerParam.END_HOUR);
      if (StringUtil.isValidHour(endHour)) {
        event.setEndHour(endHour);
      }
      event.setPlace(getFolderValueFromTriggerParam(AlmanachTriggerParam.PLACE));
      event.setEventUrl(getFolderValueFromTriggerParam(AlmanachTriggerParam.EVENT_URL));
      boolean priority =
          StringUtil.getBooleanValue(getFolderValueFromTriggerParam(AlmanachTriggerParam.PRIORITY));
      if (priority) {
        event.setPriority(1);
      }
      String eventId = getAlmanachBm().addEvent(event, null);
      SilverTrace
          .info("processManager", "SendInAlmanach", "Add an event successfully id=" + eventId);
    } else {
      StringBuilder warnMsg = new StringBuilder();
      warnMsg.append("Workflow export event problem :");
      if (almanachParam == null || !StringUtil.isDefined(almanachParam.getValue()) ||
          !getOrganizationController().isComponentExist(almanachParam.getValue())) {
        warnMsg.append("You must set a correct trigger parameter tp_almanachId.");
      }
      if (startDate == null) {
        warnMsg.append("You must set a correct trigger parameter tp_startDate.");
      }
      SilverTrace.warn("processManager", "SendInAlmanach", warnMsg.toString());
    }
  }

  /**
   * Check if almanach target application and event start date exist
   * @param almanachParam the almanach target application trigger parameter
   * @param startDate the event start date
   * @return true if all mandatory field exist
   */
  private boolean isMandatoryTriggerParamValid(Parameter almanachParam, Date startDate) {
    return almanachParam != null && StringUtil.isDefined(almanachParam.getValue()) &&
        getOrganizationController().isComponentExist(almanachParam.getValue()) && startDate != null;
  }

  Date getEventDate(AlmanachTriggerParam atp) {
    String triggerParamValue = retrieveTriggerParamValue(atp);
    if (triggerParamValue != null) {
      try {
        return DateUtil.parse(evaluateFolderValues(triggerParamValue), "dd/MM/yyyy");
      } catch (ParseException e) {
        SilverTrace.warn("processManager", "SendInAlmanach", "Start date loading error");
      }
    }
    return null;
  }

  /**
   * @param almanachParam the almanach trigger param
   * @return
   */
  private String getFolderValueFromTriggerParam(AlmanachTriggerParam almanachParam) {
    return evaluateFolderValues(retrieveTriggerParamValue(almanachParam));
  }

  /**
   * @param almanachParam
   * @return
   */
  private String retrieveTriggerParamValue(AlmanachTriggerParam almanachParam) {
    String triggerParamValue = StringUtil.EMPTY;
    Parameter triggerParam = getTriggerParameter(almanachParam.getParameterName());
    if (triggerParam != null) {
      triggerParamValue = triggerParam.getValue();
    }
    return triggerParamValue;
  }

  /**
   * @param triggerParamValue the trigger parameter value
   * @return the translated string if ${folder.XXX} has been evaluated successfully, the
   * triggerParamValue else if.
   */
  private String evaluateFolderValues(String triggerParamValue) {
    String evaluateValue = triggerParamValue;
    if (StringUtil.isDefined(triggerParamValue)) {
      try {
        evaluateValue =
            DataRecordUtil.applySubstitution(triggerParamValue, getProcessInstance()
                .getAllDataRecord(role,
                    "fr"), "fr");
      } catch (WorkflowException e) {
        SilverTrace.error("workflowEngine", "SendInAlmanach.execute()", "root.MSG_GEN_ERROR", e);
      }
    }
    return evaluateValue;
  }

  public String getRole() {
    return role;
  }

  public void setRole(String role) {
    this.role = role;
  }

  protected AlmanachService getAlmanachBm() {
    try {
      return ServiceProvider.getService(AlmanachService.class);
    } catch (Exception e) {
      throw new AlmanachRuntimeException("SendInAlmanach.getKmeliaBm()",
          SilverpeasRuntimeException.ERROR, "root.EX_CANT_GET_REMOTE_OBJECT", e);
    }
  }

  /**
   * Get actor if exist, admin otherwise
   * @return UserDetail
   */
  private String getBestUserId() {
    String currentUserId = ADMIN_ID;
    // For a manual action (event)
    if (getEvent().getUser() != null) {
      currentUserId = getEvent().getUser().getUserId();
    }
    return currentUserId;
  }

  protected OrganizationController getOrganizationController() {
    return organizationController;
  }

}
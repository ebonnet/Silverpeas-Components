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
package org.silverpeas.components.almanach.ui;

import org.silverpeas.core.web.search.AbstractResultDisplayer;
import org.silverpeas.core.web.search.ResultDisplayer;
import org.silverpeas.core.web.search.SearchResultContentVO;
import org.silverpeas.components.almanach.model.EventDetail;
import org.silverpeas.components.almanach.model.EventPK;
import org.silverpeas.components.almanach.model.Periodicity;
import org.silverpeas.components.almanach.service.AlmanachService;
import org.silverpeas.core.pdc.pdc.model.GlobalSilverResult;
import org.silverpeas.core.ui.DisplayI18NHelper;
import org.silverpeas.core.util.EncodeHelper;
import org.silverpeas.core.util.MultiSilverpeasBundle;
import org.silverpeas.core.util.ResourceLocator;
import org.silverpeas.core.util.SettingBundle;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.util.logging.SilverLogger;
import org.silverpeas.core.template.SilverpeasTemplate;
import org.silverpeas.core.template.SilverpeasTemplateFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Properties;

/**
 * <pre>
 * This class implements a ResultDisplayer in order to customize search result display. It uses
 * "Named" annotation to inject dependency.
 * Be careful to not modify this name that uses the following rules : componentName + POSTFIX_BEAN_NAME
 * POSTFIX_BEAN_NAME = ResultDisplayer
 * </pre>
 */
@Named("almanachResultDisplayer")
public class ResultSearchRenderer extends AbstractResultDisplayer implements ResultDisplayer {

  private static final Properties templateConfig = new Properties();
  private static final String TEMPLATE_FILENAME = "event_result_template";

  /**
   * Load template configuration
   */
  static {
    SettingBundle settings =
        ResourceLocator.getSettingBundle("org.silverpeas.almanach.settings.almanachSettings");
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_ROOT_DIR, settings
        .getString("templatePath"));
    templateConfig.setProperty(SilverpeasTemplate.TEMPLATE_CUSTOM_DIR, settings
        .getString("customersTemplatePath"));
  }

  /**
   * Attribute loaded with dependency injection
   */
  @Inject
  private AlmanachService almanachService;

  @Override
  public String getResultContent(SearchResultContentVO searchResult) {
    String result = "";

    // Retrieve the event detail from silverResult
    GlobalSilverResult silverResult = searchResult.getGsr();
    EventPK eventPK = new EventPK(silverResult.getId());
    EventDetail event = null;
    try {
      event = getAlmanachService().getEventDetail(eventPK);
    } catch (Exception e) {
      SilverLogger.getLogger(this)
          .warn("Unable to load event {0}: {1}", eventPK.getId(), e.getMessage());
    }
    // Create a SilverpeasTemplate
    SilverpeasTemplate template = getNewTemplate();
    this.setCommonAttributes(searchResult, template);

    if (event != null) {
      MultiSilverpeasBundle settings = searchResult.getSettings();
      setEventAttributes(event, template, settings);

      result =
          template.applyFileTemplate(TEMPLATE_FILENAME + '_' +
              DisplayI18NHelper.getDefaultLanguage());
    }
    return result;
  }

  /**
   * Add event attributes to template given in parameter
   * @param event the current event
   * @param template the template object where we add data
   * @param settings the specific almanach resources wrapper object
   */
  private void setEventAttributes(EventDetail event, SilverpeasTemplate template,
      MultiSilverpeasBundle settings) {
    template.setAttribute("eventDetail", event);
    template.setAttribute("evtStartDate", event.getStartDate());
    String location = event.getPlace();
    if (StringUtil.isDefined(location)) {
      template.setAttribute("evtLocation", location);
    }
    if (event.getEndDate() != null) {
      template.setAttribute("evtEndDate", event.getEndDate());
    }
    if (StringUtil.isDefined(event.getStartHour())) {
      template.setAttribute("evtStartHour", event.getStartHour());
    }
    if (StringUtil.isDefined(event.getEndHour())) {
      template.setAttribute("evtEndHour", event.getEndHour());
    }

    if (event.getPriority() != 0) {
      template.setAttribute("evtPriority", settings.getString("prioriteImportante"));
    } else {
      template.setAttribute("evtPriority", settings.getString("prioriteNormale"));
    }
    Periodicity periodicity = event.getPeriodicity();
    String strPeriod = null;
    if (periodicity == null) {
      strPeriod = settings.getString("noPeriodicity");
    } else {
      if (periodicity.getUnity() == Periodicity.UNIT_DAY) {
        strPeriod = settings.getString("allDays");
      } else if (periodicity.getUnity() == Periodicity.UNIT_WEEK) {
        strPeriod = settings.getString("allWeeks");
      } else if (periodicity.getUnity() == Periodicity.UNIT_MONTH) {
        strPeriod = settings.getString("allMonths");
      } else if (periodicity.getUnity() == Periodicity.UNIT_YEAR) {
        strPeriod = settings.getString("allYears");
      }
    }
    if (StringUtil.isDefined(strPeriod)) {
      template.setAttribute("evtPeriodicity", strPeriod);
    }

    String eventURL = event.getEventUrl();
    if (StringUtil.isDefined(eventURL)) {
      if (!eventURL.contains("://")) {
        eventURL = "http://" + eventURL;
      }
      template.setAttribute("evtURL", EncodeHelper.javaStringToHtmlString(eventURL));
    }
  }

  /**
   * @return a new Silverpeas Template
   */
  protected SilverpeasTemplate getNewTemplate() {
    return SilverpeasTemplateFactory.createSilverpeasTemplate(templateConfig);
  }

  /**
   * @return the almanachService
   */
  public AlmanachService getAlmanachService() {
    return almanachService;
  }

  /**
   * @param almanachService the almanachService to set
   */
  public void setAlmanachService(AlmanachService almanachService) {
    this.almanachService = almanachService;
  }

}

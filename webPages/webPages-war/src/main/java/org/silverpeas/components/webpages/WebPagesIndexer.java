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

/*
 * Created on 19 avr. 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.silverpeas.components.webpages;

import org.silverpeas.core.contribution.content.form.FormException;
import org.silverpeas.core.contribution.content.form.RecordSet;
import org.silverpeas.core.contribution.template.publication.PublicationTemplate;
import org.silverpeas.core.contribution.template.publication.PublicationTemplateManager;
import org.silverpeas.core.web.index.components.ComponentIndexation;
import org.silverpeas.core.admin.service.Administration;
import org.silverpeas.core.admin.component.model.ComponentInst;
import org.silverpeas.core.admin.user.model.UserDetail;
import org.silverpeas.core.contribution.attachment.AttachmentService;
import org.silverpeas.components.webpages.model.WebPagesException;
import org.silverpeas.core.index.indexing.model.FullIndexEntry;
import org.silverpeas.core.index.indexing.model.IndexEngineProxy;
import org.silverpeas.core.ForeignPK;
import org.silverpeas.core.util.StringUtil;
import org.silverpeas.core.exception.SilverpeasException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;

/**
 * @author sdevolder
 */
@Singleton
@Named("webPages" + ComponentIndexation.QUALIFIER_SUFFIX)
public class WebPagesIndexer implements ComponentIndexation {

  private static String XML_TEMPLATE_PARAM = "xmlTemplate";

  @Inject
  private Administration admin;
  @Inject
  private PublicationTemplateManager templateManager;
  @Inject
  private AttachmentService attachmentService;

  @Override
  public void index(ComponentInst componentInst) throws Exception {
    if (isXMLTemplateUsed(componentInst.getId())) {
      indexForm(componentInst);
    } else {
      ForeignPK foreignPK = new ForeignPK(componentInst.getId(), componentInst.getId());
      attachmentService.indexAllDocuments(foreignPK, null, null);
    }
  }

  private void indexForm(ComponentInst componentInst) throws WebPagesException {
    RecordSet recordSet;
    try {
      PublicationTemplate pub = templateManager.getPublicationTemplate(
          componentInst.getId() + ":" + getShortNameOfXMLTemplateUsedFor(componentInst.getId()));
      recordSet = pub.getRecordSet();
    } catch (Exception e) {
      throw new WebPagesException("WebPagesIndexer.indexForm()", SilverpeasException.ERROR,
          "webPages.EX_CANT_GET_FORM", e);
    }
    // index data
    try {
      FullIndexEntry indexEntry =
          new FullIndexEntry(componentInst.getId(), "Component", componentInst.getId());
      indexEntry.setCreationDate(new Date());
      indexEntry.setCreationUser(UserDetail.getCurrentRequester().getId());
      indexEntry.setTitle(componentInst.getLabel());
      indexEntry.setPreView(componentInst.getDescription());

      recordSet
          .indexRecord("0", getShortNameOfXMLTemplateUsedFor(componentInst.getId()), indexEntry);

      IndexEngineProxy.addIndexEntry(indexEntry);
    } catch (FormException e) {
      throw new WebPagesException("WebPagesIndexer.indexForm()", SilverpeasException.ERROR,
          "webPages.EX_CANT_INDEX_DATA", e);
    }
  }

  private String getXMLTemplateUsedFor(String componentId) {
    return admin.getComponentParameterValue(componentId, XML_TEMPLATE_PARAM);
  }

  private boolean isXMLTemplateUsed(String componentId) {
    return StringUtil.isDefined(getXMLTemplateUsedFor(componentId));
  }

  private String getShortNameOfXMLTemplateUsedFor(String componentId) {
    String xmlFormName = getXMLTemplateUsedFor(componentId);
    return xmlFormName.substring(xmlFormName.indexOf("/") + 1, xmlFormName.indexOf("."));
  }

}

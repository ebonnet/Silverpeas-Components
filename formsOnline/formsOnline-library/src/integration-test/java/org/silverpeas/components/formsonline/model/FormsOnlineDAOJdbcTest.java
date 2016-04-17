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

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.silverpeas.core.test.BasicWarBuilder;
import org.silverpeas.core.test.rule.DbUnitLoadingRule;
import org.silverpeas.core.util.DateUtil;
import org.silverpeas.core.util.ServiceProvider;

import java.text.ParseException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author ebonnet
 */
@RunWith(Arquillian.class)
public class FormsOnlineDAOJdbcTest {

  @Rule
  public DbUnitLoadingRule dbUnitLoadingRule =
      new DbUnitLoadingRule("create-database.sql", "forms-dataset.xml");

  @Deployment
  public static Archive<?> createTestArchive() {
    return BasicWarBuilder.onWarForTestClass(FormsOnlineDAOJdbcTest.class)
        .testFocusedOn(warBuilder -> {
          warBuilder.addMavenDependenciesWithPersistence("org.silverpeas.core:silverpeas-core");
          warBuilder.addMavenDependencies("org.silverpeas.core.services:silverpeas-core-tagcloud");
          warBuilder.addAsResource("META-INF/test-MANIFEST.MF", "META-INF/MANIFEST.MF");
          warBuilder.addMavenDependencies("org.apache.tika:tika-core");
          warBuilder.addMavenDependencies("org.apache.tika:tika-parsers");
          warBuilder.addPackages(true, "org.silverpeas.components.formsonline");
        }).build();
  }

  private FormsOnlineDAO dao;

  @Before
  public void generalSetup() {
    dao = ServiceProvider.getService(FormsOnlineDAO.class);
  }

  public FormsOnlineDAOJdbcTest() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of createForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testCreateForm() throws Exception {
    FormDetail formDetail = new FormDetail();
    formDetail.setInstanceId("formsOnline15");
    formDetail.setCreatorId("0");
    formDetail.setName("Demande de construction de salle...");
    formDetail.setState(FormInstance.STATE_UNREAD);
    formDetail.setTitle("Titre");
    formDetail.setXmlFormName("descriptif_salle.xml");
    FormDetail result = dao.createForm(formDetail);
    assertEquals(1002, result.getId());
  }

  /**
   * Test of getForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testGetForm() throws Exception {
    String instanceId = "formsOnline100";
    int formId = 1000;
    FormDetail expResult = getFormDetailExpectedResult();
    FormDetail result = dao.getForm(new FormPK(formId, instanceId));
    assertEquals(expResult.equals(result), true);
  }

  private FormDetail getFormDetailExpectedResult() throws ParseException {
    FormDetail expResult = new FormDetail();
    expResult.setId(1000);
    expResult.setCreatorId("0");
    expResult.setAlreadyUsed(true);
    expResult.setCreationDate(DateUtil.parse("2012/01/09"));
    expResult.setDescription("Formulaire de description d'une salle");
    expResult.setInstanceId("formsOnline100");
    expResult.setName("Référencement des salles");
    expResult.setState(FormInstance.STATE_UNREAD);
    expResult.setTitle("Titre de mon formulaire en ligne");
    expResult.setXmlFormName("descriptif_salle.xml");
    return expResult;
  }


  /**
   * Test of findAllForms method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testFindAllForms() throws Exception {
    String instanceId = "formsOnline100";
    List<FormDetail> result = dao.findAllForms(instanceId);
    assertEquals(result.size(), 2);
  }

  /**
   * Test of updateForm method, of class FormsOnlineDAOJdbc.
   */
  @Test
  public void testUpdateForm() throws Exception {
    FormDetail curForm = getFormDetailExpectedResult();
    curForm.setState(FormInstance.STATE_READ);
    dao.updateForm(curForm);
    // TODO remove following lines
    // Need to create another connection because the last one was closed inside updateForm method
//    dao = new FormsOnlineDAOJdbcMock(dataSource.getConnection());
    FormDetail updatedForm = dao.getForm(curForm.getPK());
    assertEquals(curForm.equals(updatedForm), true);
  }

  @Test
  public void testGetReceivedRequests() throws Exception {
    List<FormInstance> forms =
        dao.getReceivedRequests(new FormPK(1000, "formsOnline100"), true, null);
    assertEquals(1, forms.size());
    assertEquals(1000, forms.get(0).getFormId());
    assertEquals("20", forms.get(0).getId());
    assertEquals("Salle 18 disponible", forms.get(0).getComments());
  }

}

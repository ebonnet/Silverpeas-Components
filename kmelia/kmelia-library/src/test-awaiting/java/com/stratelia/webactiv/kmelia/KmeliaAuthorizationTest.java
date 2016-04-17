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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.silverpeas.components.kmelia;

import com.silverpeas.jcrutil.RandomGenerator;
import com.stratelia.webactiv.SilverpeasRole;
import com.stratelia.webactiv.beans.admin.DefaultOrganizationController;
import org.silverpeas.components.kmelia.control.KmeliaBm;
import com.stratelia.webactiv.node.control.NodeService;
import org.silverpeas.core.contribution.publication.control.PublicationService;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;
import org.silverpeas.core.contribution.publication.model.PublicationPK;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.silverpeas.core.admin.service.OrganizationController;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 *
 * @author ehugonnet
 */
public class KmeliaAuthorizationTest {

  public KmeliaAuthorizationTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  /**
   * Test of enableCache method, of class KmeliaAuthorization.
   */
  @Test
  public void testEnableCache() {
    KmeliaAuthorization instance = new KmeliaAuthorization();
    instance.enableCache();
    assertTrue(instance.isCacheEnabled());
  }

  /**
   * Test of disableCache method, of class KmeliaAuthorization.
   */
  @Test
  public void testDisableCache() {
    KmeliaAuthorization instance = new KmeliaAuthorization();
    instance.disableCache();
    assertFalse(instance.isCacheEnabled());
  }

  /**
   * Test of isAccessAuthorized method, of class KmeliaAuthorization.
   *
   * @throws Exception
   */
  @Test
  public void testIsAccessAuthorizedForValidPublication() throws Exception {
    String instanceId = "100";
    String adminId = "11";
    OrganizationController controller = mock(DefaultOrganizationController.class);
    when(controller.isComponentAvailable(eq(instanceId), anyString())).thenReturn(Boolean.TRUE);
    when(controller.isComponentAvailable(instanceId, "10")).thenReturn(Boolean.FALSE);
    when(controller.getComponentParameterValue(instanceId, KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn(null);
    when(controller.getUserProfiles(adminId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.admin.toString(), SilverpeasRole.user.toString()});
    KmeliaAuthorization instance = new KmeliaAuthorization(controller);
    NodeService nodeService = mock(NodeService.class);
    instance.setNodeBm(nodeService);
    PublicationService validBm = mock(PublicationService.class);
    PublicationDetail validPublication = mock(PublicationDetail.class);
    when(validPublication.getStatus()).thenReturn(PublicationDetail.VALID);
    PublicationPK validPk = new PublicationPK("2000", null, instanceId);
    when(validBm.getDetail(validPk)).thenReturn(validPublication);
    instance.setPublicationBm(validBm);
    KmeliaBm kmeliaBm = mock(KmeliaBm.class);
    when(kmeliaBm.isPublicationVisible(validPublication, SilverpeasRole.admin, adminId)).thenReturn(
        true);
    instance.setKmeliaBm(kmeliaBm);

    assertFalse("If component is not avilable, no access is granted", instance.isAccessAuthorized(
        instanceId, "10", "1000"));
    assertTrue("Valid publication are accessible with no rights on topic", instance.
        isAccessAuthorized(instanceId, adminId, "1000"));
    assertTrue("Valid publication are accessible with no rights on topic", instance.
        isAccessAuthorized(instanceId, adminId, "2000"));
  }

  @Test
  public void testIsAccessAuthorizedForToValidatePublication() throws Exception {
    String instanceId = "100";
    String adminId = "11";
    String userId = "12";
    String publisherId = "13";
    String authorWriterId = "14";
    String notAuthorizedWriterId = "15";
    OrganizationController controller = mock(DefaultOrganizationController.class);
    when(controller.isComponentAvailable(eq(instanceId), anyString())).thenReturn(Boolean.TRUE);
    when(controller.getComponentParameterValue(instanceId, KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn(null);
    when(controller.getUserProfiles(adminId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.admin.toString(), SilverpeasRole.user.toString()});
    when(controller.getUserProfiles(userId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.reader.toString()});
    when(controller.getUserProfiles(publisherId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.publisher.toString()});
    when(controller.getUserProfiles(authorWriterId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.writer.toString()});
    when(controller.getUserProfiles(notAuthorizedWriterId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.writer.toString()});
    KmeliaAuthorization instance = new KmeliaAuthorization(controller);
    NodeService nodeService = mock(NodeService.class);
    instance.setNodeBm(nodeService);
    PublicationService validBm = mock(PublicationService.class);
    PublicationDetail toValidatePublication = mock(PublicationDetail.class);
    when(toValidatePublication.getStatus()).thenReturn(PublicationDetail.TO_VALIDATE);
    when(toValidatePublication.isValidationRequired()).thenReturn(Boolean.TRUE);
    when(toValidatePublication.isValid()).thenReturn(Boolean.FALSE);
    when(toValidatePublication.isPublicationEditor(authorWriterId)).thenReturn(Boolean.TRUE);
    PublicationPK toValidatePk = new PublicationPK("3000", null, instanceId);
    when(validBm.getDetail(toValidatePk)).thenReturn(toValidatePublication);
    instance.setPublicationBm(validBm);
    KmeliaBm kmeliaBm = mock(KmeliaBm.class);
    when(kmeliaBm.isPublicationVisible(eq(toValidatePublication), any(SilverpeasRole.class),
        anyString())).thenReturn(true);
    instance.setKmeliaBm(kmeliaBm);

    assertTrue("Admin, publisher, creator or updater has access", instance.isAccessAuthorized(
        instanceId, adminId, "3000"));
    assertFalse("Admin, publisher, creator or updater has access", instance.isAccessAuthorized(
        instanceId, userId, "3000"));
    assertTrue("Admin, publisher, creator or updater has access", instance.isAccessAuthorized(
        instanceId, publisherId, "3000"));
    assertTrue("Admin, publisher, creator or updater has access", instance.isAccessAuthorized(
        instanceId, authorWriterId, "3000"));
    assertFalse("Admin, publisher, creator or updater has access", instance.isAccessAuthorized(
        instanceId, notAuthorizedWriterId, "3000"));
  }

  @Test
  public void testIsAccessAuthorizedForDraftPublication() throws Exception {
    String instanceId = "100";
    String adminId = "11";
    String userId = "12";
    String publisherId = "13";
    String authorWriterId = "14";
    String notAuthorizedWriterId = "15";
    OrganizationController controller = mock(DefaultOrganizationController.class);
    when(controller.isComponentAvailable(eq(instanceId), anyString())).thenReturn(Boolean.TRUE);
    when(controller.getComponentParameterValue(instanceId, KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn(null);
    when(controller.getUserProfiles(adminId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.admin.toString(), SilverpeasRole.user.toString()});
    when(controller.getUserProfiles(userId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.reader.toString()});
    when(controller.getUserProfiles(publisherId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.publisher.toString()});
    when(controller.getUserProfiles(authorWriterId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.writer.toString()});
    when(controller.getUserProfiles(notAuthorizedWriterId, instanceId)).thenReturn(new String[]{
      SilverpeasRole.user.toString(), SilverpeasRole.writer.toString()});
    KmeliaAuthorization instance = new KmeliaAuthorization(controller);
    NodeService nodeService = mock(NodeService.class);
    instance.setNodeBm(nodeService);
    PublicationService validBm = mock(PublicationService.class);
    PublicationDetail toValidatePublication = mock(PublicationDetail.class);
    when(toValidatePublication.getStatus()).thenReturn(PublicationDetail.DRAFT);
    when(toValidatePublication.isDraft()).thenReturn(Boolean.TRUE);
    when(toValidatePublication.isPublicationEditor(authorWriterId)).thenReturn(Boolean.TRUE);
    PublicationPK toValidatePk = new PublicationPK("4000", null, instanceId);
    when(validBm.getDetail(toValidatePk)).thenReturn(toValidatePublication);
    instance.setPublicationBm(validBm);
    KmeliaBm kmeliaBm = mock(KmeliaBm.class);
    when(kmeliaBm.isPublicationVisible(eq(toValidatePublication), any(SilverpeasRole.class),
        anyString())).thenReturn(true);
    instance.setKmeliaBm(kmeliaBm);

    assertFalse("Only the creator or updater has access", instance.isAccessAuthorized(instanceId,
        adminId, "4000"));
    assertFalse("Only the creator or updater has access", instance.isAccessAuthorized(instanceId,
        userId, "4000"));
    assertFalse("Only the creator or updater has access", instance.isAccessAuthorized(instanceId,
        publisherId, "4000"));
    assertTrue("Only the creator or updater has access", instance.isAccessAuthorized(instanceId,
        authorWriterId, "4000"));
    assertFalse("Only the creator or updater has access", instance.isAccessAuthorized(instanceId,
        notAuthorizedWriterId, "4000"));
  }

  /**
   * Test of isAccessAuthorized method, of class KmeliaAuthorization.
   */
  /*@Test
   public void testIsAccessAuthorizedForAnyObject() {
   System.out.println("isAccessAuthorized");
   String componentId = "";
   String userId = "";
   String objectId = "";
   String objectType = "";
   KmeliaAuthorization instance = new KmeliaAuthorization();
   boolean expResult = false;
   boolean result = instance.isAccessAuthorized(componentId, userId, objectId, objectType);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
  /**
   * Test of isObjectAvailable method, of class KmeliaAuthorization.
   */
  @Test
  public void testIsObjectAvailable() {
    OrganizationController controller = mock(DefaultOrganizationController.class);
    KmeliaAuthorization instance = new KmeliaAuthorization(controller);
    assertTrue("Object not of type kmelia", instance.isObjectAvailable("100", "10", "1000", "toto"));
  }

  /**
   * Test of isKmeliaObjectType method, of class KmeliaAuthorization.
   */
  @Test
  public void testIsKmeliaObjectType() {
    KmeliaAuthorization instance = new KmeliaAuthorization();
    assertFalse(instance.isKmeliaObjectType(null));
    assertFalse(instance.isKmeliaObjectType(RandomGenerator.getRandomString()));
    assertFalse(instance.isKmeliaObjectType(KmeliaAuthorization.NODE_TYPE));
    assertTrue(instance.isKmeliaObjectType(KmeliaAuthorization.PUBLICATION_TYPE));
    assertTrue(instance.isKmeliaObjectType("Attachment" + RandomGenerator.getRandomString()));
    assertFalse(instance.isKmeliaObjectType("attachment" + RandomGenerator.getRandomString()));
    assertTrue(instance.isKmeliaObjectType("Version" + RandomGenerator.getRandomString()));
    assertFalse(instance.isKmeliaObjectType("version" + RandomGenerator.getRandomString()));
  }

  /**
   * Test of isRightsOnTopicsEnable method, of class KmeliaAuthorization.
   */
  @Test
  public void testIsRightsOnTopicsEnabled() {
    OrganizationController controller = mock(DefaultOrganizationController.class);
    when(controller.getComponentParameterValue("100", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("yes");
    when(controller.getComponentParameterValue("101", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("Yes");
    when(controller.getComponentParameterValue("102", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("Y");
    when(controller.getComponentParameterValue("103", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("1");

    when(controller.getComponentParameterValue("200", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn(null);
    when(controller.getComponentParameterValue("201", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("no");
    when(controller.getComponentParameterValue("202", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("0");
    when(controller.getComponentParameterValue("203", KmeliaAuthorization.RIGHTS_ON_TOPIC_PARAM)).
        thenReturn("");
    KmeliaAuthorization instance = new KmeliaAuthorization(controller);

    assertTrue(instance.isRightsOnTopicsEnabled("100"));
    assertTrue(instance.isRightsOnTopicsEnabled("101"));
    assertTrue(instance.isRightsOnTopicsEnabled("102"));
    assertTrue(instance.isRightsOnTopicsEnabled("103"));

    assertFalse(instance.isRightsOnTopicsEnabled("200"));
    assertFalse(instance.isRightsOnTopicsEnabled("201"));
    assertFalse(instance.isRightsOnTopicsEnabled("202"));
    assertFalse(instance.isRightsOnTopicsEnabled("203"));

  }
  /**
   * Test of isPublicationAvailable method, of class KmeliaAuthorization.
   */
  /*@Test
   public void testIsPublicationAvailable() {
   System.out.println("isPublicationAvailable");
   PublicationPK pk = null;
   String userId = "";
   KmeliaAuthorization instance = new KmeliaAuthorization();
   boolean expResult = false;
   boolean result = instance.isPublicationAvailable(pk, userId);
   assertEquals(expResult, result);
   // TODO review the generated test code and remove the default call to fail.
   fail("The test case is a prototype.");
   }*/
}

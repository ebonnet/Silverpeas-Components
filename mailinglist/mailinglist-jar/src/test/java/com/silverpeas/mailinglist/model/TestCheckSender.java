package com.silverpeas.mailinglist.model;

import java.io.IOException;
import java.sql.SQLException;

import javax.jms.QueueConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.Reference;

import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jvnet.mock_javamail.Mailbox;

import com.mockrunner.mock.jms.MockQueue;
import com.silverpeas.mailinglist.AbstractSilverpeasDatasourceSpringContextTests;
import com.silverpeas.mailinglist.service.ServicesFactory;
import com.silverpeas.mailinglist.service.model.beans.InternalUser;
import com.silverpeas.mailinglist.service.model.beans.MailingList;
import com.stratelia.webactiv.util.JNDINames;
import com.stratelia.webactiv.util.fileFolder.FileFolderManager;

public class TestCheckSender extends
    AbstractSilverpeasDatasourceSpringContextTests {

  private static final String ArchivageNotModeratedOpen_ID = "101";
  private static final String ArchivageNotModeratedClosed_ID = "102";

  protected String[] getConfigLocations() {
    return new String[] { "spring-checker.xml", "spring-notification.xml",
        "spring-hibernate.xml", "spring-datasource.xml" };
  }

  protected void onTearDown() {
    Mailbox.clearAll();
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      FileFolderManager.deleteFolder("c:\\tmp\\uploads\\componentId", false);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected void onSetUp() {
    Mailbox.clearAll();
    IDatabaseConnection connection = null;
    try {
      connection = getConnection();
      DatabaseOperation.DELETE_ALL.execute(connection, getDataSet());
      DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
      registerMockJMS();
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      if (connection != null) {
        try {
          connection.getConnection().close();
        } catch (SQLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  protected IDataSet getDataSet() throws DataSetException, IOException {
    FlatXmlDataSet dataSet;
    if (isOracle()) {
      dataSet = new FlatXmlDataSet(TestCheckSender.class
          .getResourceAsStream("test-check-sender-dataset.xml"));
    } else {
      dataSet = new FlatXmlDataSet(TestCheckSender.class
          .getResourceAsStream("test-check-sender-dataset.xml"));
    }
    return dataSet;
  }

  protected void registerMockJMS() throws Exception {
    InitialContext ic = new InitialContext();
    // Construct BasicDataSource reference
    Reference refFactory = new Reference("javax.jms.QueueConnectionFactory",
        "com.silverpeas.mailinglist.jms.MockObjectFactory", null);
    ic.rebind(JNDINames.JMS_FACTORY, refFactory);
    Reference refQueue = new Reference("javax.jms.Queue",
        "com.silverpeas.mailinglist.jms.MockObjectFactory", null);
    ic.rebind(JNDINames.JMS_QUEUE, refQueue);
    QueueConnectionFactory qconFactory = (QueueConnectionFactory) ic
        .lookup(JNDINames.JMS_FACTORY);
    assertNotNull(qconFactory);
    MockQueue queue = (MockQueue) ic.lookup(JNDINames.JMS_QUEUE);
    queue.clear();
  }

  public void testArchivageNotModeratedOpen() {
    String email = "maggie.simpson@silverpeas.com";
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        ArchivageNotModeratedOpen_ID);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertTrue(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non mod�r�e et ouverte avec un lecteur abonn�", list
            .getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    InternalUser reader = (InternalUser) list.getReaders().iterator().next();
    assertEquals(email, reader.getEmail());
    assertNotNull(list.getModerators());
    assertEquals(0, list.getModerators().size());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    MailingListComponent component = new MailingListComponent(
        ArchivageNotModeratedOpen_ID);
    assertTrue(component.checkSender(email));
    assertTrue(list.isEmailAuthorized(email));
  }

  public void testArchivageNotModeratedClosed() {
    String email = "lisa.simpson@silverpeas.com";
    String spammer = "joe.theplumber@spam.com";
    MailingList list = ServicesFactory.getMailingListService().findMailingList(
        ArchivageNotModeratedClosed_ID);
    assertNotNull(list);
    assertFalse(list.isModerated());
    assertFalse(list.isOpen());
    assertFalse(list.isNotify());
    assertEquals(
        "Liste archivage non mod�r�e et ferm�e avec un lecteur abonn�", list
            .getName());
    assertEquals("thesimpsons@silverpeas.com", list.getSubscribedAddress());
    assertNotNull(list.getInternalSubscribers());
    assertEquals(1, list.getInternalSubscribers().size());
    assertNotNull(list.getReaders());
    assertEquals(1, list.getReaders().size());
    assertNotNull(list.getModerators());
    assertEquals(0, list.getModerators().size());
    InternalUser reader = (InternalUser) list.getReaders().iterator().next();
    assertEquals(email, reader.getEmail());
    assertNotNull(list.getExternalSubscribers());
    assertEquals(0, list.getExternalSubscribers().size());
    assertNotNull(list.getGroupSubscribers());
    assertEquals(0, list.getGroupSubscribers().size());
    MailingListComponent component = new MailingListComponent(
        ArchivageNotModeratedClosed_ID);
    assertTrue(component.checkSender(email));
    assertTrue(list.isEmailAuthorized(email));
    assertFalse(component.checkSender(spammer));
    assertFalse(list.isEmailAuthorized(spammer));
  }

}
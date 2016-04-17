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
 * FLOSS exception.  You should have received a copy of the text describing
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

package org.silverpeas.components.kmelia.updatechainhelpers;

import org.silverpeas.core.pdc.pdc.model.ClassifyPosition;
import org.silverpeas.core.pdc.pdc.model.ClassifyValue;
import org.silverpeas.core.pdc.pdc.model.PdcException;
import org.silverpeas.core.pdc.pdc.model.Value;
import org.silverpeas.components.kmelia.control.KmeliaSessionController;
import org.silverpeas.components.kmelia.model.KmeliaRuntimeException;
import org.silverpeas.core.persistence.jdbc.DBUtil;
import org.silverpeas.core.exception.SilverpeasRuntimeException;
import org.silverpeas.core.node.model.NodeDetail;
import org.silverpeas.core.contribution.publication.model.PublicationDetail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class DefineServiceOfUserAndDocuments extends UpdateChainHelperImpl {

  public void execute(UpdateChainHelperContext uchc) {
    KmeliaSessionController kmeliaScc = uchc.getKmeliaScc();

    // récupération des données
    PublicationDetail pubDetail = uchc.getPubDetail();

    // Retrieve service and matricule of user
    String userName = pubDetail.getName();
    String lastName = getField("lastname", userName);
    String firstName = getField("firstname", userName);
    String service = getField("service", userName);
    String matricule = getField("matricule", userName);

    // associer le service au node
    String[] topics = new String[1];
    List<NodeDetail> allTopics = uchc.getAllTopics();
    for (NodeDetail node : allTopics) {
      if (node.getName().toUpperCase().equals(service.toUpperCase())) {
        topics[0] = node.getId() + "," + node.getNodePK().getInstanceId();
      }
    }
    uchc.setTopics(topics);

    // Maj Publication
    pubDetail.setName(matricule + " " + lastName.toUpperCase() + " " + firstName.toUpperCase());
    String newDescription = pubDetail.getDescription().concat(" ").concat(pubDetail.getKeywords());
    pubDetail.setDescription(newDescription);
    String keywords = kmeliaScc.getComponentLabel();
    pubDetail.setKeywords(keywords);

    uchc.setPubDetail(pubDetail);

    // Classer la publication sur le service
    int silverObjectId = kmeliaScc.getSilverObjectId(pubDetail.getId());
    try {
      List<Value> axisValues = kmeliaScc.getPdcManager().getAxisValuesByName(service);
      for (Value axisValue : axisValues) {
        String selectedPosition = axisValue.getTreeId() + "|" + axisValue.getFullPath();
        ClassifyPosition position = buildPosition(null, selectedPosition);
        kmeliaScc.getPdcManager()
            .addPosition(silverObjectId, position, kmeliaScc.getComponentId(), false);
      }
    } catch (PdcException pde) {
      pde.printStackTrace();
    }
  }


  private String getField(String field, String userName) {
    Connection con = getConnection();
    String result = "";

    String query = "select " + field +
        " from personnel where (lastname||' '||firstname|| ' '||matricule) = ? ";

    try (PreparedStatement prepStmt = con.prepareStatement(query)) {
      prepStmt.setString(1, userName);
      try (ResultSet rs = prepStmt.executeQuery()) {
        while (rs.next()) {
          // Retrieve result
          result = rs.getString(1);
        }
      }
    } catch (SQLException sqlEx) {
      throw new KmeliaRuntimeException("DefineServiceOfUserAndDocuments.getUserServiceMatricule()",
          SilverpeasRuntimeException.ERROR, "kmelia.SERVICE_NOT_EXIST", sqlEx);
    } finally {
      freeConnection(con);
    }
    return result;
  }

  private Connection getConnection() {
    try {
      Connection con = DBUtil.openConnection();
      return con;
    } catch (Exception e) {
      throw new KmeliaRuntimeException("DefineServiceOfUser.getConnection()",
          SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_OPEN_FAILED", e);
    }
  }

  private void freeConnection(Connection con) {
    if (con != null) {
      try {
        con.close();
      } catch (Exception e) {
        throw new KmeliaRuntimeException("DefineServiceOfUser.getConnection()",
            SilverpeasRuntimeException.ERROR, "root.EX_CONNECTION_CLOSE_FAILED", "", e);
      }
    }
  }

  private ClassifyPosition buildPosition(String positionId, String valuesFromJsp) {
    // valuesFromJsp looks like 12|/0/1/2/,14|/15/34/
    // [axisId|valuePath+valueId]*
    StringTokenizer st = new StringTokenizer(valuesFromJsp, ",");
    String valueInfo = "";
    String axisId = "";
    String valuePath = "";
    ClassifyValue value = null;
    List<ClassifyValue> values = new ArrayList<>();
    for (; st.hasMoreTokens(); ) {
      valueInfo = st.nextToken();
      if (valueInfo.length() >= 3) {
        axisId = valueInfo.substring(0, valueInfo.indexOf('|'));
        valuePath = valueInfo.substring(valueInfo.indexOf('|') + 1, valueInfo.length());
        value = new ClassifyValue(Integer.parseInt(axisId), valuePath);
        values.add(value);
      }
    }

    int id = -1;
    if (positionId != null) {
      id = Integer.parseInt(positionId);
    }
    ClassifyPosition position = new ClassifyPosition(values);
    position.setPositionId(id);
    return position;
  }

}
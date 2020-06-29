/*
 * Copyright 2011 The IEC61850bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.iec61850bean.integrationtests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaFloat32;
import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientEventListener;
import com.beanit.iec61850bean.ClientSap;
import com.beanit.iec61850bean.DataSet;
import com.beanit.iec61850bean.Fc;
import com.beanit.iec61850bean.FcModelNode;
import com.beanit.iec61850bean.Report;
import com.beanit.iec61850bean.SclParseException;
import com.beanit.iec61850bean.SclParser;
import com.beanit.iec61850bean.ServerEventListener;
import com.beanit.iec61850bean.ServerModel;
import com.beanit.iec61850bean.ServerSap;
import com.beanit.iec61850bean.ServiceError;
import com.beanit.iec61850bean.Urcb;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReportingTest implements ClientEventListener {

  private static final String PREEXISTING_DATASET_REFERENCE = "ied1lDevice1/LLN0$dataset1";
  private static final String CREATED_DATASET_REFERENCE = "ied1lDevice1/LLN0$datasetnew";
  private static final String CHANGING_SERVER_DA_REFERENCE_1 =
      "ied1lDevice1/MMXU1.W.phsA.cVal.mag.f";
  private static final int PORT = 54321;
  private static final String ICD_FILE = "src/test/resources/iec61850bean-sample01.icd";
  private static final String URCB1_REFERENCE = "ied1lDevice1/LLN0.urcb101";
  ClientAssociation clientAssociation;
  private ServerSap serverSap;
  private ServerModel serverModel;
  private ServerModel clientModel;
  private int reportCounter = 0;

  @BeforeEach
  public void startServerAndClient() throws SclParseException, IOException, ServiceError {
    startServer();
    startClient();
  }

  private void startClient() throws IOException, ServiceError {
    ClientSap clientSap = new ClientSap();
    this.clientAssociation =
        clientSap.associate(InetAddress.getByName("localhost"), PORT, "", this);
    this.clientModel = this.clientAssociation.retrieveModel();
  }

  private void startServer() throws SclParseException, IOException {

    serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);

    this.serverSap.startListening(
        new ServerEventListener() {

          @Override
          public List<ServiceError> write(List<BasicDataAttribute> arg0) {
            return null;
          }

          @Override
          public void serverStoppedListening(ServerSap arg0) {}
        });

    this.serverModel = this.serverSap.getModelCopy();
  }

  @Test
  public void reportingTest() throws ServiceError, IOException, InterruptedException {
    Urcb urcb = this.clientModel.getUrcb(URCB1_REFERENCE);
    assertNotNull(urcb);

    this.clientAssociation.getRcbValues(urcb);
    this.clientAssociation.reserveUrcb(urcb);
    this.clientAssociation.enableReporting(urcb);

    Thread.sleep(500);

    BdaFloat32 mag =
        (BdaFloat32) this.serverModel.findModelNode(CHANGING_SERVER_DA_REFERENCE_1, Fc.MX);
    assertNotNull(mag);
    assertEquals(0, this.reportCounter);

    mag.setFloat(3.0f);
    List<BasicDataAttribute> bdas = new ArrayList<>();
    bdas.add(mag);
    this.serverSap.setValues(bdas);

    Thread.sleep(500);

    assertEquals(1, this.reportCounter);
  }

  @Test
  public void reportingWithCreatedDataSetTest()
      throws ServiceError, IOException, InterruptedException {
    // BdaFloat32 clientMag =
    // (BdaFloat32)this.clientModel.findModelNode(CHANGING_SERVER_DA_REFERENCE, Fc.MX);

    FcModelNode clientMag =
        (FcModelNode) this.clientModel.findModelNode(CHANGING_SERVER_DA_REFERENCE_1, Fc.MX);
    assertNotNull(clientMag);
    List<FcModelNode> dataSetMembers = new ArrayList<>();
    dataSetMembers.add(clientMag);

    DataSet dataSet = new DataSet(CREATED_DATASET_REFERENCE, dataSetMembers);
    this.clientAssociation.createDataSet(dataSet);

    Urcb urcb = this.clientModel.getUrcb(URCB1_REFERENCE);
    assertNotNull(urcb);

    this.clientAssociation.getRcbValues(urcb);

    assertEquals(PREEXISTING_DATASET_REFERENCE, urcb.getDatSet().getStringValue());

    System.out.println("dataset: " + urcb.getDatSet().getStringValue());

    this.clientAssociation.reserveUrcb(urcb);

    urcb.getDatSet().setValue(CREATED_DATASET_REFERENCE);
    List<ServiceError> serviceErrors =
        this.clientAssociation.setRcbValues(
            urcb, false, true, false, false, false, false, false, false);

    assertNull(serviceErrors.get(0));

    this.clientAssociation.getRcbValues(urcb);

    assertEquals(CREATED_DATASET_REFERENCE, urcb.getDatSet().getStringValue());

    this.clientAssociation.enableReporting(urcb);

    Thread.sleep(500);

    BdaFloat32 mag =
        (BdaFloat32) this.serverModel.findModelNode(CHANGING_SERVER_DA_REFERENCE_1, Fc.MX);
    assertNotNull(mag);
    assertEquals(0, this.reportCounter);

    mag.setFloat(3.0f);
    List<BasicDataAttribute> bdas = new ArrayList<>();
    bdas.add(mag);
    this.serverSap.setValues(bdas);

    Thread.sleep(1_000);

    assertEquals(1, this.reportCounter);
  }

  @AfterEach
  public void disconnectAndStopServer() throws Exception {
    if (this.serverSap != null) {
      this.serverSap.stop();
    }
  }

  @Override
  public void associationClosed(IOException arg0) {}

  @Override
  public void newReport(Report arg0) {
    System.out.println("got a report.");
    synchronized (this) {
      this.reportCounter++;
    }
  }
}

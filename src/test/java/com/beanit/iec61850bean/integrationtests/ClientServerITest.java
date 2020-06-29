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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaBoolean;
import com.beanit.iec61850bean.BdaFloat32;
import com.beanit.iec61850bean.BdaInt32;
import com.beanit.iec61850bean.BdaInt32U;
import com.beanit.iec61850bean.BdaQuality;
import com.beanit.iec61850bean.BdaQuality.Validity;
import com.beanit.iec61850bean.BdaReasonForInclusion;
import com.beanit.iec61850bean.BdaTimestamp;
import com.beanit.iec61850bean.BdaVisibleString;
import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientEventListener;
import com.beanit.iec61850bean.ClientSap;
import com.beanit.iec61850bean.DataSet;
import com.beanit.iec61850bean.Fc;
import com.beanit.iec61850bean.FcModelNode;
import com.beanit.iec61850bean.ModelNode;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ClientServerITest extends Thread implements ServerEventListener, ClientEventListener {

  private static final long MEGABYTE = 1024L * 1024L;
  // Get the Java runtime
  public static Runtime runtime = Runtime.getRuntime();
  private static int numReports = 0;
  private static int numSuccess = 0;
  private static int numAssociationClosed = 0;
  int port = 54321;
  String host = "127.0.0.1";
  ClientSap clientSap = new ClientSap();
  ServerSap serverSap = null;
  ClientAssociation clientAssociation = null;
  ClientAssociation clientAssociation2 = null;
  ServerModel serversServerModel = null;

  private static void getDataRecursive(ModelNode modelNode, ClientAssociation clientAssociation)
      throws ServiceError, IOException {
    if (modelNode.getChildren() == null) {
      return;
    }
    for (ModelNode childNode : modelNode) {
      FcModelNode fcChildNode = (FcModelNode) childNode;
      if (fcChildNode.getFc() != Fc.CO) {
        System.out.println("calling GetDataValues(" + childNode.getReference() + ")");
        clientAssociation.getDataValues(fcChildNode);
      }
      // clientAssociation.setDataValues(fcChildNode);
      getDataRecursive(childNode, clientAssociation);
    }
  }

  public static int findArray(Byte[] array, Byte[] subArray) {
    return Collections.indexOfSubList(Arrays.asList(array), Arrays.asList(subArray));
  }

  public static String getByteArrayString(byte[] byteArray) {
    StringBuilder builder = new StringBuilder();
    int l = 1;
    for (byte b : byteArray) {
      if ((l != 1) && ((l - 1) % 8 == 0)) {
        builder.append(' ');
      }
      if ((l != 1) && ((l - 1) % 16 == 0)) {
        builder.append('\n');
      }
      l++;
      builder.append("0x");
      String hexString = Integer.toHexString(b & 0xff);
      if (hexString.length() == 1) {
        builder.append(0);
      }
      builder.append(hexString + " ");
    }
    return builder.toString();
  }

  public static long bytesToMegabytes(long bytes) {
    return bytes / MEGABYTE;
  }

  @Test
  public void testClientServerCom()
      throws IOException, ServiceError, javax.naming.ConfigurationException, SclParseException,
          InterruptedException {

    clientSap.setTSelRemote(new byte[] {0, 1});
    clientSap.setTSelLocal(new byte[] {0, 0});
    clientSap.setApTitleCalled(new int[] {1, 1, 999, 1});
    BdaTimestamp timestamp;
    BdaVisibleString namePlateVendor;

    // ---------------------------------------------------
    // ------------------1st test server------------------
    runServer("src/test/resources/iec61850bean-sample01.icd", port);
    System.out.println("IED Server is running");

    // -----------------------------------------------------------
    // Client
    // -----------------------------------------------------------

    System.out.println("Attempting to connect to server " + host + " on port " + port);

    clientAssociation = clientSap.associate(InetAddress.getByName(host), port, null, this);

    // ServerModel serverModel = clientAssociation.retrieveModel();
    ServerModel serverModel =
        SclParser.parse("src/test/resources/iec61850bean-sample01.icd").get(0);
    clientAssociation.setServerModel(serverModel);

    getAllBdas(serverModel);

    timestamp = (BdaTimestamp) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);
    clientAssociation.getDataValues(timestamp);

    namePlateVendor =
        (BdaVisibleString) serverModel.findModelNode("ied1lDevice1/LLN0.NamPlt.vendor", Fc.DC);
    namePlateVendor.setValue("beanit".getBytes(UTF_8));
    clientAssociation.setDataValues(namePlateVendor);
    namePlateVendor.setDefault();
    clientAssociation.getDataValues(namePlateVendor);

    assertEquals("beanit", new String(namePlateVendor.getValue(), UTF_8));

    // -------------Test DataSets-Start---------------------

    Collection<DataSet> dataSets = serverModel.getDataSets();
    assertEquals(2, dataSets.size());

    DataSet dataSet1 = serverModel.getDataSet("ied1lDevice1/LLN0.dataset1");

    assertEquals("ied1lDevice1/LLN0.dataset1", dataSet1.getReferenceStr());

    DataSet dataSet2 = serverModel.getDataSet("ied1lDevice1/LLN0.dataset2");

    assertEquals("ied1lDevice1/LLN0.dataset2", dataSet2.getReferenceStr());

    for (FcModelNode dataSet2Member : dataSet2) {
      ((BdaInt32) dataSet2Member).setValue(9);
    }

    clientAssociation.setDataSetValues(dataSet2);

    dataSet1 = serverModel.getDataSet(dataSet1.getReferenceStr());

    clientAssociation.getDataSetValues(dataSet1);

    List<FcModelNode> nonPersistentDataSetMembers = new ArrayList<>(2);
    nonPersistentDataSetMembers.add(namePlateVendor);
    nonPersistentDataSetMembers.add(timestamp);

    DataSet nonPersistentDataSet =
        new DataSet("@nonPersistentDataSet", nonPersistentDataSetMembers);

    clientAssociation.createDataSet(nonPersistentDataSet);

    namePlateVendor.setDefault();

    List<ServiceError> serviceErrors = clientAssociation.getDataSetValues(nonPersistentDataSet);

    for (ServiceError serviceError : serviceErrors) {
      assertNull(serviceError);
    }

    assertTrue("beanit".equals(new String(namePlateVendor.getValue(), UTF_8)));

    clientAssociation.deleteDataSet(nonPersistentDataSet);

    serviceErrors = clientAssociation.getDataSetValues(nonPersistentDataSet);

    assertEquals(2, serviceErrors.size());

    for (ServiceError serviceError : serviceErrors) {
      assertNotNull(serviceError);
    }

    // -------------Test DataSets-End---------------------

    serverModel.findModelNode("ied1lDevice1/MMXU1.TotW", Fc.MX);

    BdaFloat32 f =
        (BdaFloat32) serverModel.findModelNode("ied1lDevice1/MMXU1.W.phsA.cVal.mag.f", Fc.MX);

    f.setFloat(3.0f);

    assertEquals(f.getFloat(), 3.0f, 0.00001);

    clientAssociation.getDataSetValues(dataSet1);

    assertEquals(f.getFloat(), 0.0f, 0.00001);

    /* Test selecting a controllable Data Object */

    BdaInt32U sboTimeout =
        (BdaInt32U) serverModel.findModelNode("ied1lDevice1/CSWI1.PosA.sboTimeout", Fc.CF);
    sboTimeout.setValue(2000l);

    BdaVisibleString sbo =
        (BdaVisibleString) serverModel.findModelNode("ied1lDevice1/CSWI1.PosA.SBO", Fc.CO);

    assertNotNull(sbo);

    clientAssociation.getDataValues(sbo);

    assertTrue(sbo.getStringValue().equals("success"));

    clientAssociation.getDataValues(sbo);

    assertEquals(sbo.getStringValue(), "success");

    /* select with second connection */

    clientAssociation2 = clientSap.associate(InetAddress.getByName(host), port, null, this);
    ServerModel serverModel2 = clientAssociation2.retrieveModel();

    BdaVisibleString sbo2 =
        (BdaVisibleString) serverModel2.findModelNode("ied1lDevice1/CSWI1.PosA.SBO", Fc.CO);

    clientAssociation2.getDataValues(sbo2);
    assertEquals(sbo2.getStringValue(), "");

    /* select with second connection after the sboTimeout of 1000ms should have been run out */

    // clientAssociation.close();

    Thread.sleep(1000);

    clientAssociation2.getDataValues(sbo2);
    assertEquals(sbo2.getStringValue(), "success");

    /* select with first connnection after the second was quit */

    clientAssociation.getDataValues(sbo);
    assertEquals(sbo.getStringValue(), "");

    clientAssociation2.close();

    Thread.sleep(100);

    clientAssociation.getDataValues(sbo);
    assertEquals(sbo.getStringValue(), "success");

    FcModelNode switchPosAFcDo =
        (FcModelNode) serverModel.findModelNode("ied1lDevice1/CSWI1.PosA", Fc.CO);

    assertNotNull(switchPosAFcDo);

    clientAssociation.operate(switchPosAFcDo);

    // -------------Test Reporting-Start-------------------

    Urcb urcb1 = serverModel.getUrcb("ied1lDevice1/LLN0.urcb101");
    assertNotNull(urcb1);

    Urcb urcb2 = serverModel.getUrcb("ied1lDevice1/LLN0.urcb2");
    assertNotNull(urcb2);

    clientAssociation.getRcbValues(urcb1);
    clientAssociation.getRcbValues(urcb2);

    BdaBoolean resv = (BdaBoolean) urcb1.getChild("Resv");
    assertNotNull(resv);
    clientAssociation.getDataValues(resv);
    assertFalse(resv.getValue());
    clientAssociation.reserveUrcb(urcb1);
    clientAssociation.getDataValues(resv);
    assertTrue(resv.getValue());

    assertEquals("urcb1", urcb1.getRptId().getStringValue());
    assertEquals("ied1lDevice1/LLN0.urcb2", urcb2.getRptId().getStringValue());

    assertEquals("ied1lDevice1/LLN0$dataset1", urcb1.getDatSet().getStringValue());
    assertEquals("ied1lDevice1/LLN0$dataset1", urcb2.getDatSet().getStringValue());

    assertEquals(true, urcb1.getOptFlds().isDataSetName());
    assertEquals(false, urcb1.getOptFlds().isBufferOverflow());
    assertEquals(false, urcb1.getOptFlds().isConfigRevision());
    assertEquals(false, urcb1.getOptFlds().isDataReference());
    assertEquals(false, urcb1.getOptFlds().isEntryId());
    assertEquals(true, urcb1.getOptFlds().isReasonForInclusion());
    assertEquals(true, urcb1.getOptFlds().isReportTimestamp());
    assertEquals(false, urcb1.getOptFlds().isSegmentation());
    assertEquals(true, urcb1.getOptFlds().isSequenceNumber());

    assertEquals(false, urcb2.getOptFlds().isDataSetName());
    assertEquals(false, urcb2.getOptFlds().isBufferOverflow());
    assertEquals(false, urcb2.getOptFlds().isConfigRevision());
    assertEquals(false, urcb2.getOptFlds().isDataReference());
    assertEquals(false, urcb2.getOptFlds().isEntryId());
    assertEquals(false, urcb2.getOptFlds().isReasonForInclusion());
    assertEquals(false, urcb2.getOptFlds().isReportTimestamp());
    assertEquals(false, urcb2.getOptFlds().isSegmentation());
    assertEquals(false, urcb2.getOptFlds().isSequenceNumber());

    assertEquals(50L, urcb1.getBufTm().getValue());

    assertEquals(true, urcb1.getTrgOps().isDataChange());
    assertEquals(true, urcb1.getTrgOps().isDataUpdate());
    assertEquals(true, urcb1.getTrgOps().isGeneralInterrogation());
    assertEquals(false, urcb1.getTrgOps().isIntegrity());
    assertEquals(true, urcb1.getTrgOps().isQualityChange());

    assertEquals(false, urcb2.getTrgOps().isDataChange());
    assertEquals(false, urcb2.getTrgOps().isDataUpdate());
    assertEquals(true, urcb2.getTrgOps().isGeneralInterrogation());
    assertEquals(false, urcb2.getTrgOps().isIntegrity());
    assertEquals(false, urcb2.getTrgOps().isQualityChange());

    assertEquals(5000L, urcb1.getIntgPd().getValue());

    assertEquals(0, urcb1.getSqNum().getValue());

    assertEquals(0L, urcb1.getConfRev().getValue());

    urcb1.getRptId().setValue("myurcb1");
    urcb1.getTrgOps().setGeneralInterrogation(false);
    urcb1.getTrgOps().setDataUpdate(false);
    urcb1.getTrgOps().setDataChange(false);

    clientAssociation.setRcbValues(urcb1, true, true, true, true, true, true, true, true);

    urcb1.getRptId().setValue("fasdfsadf");

    clientAssociation.getRcbValues(urcb1);

    assertEquals("myurcb1", urcb1.getRptId().getStringValue());

    clientAssociation.reserveUrcb(urcb1);
    clientAssociation.cancelUrcbReservation(urcb1);
    clientAssociation.enableReporting(urcb1);

    boolean thrown = false;
    try {
      clientAssociation.startGi(urcb1);
    } catch (ServiceError e) {
      thrown = true;
    }
    assertTrue(thrown);

    urcb1.getTrgOps().setGeneralInterrogation(true);
    urcb1.getTrgOps().setDataChange(true);
    urcb1.getIntgPd().setValue(1000);

    serviceErrors =
        clientAssociation.setRcbValues(urcb1, false, false, false, false, true, true, false, false);

    assertNotNull(serviceErrors.get(0));
    clientAssociation.disableReporting(urcb1);

    serviceErrors =
        clientAssociation.setRcbValues(urcb1, false, false, false, false, true, true, false, false);
    assertNull(serviceErrors.get(0));

    clientAssociation.enableReporting(urcb1);

    clientAssociation.startGi(urcb1);

    Thread.sleep(4000);

    assertEquals(2, numReports);
    assertEquals(2, numSuccess);

    clientAssociation.disableReporting(urcb1);

    urcb1.getTrgOps().setIntegrity(true);
    serviceErrors =
        clientAssociation.setRcbValues(
            urcb1, false, false, false, false, true, false, false, false);
    assertNull(serviceErrors.get(0));

    clientAssociation.enableReporting(urcb1);
    Thread.sleep(6500);

    assertEquals(8, numReports);
    assertEquals(8, numSuccess);

    clientAssociation.disableReporting(urcb1);

    // -------------Test Reporting-End---------------------

    clientAssociation.disconnect();

    Thread.sleep(500);
    assertEquals(2, numAssociationClosed);

    serverSap.stop();
  }

  private void getAllBdas(ServerModel serverModel) throws ServiceError, IOException {

    for (ModelNode ld : serverModel) {
      for (ModelNode ln : ld) {
        getDataRecursive(ln, clientAssociation);
      }
    }
  }

  private void runServer(String sclFilePath, int port) throws SclParseException, IOException {

    serverSap = new ServerSap(port, 0, null, SclParser.parse(sclFilePath).get(0), null);
    serverSap.setPort(port);
    serverSap.startListening(this);
    serversServerModel = serverSap.getModelCopy();
    start();
  }

  @Override
  public void serverStoppedListening(ServerSap serverSap) {
    // TODO Auto-generated method stub
  }

  @Override
  public List<ServiceError> write(List<BasicDataAttribute> bdas) {
    System.out.println("DataSource: got write request");
    return null;
  }

  @Override
  public void run() {

    BdaFloat32 totWMag =
        (BdaFloat32) serversServerModel.findModelNode("ied1lDevice1/MMXU1.TotW.mag.f", Fc.MX);
    BdaQuality q =
        (BdaQuality) serversServerModel.findModelNode("ied1lDevice1/MMXU1.TotW.q", Fc.MX);
    BdaTimestamp t =
        (BdaTimestamp) serversServerModel.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);

    List<BasicDataAttribute> totWBdas = new ArrayList<>(3);
    totWBdas.add(totWMag);
    totWBdas.add(q);
    totWBdas.add(t);

    double totWMagVal = 0.0f;
    q.setValidity(BdaQuality.Validity.GOOD);

    // for (int i = 0; i < 500000; i++) {

    totWMagVal += 1.0;

    System.out.println("setting totWmag to: " + totWMagVal);
    totWMag.setFloat((float) totWMagVal);
    t.setCurrentTime();

    if (q.getValidity() == Validity.GOOD) {
      q.setValidity(BdaQuality.Validity.INVALID);
    } else {
      q.setValidity(BdaQuality.Validity.GOOD);
    }

    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
      // is not interrupted
    }

    serverSap.setValues(totWBdas);

    // // Run the garbage collector
    // runtime.gc();
    // // Calculate the used memory
    // long memory = runtime.totalMemory() - runtime.freeMemory();
    // System.out.println("Used memory is bytes: " + memory);
    // System.out.println("Used memory is megabytes: " + bytesToMegabytes(memory));

    // try {
    // Thread.sleep(2000);
    // } catch (InterruptedException e) {
    // }
    // }

  }

  @Override
  public void newReport(Report report) {
    System.out.println("got report!");
    numReports++;

    if (numReports == 1) {
      List<BdaReasonForInclusion> reasons = report.getReasonCodes();
      assertEquals(2, reasons.size());
      assertTrue(reasons.get(0).isGeneralInterrogation());
      assertFalse(reasons.get(0).isDataUpdate());
    } else if (numReports == 2) {
      List<BdaReasonForInclusion> reasons = report.getReasonCodes();
      assertEquals(1, reasons.size());
      assertFalse(reasons.get(0).isGeneralInterrogation());
      assertTrue(reasons.get(0).isDataChange());
    } else if (numReports >= 3) {
      List<BdaReasonForInclusion> reasons = report.getReasonCodes();
      assertEquals(2, reasons.size());
      assertTrue(reasons.get(0).isIntegrity());
      assertTrue(reasons.get(1).isIntegrity());
    }

    numSuccess++;
  }

  @Override
  public void associationClosed(IOException e) {
    System.out.println("Association closed!");
    if (e != null) {
      System.out.println(e.getMessage());
    }
    numAssociationClosed++;
  }
}

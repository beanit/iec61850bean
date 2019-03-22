/*
 * Copyright 2011 The OpenIEC61850 Authors
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
package com.beanit.openiec61850.integrationtests;

import com.beanit.openiec61850.BasicDataAttribute;
import com.beanit.openiec61850.BdaBoolean;
import com.beanit.openiec61850.BdaFloat32;
import com.beanit.openiec61850.BdaInt32;
import com.beanit.openiec61850.BdaInt32U;
import com.beanit.openiec61850.BdaQuality;
import com.beanit.openiec61850.BdaQuality.Validity;
import com.beanit.openiec61850.BdaReasonForInclusion;
import com.beanit.openiec61850.BdaTimestamp;
import com.beanit.openiec61850.BdaVisibleString;
import com.beanit.openiec61850.ClientAssociation;
import com.beanit.openiec61850.ClientEventListener;
import com.beanit.openiec61850.ClientSap;
import com.beanit.openiec61850.DataSet;
import com.beanit.openiec61850.Fc;
import com.beanit.openiec61850.FcModelNode;
import com.beanit.openiec61850.ModelNode;
import com.beanit.openiec61850.Report;
import com.beanit.openiec61850.SclParseException;
import com.beanit.openiec61850.SclParser;
import com.beanit.openiec61850.ServerEventListener;
import com.beanit.openiec61850.ServerModel;
import com.beanit.openiec61850.ServerSap;
import com.beanit.openiec61850.ServiceError;
import com.beanit.openiec61850.Urcb;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

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
    runServer("src/test/resources/openiec61850sample01.icd", port);
    System.out.println("IED Server is running");

    // -----------------------------------------------------------
    // Client
    // -----------------------------------------------------------

    System.out.println("Attempting to connect to server " + host + " on port " + port);

    clientAssociation = clientSap.associate(InetAddress.getByName(host), port, null, this);

    // ServerModel serverModel = clientAssociation.retrieveModel();
    ServerModel serverModel = SclParser.parse("src/test/resources/openiec61850sample01.icd").get(0);
    clientAssociation.setServerModel(serverModel);

    getAllBdas(serverModel);

    timestamp = (BdaTimestamp) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);
    clientAssociation.getDataValues(timestamp);

    namePlateVendor =
        (BdaVisibleString) serverModel.findModelNode("ied1lDevice1/LLN0.NamPlt.vendor", Fc.DC);
    namePlateVendor.setValue("Fraunhofer ISE".getBytes());
    clientAssociation.setDataValues(namePlateVendor);
    namePlateVendor.setDefault();
    clientAssociation.getDataValues(namePlateVendor);

    Assert.assertEquals("Fraunhofer ISE", new String(namePlateVendor.getValue()));

    // -------------Test DataSets-Start---------------------

    Collection<DataSet> dataSets = serverModel.getDataSets();
    Assert.assertEquals(2, dataSets.size());

    DataSet dataSet1 = serverModel.getDataSet("ied1lDevice1/LLN0.dataset1");

    Assert.assertEquals("ied1lDevice1/LLN0.dataset1", dataSet1.getReferenceStr());

    DataSet dataSet2 = serverModel.getDataSet("ied1lDevice1/LLN0.dataset2");

    Assert.assertEquals("ied1lDevice1/LLN0.dataset2", dataSet2.getReferenceStr());

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
      Assert.assertNull(serviceError);
    }

    Assert.assertTrue("Fraunhofer ISE".equals(new String(namePlateVendor.getValue())));

    clientAssociation.deleteDataSet(nonPersistentDataSet);

    serviceErrors = clientAssociation.getDataSetValues(nonPersistentDataSet);

    Assert.assertEquals(2, serviceErrors.size());

    for (ServiceError serviceError : serviceErrors) {
      Assert.assertNotNull(serviceError);
    }

    // -------------Test DataSets-End---------------------

    serverModel.findModelNode("ied1lDevice1/MMXU1.TotW", Fc.MX);

    BdaFloat32 f =
        (BdaFloat32) serverModel.findModelNode("ied1lDevice1/MMXU1.W.phsA.cVal.mag.f", Fc.MX);

    f.setFloat(3.0f);

    Assert.assertEquals(f.getFloat(), 3.0f, 0.00001);

    clientAssociation.getDataSetValues(dataSet1);

    Assert.assertEquals(f.getFloat(), 0.0f, 0.00001);

    /* Test selecting a controllable Data Object */

    BdaInt32U sboTimeout =
        (BdaInt32U) serverModel.findModelNode("ied1lDevice1/CSWI1.PosA.sboTimeout", Fc.CF);
    sboTimeout.setValue(2000l);

    BdaVisibleString sbo =
        (BdaVisibleString) serverModel.findModelNode("ied1lDevice1/CSWI1.PosA.SBO", Fc.CO);

    Assert.assertNotNull(sbo);

    clientAssociation.getDataValues(sbo);

    Assert.assertTrue(sbo.getStringValue().equals("success"));

    clientAssociation.getDataValues(sbo);

    Assert.assertEquals(sbo.getStringValue(), "success");

    /* select with second connection */

    clientAssociation2 = clientSap.associate(InetAddress.getByName(host), port, null, this);
    ServerModel serverModel2 = clientAssociation2.retrieveModel();

    BdaVisibleString sbo2 =
        (BdaVisibleString) serverModel2.findModelNode("ied1lDevice1/CSWI1.PosA.SBO", Fc.CO);

    clientAssociation2.getDataValues(sbo2);
    Assert.assertEquals(sbo2.getStringValue(), "");

    /* select with second connection after the sboTimeout of 1000ms should have been run out */

    // clientAssociation.close();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    clientAssociation2.getDataValues(sbo2);
    Assert.assertEquals(sbo2.getStringValue(), "success");

    /* select with first connnection after the second was quit */

    clientAssociation.getDataValues(sbo);
    Assert.assertEquals(sbo.getStringValue(), "");

    clientAssociation2.close();

    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    clientAssociation.getDataValues(sbo);
    Assert.assertEquals(sbo.getStringValue(), "success");

    FcModelNode switchPosAFcDo =
        (FcModelNode) serverModel.findModelNode("ied1lDevice1/CSWI1.PosA", Fc.CO);

    Assert.assertNotNull(switchPosAFcDo);

    clientAssociation.operate(switchPosAFcDo);

    // -------------Test Reporting-Start-------------------

    Urcb urcb1 = serverModel.getUrcb("ied1lDevice1/LLN0.urcb101");
    Assert.assertNotNull(urcb1);

    Urcb urcb2 = serverModel.getUrcb("ied1lDevice1/LLN0.urcb2");
    Assert.assertNotNull(urcb2);

    clientAssociation.getRcbValues(urcb1);
    clientAssociation.getRcbValues(urcb2);

    BdaBoolean resv = (BdaBoolean) urcb1.getChild("Resv");
    Assert.assertNotNull(resv);
    clientAssociation.getDataValues(resv);
    Assert.assertFalse(resv.getValue());
    clientAssociation.reserveUrcb(urcb1);
    clientAssociation.getDataValues(resv);
    Assert.assertTrue(resv.getValue());

    Assert.assertEquals("urcb1", urcb1.getRptId().getStringValue());
    Assert.assertEquals("ied1lDevice1/LLN0.urcb2", urcb2.getRptId().getStringValue());

    Assert.assertEquals("ied1lDevice1/LLN0$dataset1", urcb1.getDatSet().getStringValue());
    Assert.assertEquals("ied1lDevice1/LLN0$dataset1", urcb2.getDatSet().getStringValue());

    Assert.assertEquals(true, urcb1.getOptFlds().isDataSetName());
    Assert.assertEquals(false, urcb1.getOptFlds().isBufferOverflow());
    Assert.assertEquals(false, urcb1.getOptFlds().isConfigRevision());
    Assert.assertEquals(false, urcb1.getOptFlds().isDataReference());
    Assert.assertEquals(false, urcb1.getOptFlds().isEntryId());
    Assert.assertEquals(true, urcb1.getOptFlds().isReasonForInclusion());
    Assert.assertEquals(true, urcb1.getOptFlds().isReportTimestamp());
    Assert.assertEquals(false, urcb1.getOptFlds().isSegmentation());
    Assert.assertEquals(true, urcb1.getOptFlds().isSequenceNumber());

    Assert.assertEquals(false, urcb2.getOptFlds().isDataSetName());
    Assert.assertEquals(false, urcb2.getOptFlds().isBufferOverflow());
    Assert.assertEquals(false, urcb2.getOptFlds().isConfigRevision());
    Assert.assertEquals(false, urcb2.getOptFlds().isDataReference());
    Assert.assertEquals(false, urcb2.getOptFlds().isEntryId());
    Assert.assertEquals(false, urcb2.getOptFlds().isReasonForInclusion());
    Assert.assertEquals(false, urcb2.getOptFlds().isReportTimestamp());
    Assert.assertEquals(false, urcb2.getOptFlds().isSegmentation());
    Assert.assertEquals(false, urcb2.getOptFlds().isSequenceNumber());

    Assert.assertEquals(50L, urcb1.getBufTm().getValue());

    Assert.assertEquals(true, urcb1.getTrgOps().isDataChange());
    Assert.assertEquals(true, urcb1.getTrgOps().isDataUpdate());
    Assert.assertEquals(true, urcb1.getTrgOps().isGeneralInterrogation());
    Assert.assertEquals(false, urcb1.getTrgOps().isIntegrity());
    Assert.assertEquals(true, urcb1.getTrgOps().isQualityChange());

    Assert.assertEquals(false, urcb2.getTrgOps().isDataChange());
    Assert.assertEquals(false, urcb2.getTrgOps().isDataUpdate());
    Assert.assertEquals(true, urcb2.getTrgOps().isGeneralInterrogation());
    Assert.assertEquals(false, urcb2.getTrgOps().isIntegrity());
    Assert.assertEquals(false, urcb2.getTrgOps().isQualityChange());

    Assert.assertEquals(5000L, urcb1.getIntgPd().getValue());

    Assert.assertEquals(0, urcb1.getSqNum().getValue());

    Assert.assertEquals(0L, urcb1.getConfRev().getValue());

    urcb1.getRptId().setValue("myurcb1");
    urcb1.getTrgOps().setGeneralInterrogation(false);
    urcb1.getTrgOps().setDataUpdate(false);
    urcb1.getTrgOps().setDataChange(false);

    clientAssociation.setRcbValues(urcb1, true, true, true, true, true, true, true, true);

    urcb1.getRptId().setValue("fasdfsadf");

    clientAssociation.getRcbValues(urcb1);

    Assert.assertEquals("myurcb1", urcb1.getRptId().getStringValue());

    clientAssociation.reserveUrcb(urcb1);
    clientAssociation.cancelUrcbReservation(urcb1);
    clientAssociation.enableReporting(urcb1);

    boolean thrown = false;
    try {
      clientAssociation.startGi(urcb1);
    } catch (ServiceError e) {
      thrown = true;
    }
    Assert.assertTrue(thrown);

    urcb1.getTrgOps().setGeneralInterrogation(true);
    urcb1.getTrgOps().setDataChange(true);
    urcb1.getIntgPd().setValue(1000);

    serviceErrors =
        clientAssociation.setRcbValues(urcb1, false, false, false, false, true, true, false, false);

    Assert.assertNotNull(serviceErrors.get(0));
    clientAssociation.disableReporting(urcb1);

    serviceErrors =
        clientAssociation.setRcbValues(urcb1, false, false, false, false, true, true, false, false);
    Assert.assertNull(serviceErrors.get(0));

    clientAssociation.enableReporting(urcb1);

    clientAssociation.startGi(urcb1);

    Thread.sleep(4000);

    Assert.assertEquals(2, numReports);
    Assert.assertEquals(2, numSuccess);

    clientAssociation.disableReporting(urcb1);

    urcb1.getTrgOps().setIntegrity(true);
    serviceErrors =
        clientAssociation.setRcbValues(
            urcb1, false, false, false, false, true, false, false, false);
    Assert.assertNull(serviceErrors.get(0));

    clientAssociation.enableReporting(urcb1);
    Thread.sleep(6500);

    Assert.assertEquals(8, numReports);
    Assert.assertEquals(8, numSuccess);

    clientAssociation.disableReporting(urcb1);

    // -------------Test Reporting-End---------------------

    clientAssociation.disconnect();

    Thread.sleep(500);
    Assert.assertEquals(2, numAssociationClosed);

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
  public void serverStoppedListening(ServerSap serverSAP) {
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

    float totWMagVal = 0.0f;
    q.setValidity(BdaQuality.Validity.GOOD);

    // for (int i = 0; i < 500000; i++) {

    totWMagVal += 1.0;

    System.out.println("setting totWmag to: " + totWMagVal);
    totWMag.setFloat(totWMagVal);
    t.setCurrentTime();

    if (q.getValidity() == Validity.GOOD) {
      q.setValidity(BdaQuality.Validity.INVALID);
    } else {
      q.setValidity(BdaQuality.Validity.GOOD);
    }

    try {
      Thread.sleep(4000);
    } catch (InterruptedException e) {
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
      Assert.assertEquals(2, reasons.size());
      Assert.assertTrue(reasons.get(0).isGeneralInterrogation());
      Assert.assertFalse(reasons.get(0).isDataUpdate());
    } else if (numReports == 2) {
      List<BdaReasonForInclusion> reasons = report.getReasonCodes();
      Assert.assertEquals(1, reasons.size());
      Assert.assertFalse(reasons.get(0).isGeneralInterrogation());
      Assert.assertTrue(reasons.get(0).isDataChange());
    } else if (numReports >= 3) {
      List<BdaReasonForInclusion> reasons = report.getReasonCodes();
      Assert.assertEquals(2, reasons.size());
      Assert.assertTrue(reasons.get(0).isIntegrity());
      Assert.assertTrue(reasons.get(1).isIntegrity());
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

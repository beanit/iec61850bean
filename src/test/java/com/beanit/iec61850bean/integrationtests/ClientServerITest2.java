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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaReasonForInclusion;
import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ClientEventListener;
import com.beanit.iec61850bean.ClientSap;
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
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ClientServerITest2 extends Thread implements ServerEventListener, ClientEventListener {

  private static final long MEGABYTE = 1024L * 1024L;
  // Get the Java runtime
  public static Runtime runtime = Runtime.getRuntime();
  private static int numReports = 0;
  int port = 54322;
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

    runServer("src/test/resources/testModel2.icd", port);
    System.out.println("IED Server is running");

    // -----------------------------------------------------------
    // Client
    // -----------------------------------------------------------

    System.out.println("Attempting to connect to server " + host + " on port " + port);

    clientAssociation = clientSap.associate(InetAddress.getByName(host), port, null, this);

    ServerModel serverModel = SclParser.parse("src/test/resources/testModel2.icd").get(0);
    clientAssociation.setServerModel(serverModel);

    getAllBdas(serverModel);

    // timestamp = (BdaTimestamp) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW.t", Fc.MX);
    // clientAssociation.getDataValues(timestamp);

    clientAssociation.disconnect();

    Thread.sleep(500);
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

    // BdaFloat32 totWMag = (BdaFloat32)
    // serversServerModel.findModelNode("ied1lDevice1/MMXU1.TotW.mag.f", Fc.MX);
    // BdaQuality q = (BdaQuality) serversServerModel.findModelNode("ied1lDevice1/MMXU1.TotW.q",
    // Fc.MX);
    // BdaTimestamp t = (BdaTimestamp) serversServerModel.findModelNode("ied1lDevice1/MMXU1.TotW.t",
    // Fc.MX);
    //
    // List<BasicDataAttribute> totWBdas = new ArrayList<BasicDataAttribute>(3);
    // totWBdas.add(totWMag);
    // totWBdas.add(q);
    // totWBdas.add(t);
    //
    // float totWMagVal = 0.0f;
    // q.setValidity(BdaQuality.Validity.GOOD);
    //
    // // for (int i = 0; i < 500000; i++) {
    //
    // totWMagVal += 1.0;
    //
    // logger.info("setting totWmag to: " + totWMagVal);
    // totWMag.setFloat(totWMagVal);
    // t.setCurrentTime();
    //
    // if (q.getValidity() == Validity.GOOD) {
    // q.setValidity(BdaQuality.Validity.INVALID);
    // }
    // else {
    // q.setValidity(BdaQuality.Validity.GOOD);
    // }
    //
    // try {
    // Thread.sleep(4000);
    // } catch (InterruptedException e) {
    // }
    // serverSap.setValues(totWBdas);

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
  }

  @Override
  public void associationClosed(IOException e) {
    System.out.println("Association closed!");
  }
}

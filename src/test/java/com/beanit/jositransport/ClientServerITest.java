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
package com.beanit.jositransport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

public class ClientServerITest {

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

  @Test
  public void testClientServerCom() throws IOException, TimeoutException {

    int port = 18982;

    ServerTSap serverTSAP = new ServerTSap(port, new SampleServer());

    serverTSAP.startListening();

    InetAddress address = InetAddress.getByName("127.0.0.1");

    ClientTSap tSAP = new ClientTSap();
    tSAP.setMaxTPDUSizeParam(7);
    TConnection tConnection;

    tConnection = tSAP.connectTo(address, port);

    byte[] testData = {
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
      (byte) 0x00,
      (byte) 0x01,
      (byte) 0x02,
      (byte) 0x03,
      (byte) 0x04,
      (byte) 0x05,
      (byte) 0x06,
      (byte) 0x07,
      (byte) 0x08,
      (byte) 0x09,
      (byte) 0x0a,
      (byte) 0x0b,
      (byte) 0x0c,
      (byte) 0x0d,
      (byte) 0x0e,
      (byte) 0x0f,
    };

    List<byte[]> testDataList = new ArrayList<>();
    testDataList.add(testData);
    testDataList.add(testData);

    List<Integer> offsetList = new ArrayList<>();
    offsetList.add(0);
    offsetList.add(0);

    List<Integer> lengthList = new ArrayList<>();
    lengthList.add(testData.length);
    lengthList.add(testData.length);

    tConnection.send(testDataList, offsetList, lengthList);

    tConnection.setMessageTimeout(1000);

    ByteBuffer pduBuffer = ByteBuffer.allocate(600);

    tConnection.receive(pduBuffer);

    byte[] subArrayOfReturnedData = new byte[pduBuffer.limit() - pduBuffer.position()];

    System.arraycopy(
        pduBuffer.array(),
        pduBuffer.arrayOffset() + pduBuffer.position(),
        subArrayOfReturnedData,
        0,
        subArrayOfReturnedData.length);

    assertTrue(Arrays.equals(subArrayOfReturnedData, concat(testData, testData)));

    tConnection.disconnect();

    serverTSAP.stopListening();
  }

  byte[] concat(byte[] A, byte[] B) {
    byte[] C = new byte[A.length + B.length];
    System.arraycopy(A, 0, C, 0, A.length);
    System.arraycopy(B, 0, C, A.length, B.length);

    return C;
  }

  public static class SampleServer implements TConnectionListener {

    @Override
    public void connectionIndication(TConnection tConnection) {

      ByteBuffer pduBuffer = ByteBuffer.allocate(600);

      try {
        tConnection.receive(pduBuffer);
      } catch (IOException e1) {
        System.err.println("Caught exception reading data:" + e1.getMessage());
        e1.printStackTrace();
        return;
      } catch (TimeoutException e) {
        System.err.println("Caught TimeoutException reading data:" + e.getMessage());
        e.printStackTrace();
      }

      try {
        tConnection.send(
            pduBuffer.array(), pduBuffer.position(), pduBuffer.limit() - pduBuffer.position());
      } catch (IOException e) {
        System.err.println("Caught exception writing data:");
        e.printStackTrace();
        return;
      }
    }

    @Override
    public void serverStoppedListeningIndication(IOException e) {
      System.out.println("Got an indication that the server stopped listening.");
    }
  }
}

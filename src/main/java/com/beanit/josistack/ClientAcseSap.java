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
package com.beanit.josistack;

import com.beanit.jositransport.ClientTSap;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import javax.net.SocketFactory;

/**
 * This class implements the Service Access Point (SAP) for the Application Control Service Element
 * (ACSE) protocol as defined by ISO 8650 or ITU X.217/X.227. The ACSE provides services for
 * establishing and releasing application-associations. The class also realizes the lower ISO
 * Presentation Layer as defined by ISO 8823/ITU X226 and the ISO Session Layer as defined by
 * 8327/ITU X.225.
 */
public final class ClientAcseSap {

  static final byte[] P_SEL_DEFAULT = {0, 0, 0, 1};
  static final byte[] S_SEL_DEFAULT = {0, 1};
  public ClientTSap tSap = null;
  public byte[] pSelRemote = P_SEL_DEFAULT;
  public byte[] pSelLocal = P_SEL_DEFAULT;
  public byte[] sSelRemote = S_SEL_DEFAULT;
  public byte[] sSelLocal = S_SEL_DEFAULT;

  private int[] apTitleCalled = new int[] {1, 1, 999, 1, 1};
  private int[] apTitleCalling = new int[] {1, 1, 999, 1};
  private int aeQualifierCalled = 12;
  private int aeQualifierCalling = 12;

  /**
   * Use this constructor to create a client ACSE Service Access Point (SAP) that will start
   * connections to remote ACSE SAPs. Once constructed the AcseSAP contains a public TSAP that can
   * be accessed to set its configuration.
   */
  public ClientAcseSap() {
    tSap = new ClientTSap();
  }

  public ClientAcseSap(SocketFactory socketFactory) {
    tSap = new ClientTSap(socketFactory);
  }

  public void setApTitleCalled(int[] title) {
    this.apTitleCalled = title;
  }

  public void setApTitleCalling(int[] title) {
    this.apTitleCalling = title;
  }

  public void setAeQualifierCalled(int qualifier) {
    this.aeQualifierCalled = qualifier;
  }

  public void setAeQualifierCalling(int qualifier) {
    this.aeQualifierCalling = qualifier;
  }

  /**
   * Associate to a remote ServerAcseSAP that is listening at the destination address.
   *
   * @param address remote InetAddress
   * @param port remote port
   * @param localAddr local InetAddress
   * @param localPort local port
   * @param authenticationParameter an authentication parameter
   * @param apdu the payload to send with the association request
   * @return the association object
   * @throws IOException if an error occurs connecting
   */
  public AcseAssociation associate(
      InetAddress address,
      int port,
      InetAddress localAddr,
      int localPort,
      String authenticationParameter,
      ByteBuffer apdu)
      throws IOException {
    AcseAssociation acseAssociation = new AcseAssociation(null, pSelLocal);
    try {
      acseAssociation.startAssociation(
          apdu,
          address,
          port,
          localAddr,
          localPort,
          authenticationParameter,
          sSelRemote,
          sSelLocal,
          pSelRemote,
          tSap,
          apTitleCalled,
          apTitleCalling,
          aeQualifierCalled,
          aeQualifierCalling);
    } catch (IOException e) {
      acseAssociation.disconnect();
      throw e;
    }
    return acseAssociation;
  }
}

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
package com.beanit.iec61850bean;

import com.beanit.josistack.ClientAcseSap;
import java.io.IOException;
import java.net.InetAddress;
import javax.net.SocketFactory;

/**
 * The <code>ClientSap</code> class represents the IEC 61850 service access point for client
 * applications. A client application that wants to connect to a server should first create an
 * instance of <code>ClientSap</code>. Next all the necessary configuration parameters can be set.
 * Finally the <code>associate</code> function is called to connect to the server. An instance of
 * <code>ClientSap</code> can be used to create an unlimited number of associations. Changing the
 * parameters of a ClientSap has no affect on associations that have already been created.
 */
public final class ClientSap {

  static final int MINIMUM_MMS_PDU_SIZE = 64;
  private static final int MAXIMUM_MMS_PDU_SIZE = 65000;

  private static final byte[] DEFAULT_TSEL_LOCAL = new byte[] {0, 0};
  private static final byte[] DEFAULT_TSEL_REMOTE = new byte[] {0, 1};
  private static final int DEFAUTL_TPDU_SIZE_PARAMETER = 10; // size = 1024
  private final int proposedMaxServOutstandingCalling = 5;
  private final int proposedMaxServOutstandingCalled = 5;
  private final int proposedDataStructureNestingLevel = 10;
  private final ClientAcseSap acseSap;
  private int proposedMaxMmsPduSize = 65000;
  private byte[] servicesSupportedCalling =
      new byte[] {(byte) 0xee, 0x1c, 0, 0, 0x04, 0x08, 0, 0, 0x79, (byte) 0xef, 0x18};
  private int messageFragmentTimeout = 10000;
  private int responseTimeout = 20000;

  /** Use this constructor to create a default client SAP. */
  public ClientSap() {
    acseSap = new ClientAcseSap();
    acseSap.tSap.tSelLocal = DEFAULT_TSEL_LOCAL;
    acseSap.tSap.tSelRemote = DEFAULT_TSEL_REMOTE;
    acseSap.tSap.setMaxTPDUSizeParam(DEFAUTL_TPDU_SIZE_PARAMETER);
  }

  /**
   * Use this constructor to create a client SAP that uses the given <code>SocketFactory</code> to
   * connect to servers. You could pass an SSLSocketFactory to enable SSL.
   *
   * @param socketFactory the socket factory to construct the socket
   */
  public ClientSap(SocketFactory socketFactory) {
    acseSap = new ClientAcseSap(socketFactory);
    acseSap.tSap.tSelLocal = DEFAULT_TSEL_LOCAL;
    acseSap.tSap.tSelRemote = DEFAULT_TSEL_REMOTE;
    acseSap.tSap.setMaxTPDUSizeParam(DEFAUTL_TPDU_SIZE_PARAMETER);
  }

  /**
   * Gets the maximum MMS PDU size.
   *
   * @return the maximum MMS PDU size.
   */
  public int getMaxMmsPduSize() {
    return proposedMaxMmsPduSize;
  }

  /**
   * Sets the maximum MMS PDU size in bytes that the client association will support. The client
   * proposes this value to the server during association. If the server requires the use of a
   * smaller maximum MMS PDU size, then the smaller size will be accepted by the client. The default
   * size is 65000.
   *
   * @param size cannot be less than 64. The upper limit is 65000 so that segmentation at the lower
   *     transport layer is avoided. The Transport Layer's maximum PDU size is 65531.
   */
  public void setMaxMmsPduSize(int size) {
    if (size >= MINIMUM_MMS_PDU_SIZE && size <= MAXIMUM_MMS_PDU_SIZE) {
      proposedMaxMmsPduSize = size;
    } else {
      throw new IllegalArgumentException("maximum size is out of bound");
    }
  }

  /**
   * Gets the ServicesSupportedCalling parameter.
   *
   * @return the ServicesSupportedCalling parameter.
   */
  public byte[] getServicesSupportedCalling() {
    return servicesSupportedCalling;
  }

  /**
   * Sets the SevicesSupportedCalling parameter. The given parameter is sent to the server but has
   * no affect on the functionality of this client.
   *
   * @param services the ServicesSupportedCalling parameter
   */
  public void setServicesSupportedCalling(byte[] services) {
    if (services.length != 11) {
      throw new IllegalArgumentException("The services parameter needs to be of lenth 11");
    }
    servicesSupportedCalling = services;
  }

  /**
   * Sets the remote/called Session-Selector (S-SEL). The default remote S-SEL is byte[] { 0, 1 }.
   *
   * @param sSelRemote the remote/called S-SEL.
   */
  public void setSSelRemote(byte[] sSelRemote) {
    acseSap.sSelRemote = sSelRemote;
  }

  /**
   * Sets the local/calling Session-Selector (S-SEL). The default local S-SEL is byte[] { 0, 1 }.
   *
   * @param sSelLocal the local/calling S-SEL.
   */
  public void setSSelLocal(byte[] sSelLocal) {
    acseSap.sSelLocal = sSelLocal;
  }

  /**
   * Sets the remote/called Presentation-Selector (P-SEL). The default remote P-SEL is byte[] { 0,
   * 0, 0, 1 }.
   *
   * @param pSelRemote the remote/called P-SEL.
   */
  public void setPSelRemote(byte[] pSelRemote) {
    acseSap.pSelRemote = pSelRemote;
  }

  /**
   * Sets the local/calling Presentation-Selector (P-SEL). The default local P-SEL is byte[] { 0, 0,
   * 0, 1 }.
   *
   * @param pSelLocal the local/calling P-SEL.
   */
  public void setPSelLocal(byte[] pSelLocal) {
    acseSap.pSelLocal = pSelLocal;
  }

  /**
   * Sets the remote/called Transport-Selector (T-SEL). It is optionally transmitted in the OSI
   * Transport Layer connection request (CR). The default remote T-SEL is byte[] { 0, 1 }.
   *
   * @param tSelRemote the remote/called T-SEL. If null the T-SEL will be omitted. No maximum size
   *     is defined by the standard.
   */
  public void setTSelRemote(byte[] tSelRemote) {
    acseSap.tSap.tSelRemote = tSelRemote;
  }

  /**
   * Sets the local/calling Transport-Selector (T-SEL). It is optionally transmitted in the OSI
   * Transport Layer connection request (CR). The default local T-SEL byte[] { 0, 0 }.
   *
   * @param tSelLocal the local/calling T-SEL. If null the T-SEL will be omitted. No maximum size is
   *     defined by the standard.
   */
  public void setTSelLocal(byte[] tSelLocal) {
    acseSap.tSap.tSelLocal = tSelLocal;
  }

  /**
   * Set the maxTPDUSize. The default maxTPduSize is 65531 (see RFC 1006).
   *
   * @param maxTPduSizeParam The maximum length is equal to 2^(maxTPduSizeParam) octets. Note that
   *     the actual TSDU size that can be transfered is equal to TPduSize-3. Default is 65531 octets
   *     (see RFC 1006), 7 &lt;= maxTPduSizeParam &lt;= 16, needs to be set before listening or
   *     connecting
   */
  public void setMaxTPduSizeParameter(int maxTPduSizeParam) {
    acseSap.tSap.setMaxTPDUSizeParam(maxTPduSizeParam);
  }

  /**
   * Sets the remote/called Application Process Title. The default value is int[] { 1, 1, 999, 1, 1
   * }
   *
   * @param title the remote/called AP title.
   */
  public void setApTitleCalled(int[] title) {
    acseSap.setApTitleCalled(title);
  }

  /**
   * Sets the local/calling Application Process Title. The default value is int[] { 1, 1, 999, 1 }
   *
   * @param title the local/calling AP title.
   */
  public void setApTitleCalling(int[] title) {
    acseSap.setApTitleCalling(title);
  }

  /**
   * Sets the remote/called Application Entity Qualifier. The default value is 12.
   *
   * @param qualifier the remote/called AE Qualifier
   */
  public void setAeQualifierCalled(int qualifier) {
    acseSap.setAeQualifierCalled(qualifier);
  }

  /**
   * Sets the local/calling Application Entity Qualifier. The default value is 12.
   *
   * @param qualifier the local/calling AE Qualifier
   */
  public void setAeQualifierCalling(int qualifier) {
    acseSap.setAeQualifierCalling(qualifier);
  }

  /**
   * Sets the default response timeout of the <code>ClientAssociation</code> that is created using
   * this ClientSap.
   *
   * @param timeout the response timeout in milliseconds. The default is 20000.
   */
  public void setResponseTimeout(int timeout) {
    responseTimeout = timeout;
  }

  /**
   * Sets the message fragment timeout. This is the timeout that the socket timeout is set to after
   * the first byte of a message has been received. A request function (e.g. setDataValues()) will
   * throw an IOException if the socket throws this timeout because the association/connection
   * cannot recover from this kind of error.
   *
   * @param timeout the timeout in milliseconds. The default is 10000.
   */
  public void setMessageFragmentTimeout(int timeout) {
    messageFragmentTimeout = timeout;
  }

  /**
   * Connects to the IEC 61850 MMS server at the given address and port and returns the resulting
   * association object.
   *
   * @param address the address to connect to.
   * @param port the port to connect to. Usually the MMS port is 102.
   * @param authenticationParameter an optional authentication parameters that is transmitted. It
   *     will be omitted if equal to null.
   * @param reportListener the listener to be notified of incoming reports
   * @return the association object
   * @throws IOException if any kind of error occurs trying build up the association
   */
  public ClientAssociation associate(
      InetAddress address,
      int port,
      String authenticationParameter,
      ClientEventListener reportListener)
      throws IOException {
    return associate(address, port, authenticationParameter, null, -1, reportListener);
  }

  /**
   * Connects to the IEC 61850 MMS server at the given address and port and returns the resulting
   * association object.
   *
   * @param address the address to connect to
   * @param port the port to connect to. Usually the MMS port is 102.
   * @param authenticationParameter an optional authentication parameters that is transmitted. It
   *     will be omitted if equal to null.
   * @param localAddr the local address to use
   * @param localPort the local port to use
   * @param reportListener the listener to be notified of incoming reports
   * @return the association object.
   * @throws IOException if any kind of error occurs trying build up the association
   */
  public ClientAssociation associate(
      InetAddress address,
      int port,
      String authenticationParameter,
      InetAddress localAddr,
      int localPort,
      ClientEventListener reportListener)
      throws IOException {

    if (port < 0 || port > 65535) {
      throw new IllegalArgumentException("invalid port");
    }

    if (address == null) {
      throw new IllegalArgumentException("address may not be null");
    }

    if (acseSap.sSelRemote == null) {
      throw new IllegalArgumentException("sSelRemote may not be null");
    }

    if (acseSap.sSelRemote.length != 2) {
      throw new IllegalArgumentException("sSelRemote lenght must be two");
    }

    if (acseSap.sSelLocal == null) {
      throw new IllegalArgumentException("sSelLocal may not be null");
    }

    if (acseSap.sSelLocal.length != 2) {
      throw new IllegalArgumentException("sSelLocal lenght must be two");
    }

    ClientAssociation clientAssociation =
        new ClientAssociation(
            address,
            port,
            localAddr,
            localPort,
            authenticationParameter,
            acseSap,
            proposedMaxMmsPduSize,
            proposedMaxServOutstandingCalling,
            proposedMaxServOutstandingCalled,
            proposedDataStructureNestingLevel,
            servicesSupportedCalling,
            responseTimeout,
            messageFragmentTimeout,
            reportListener);

    return clientAssociation;
  }
}

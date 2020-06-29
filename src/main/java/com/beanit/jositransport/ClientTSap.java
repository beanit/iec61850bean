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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import javax.net.SocketFactory;

/**
 * This class implements a client Transport Service Access Point (TSAP) over TCP/IP as defined in
 * RFC 1006, ISO 8072, and ISO 8073. It can be used to create TConnections that connect to remote
 * ServerTSAPs.
 */
public final class ClientTSap {

  public byte[] tSelRemote = null;
  public byte[] tSelLocal = null;
  private int maxTPDUSizeParam = 16;
  private SocketFactory socketFactory = null;
  private int messageTimeout = 0;
  private int messageFragmentTimeout = 60000;

  /** Use this constructor to create a client TSAP that will start connections to remote TSAPs. */
  public ClientTSap() {
    socketFactory = SocketFactory.getDefault();
  }

  /**
   * Use this constructor to create a client TSAP that will start connections to remote TSAPs. You
   * could pass an SSLSocketFactory to enable SSL.
   *
   * @param socketFactory the socket factory to create the underlying socket
   */
  public ClientTSap(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }

  /**
   * Calculates and returns the maximum TPDUSize. This is equal to 2^(maxTPDUSizeParam)
   *
   * @param maxTPDUSizeParam the size parameter
   * @return the maximum TPDU size
   */
  public static int getMaxTPDUSize(int maxTPDUSizeParam) {
    if (maxTPDUSizeParam < 7 || maxTPDUSizeParam > 16) {
      throw new IllegalArgumentException("maxTPDUSizeParam is out of bound");
    }
    if (maxTPDUSizeParam == 16) {
      return 65531;
    } else {
      return (int) Math.pow(2, maxTPDUSizeParam);
    }
  }

  /**
   * Set the TConnection timeout for waiting for the first byte of a new message. Default is 0
   * (unlimited)
   *
   * @param messageTimeout in milliseconds
   */
  public void setMessageTimeout(int messageTimeout) {
    this.messageTimeout = messageTimeout;
  }

  /**
   * Set the TConnection timeout for receiving data once the beginning of a message has been
   * received. Default is 60000 (60seconds)
   *
   * @param messageFragmentTimeout in milliseconds
   */
  public void setMessageFragmentTimeout(int messageFragmentTimeout) {
    this.messageFragmentTimeout = messageFragmentTimeout;
  }

  /**
   * Get the maximum TPDU size parameter to be used by this TSAP
   *
   * @return the maximum TPDU size parameter
   */
  public int getMaxTPDUSizeParam() {
    return maxTPDUSizeParam;
  }

  /**
   * Set the maxTPDUSize. The default maxTPduSize is 65531 (see RFC 1006).
   *
   * @param maxTPDUSizeParam The maximum length is equal to 2^(maxTPDUSizeParam) octets. Note that
   *     the actual TSDU size that can be transfered is equal to TPduSize-3. Default is 65531 octets
   *     (see RFC 1006), 7 &lt;= maxTPDUSizeParam &lt;= 16, needs to be set before listening or
   *     connecting
   */
  public void setMaxTPDUSizeParam(int maxTPDUSizeParam) {
    if (maxTPDUSizeParam < 7 || maxTPDUSizeParam > 16) {
      throw new IllegalArgumentException("maxTPDUSizeParam is out of bound");
    }
    this.maxTPDUSizeParam = maxTPDUSizeParam;
  }

  /**
   * Connect to a remote TSAP that is listening at the destination address.
   *
   * @param address remote IP
   * @param port remote port
   * @return the Connection Object
   * @throws IOException is thrown if connection was unsuccessful.
   */
  public TConnection connectTo(InetAddress address, int port) throws IOException {
    return connectTo(address, port, null, -1);
  }

  /**
   * Connect to a remote TSAP that is listening at the destination address.
   *
   * @param address remote IP
   * @param port remote port
   * @param localAddr local IP
   * @param localPort local port
   * @return the Connection Object
   * @throws IOException is thrown if connection was unsuccessful.
   */
  public TConnection connectTo(InetAddress address, int port, InetAddress localAddr, int localPort)
      throws IOException {
    Socket socket;

    if (localAddr == null) {
      socket = socketFactory.createSocket();

      socket.connect(new InetSocketAddress(address, port), messageTimeout);
    } else {
      socket = socketFactory.createSocket(address, port, localAddr, localPort);
    }
    TConnection tConnection =
        new TConnection(socket, maxTPDUSizeParam, messageTimeout, messageFragmentTimeout, null);
    tConnection.tSelRemote = tSelRemote;
    tConnection.tSelLocal = tSelLocal;
    tConnection.startConnection();

    return tConnection;
  }

  public void setSocketFactory(SocketFactory socketFactory) {
    this.socketFactory = socketFactory;
  }
}

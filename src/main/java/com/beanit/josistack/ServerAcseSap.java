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

import com.beanit.jositransport.ServerTSap;
import com.beanit.jositransport.TConnection;
import com.beanit.jositransport.TConnectionListener;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeoutException;
import javax.net.ServerSocketFactory;

/**
 * This class implements the server Service Access Point (SAP) for the Application Control Service
 * Element (ACSE) protocol as defined by ISO 8650 or ITU X.217/X.227. The ACSE provides services for
 * establishing and releasing application-associations. The class also realizes the lower ISO
 * Presentation Layer as defined by ISO 8823/ITU X226 and the ISO Session Layer as defined by
 * 8327/ITU X.225.
 */
public final class ServerAcseSap implements TConnectionListener {

  public ServerTSap serverTSap = null;
  public byte[] pSelLocal = ClientAcseSap.P_SEL_DEFAULT;
  private AcseAssociationListener associationListener = null;

  /**
   * Use this constructor to create a server ACSE SAP that listens on a fixed port.
   *
   * @param port the local port listen on
   * @param backlog the backlog
   * @param bindAddr the InetAddress to bind to
   * @param associationListener the AssociationListener that will be notified when remote clients
   *     have associated. Once constructed the AcseSAP contains a public TSAP that can be accessed
   *     to set its configuration.
   */
  public ServerAcseSap(
      int port, int backlog, InetAddress bindAddr, AcseAssociationListener associationListener) {
    this(port, backlog, bindAddr, associationListener, ServerSocketFactory.getDefault());
  }

  /**
   * Use this constructor to create a server ACSE SAP that listens on a fixed port. The server
   * socket is created with the given socket factory.
   *
   * @param port the local port listen on
   * @param backlog the backlog
   * @param bindAddr the InetAddress to bind to
   * @param associationListener the AssociationListener that will be notified when remote clients
   *     have associated. Once constructed the AcseSAP contains a public TSAP that can be accessed
   *     to set its configuration.
   * @param serverSocketFactory the server socket factory to create the socket
   */
  public ServerAcseSap(
      int port,
      int backlog,
      InetAddress bindAddr,
      AcseAssociationListener associationListener,
      ServerSocketFactory serverSocketFactory) {
    this.associationListener = associationListener;
    serverTSap = new ServerTSap(port, backlog, bindAddr, this, serverSocketFactory);
  }

  /**
   * Start listening for incoming connections. Only for server SAPs.
   *
   * @throws IOException if an error occures starting to listen
   */
  public void startListening() throws IOException {
    if (associationListener == null || serverTSap == null) {
      throw new IllegalStateException(
          "AcseSAP is unable to listen because it was not initialized.");
    }
    serverTSap.startListening();
  }

  public void stopListening() {
    serverTSap.stopListening();
  }

  /** This function is internal and should not be called by users of this class. */
  @Override
  public void serverStoppedListeningIndication(IOException e) {
    associationListener.serverStoppedListeningIndication(e);
  }

  /** This function is internal and should not be called by users of this class. */
  @Override
  public void connectionIndication(TConnection tConnection) {

    try {

      AcseAssociation acseAssociation = new AcseAssociation(tConnection, pSelLocal);

      ByteBuffer asdu = ByteBuffer.allocate(1000);
      try {
        asdu = acseAssociation.listenForCn(asdu);
      } catch (IOException e) {
        // Server: Connection unsuccessful.
        tConnection.close();
        return;
      } catch (TimeoutException e) {
        // Illegal state: Timeout should not occur here
        tConnection.close();
        return;
      }

      associationListener.connectionIndication(acseAssociation, asdu);

    } catch (Exception e) {
      // Association closed because of an unexpected exception.
      tConnection.close();
    }
  }
}

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

import com.beanit.josistack.AcseAssociation;
import com.beanit.josistack.ServerAcseSap;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import javax.net.ServerSocketFactory;

/**
 * The <code>ServerSap</code> class represents the IEC 61850 service access point for server
 * applications. It corresponds to the AccessPoint defined in the ICD/SCL file. A server application
 * that is to listen for client connections should first get an instance of <code>ServerSap</code>
 * using the static function ServerSap.getSapsFromSclFile(). Next all the necessary configuration
 * parameters can be set. Finally the <code>startListening</code> function is called to listen for
 * client associations. Changing properties of a ServerSap after starting to listen is not
 * recommended and has unknown effects.
 */
public final class ServerSap {

  static final int MINIMUM_MMS_PDU_SIZE = 64;
  private static final int MAXIMUM_MMS_PDU_SIZE = 65000;
  final ServerModel serverModel;
  final List<ServerAssociation> associations = new ArrayList<>();
  byte[] servicesSupportedCalled =
      new byte[] {(byte) 0xee, 0x1c, 0, 0, 0x04, 0x08, 0, 0, 0x79, (byte) 0xef, 0x18};
  byte[] cbbBitString = {(byte) 0xfb, 0x00};
  ServerEventListener serverEventListener;
  Timer timer;
  boolean listening = false;
  private int proposedMaxMmsPduSize = 65000;
  private int proposedMaxServOutstandingCalling = 5;
  private int proposedMaxServOutstandingCalled = 5;
  private int proposedDataStructureNestingLevel = 10;
  private int maxAssociations = 100;
  private ServerAcseSap acseSap;
  private int port = 102;
  private int backlog = 0;
  private InetAddress bindAddr = null;
  private ServerSocketFactory serverSocketFactory = null;

  /**
   * Creates a ServerSap.
   *
   * @param port local port to listen on for new connections
   * @param backlog The maximum queue length for incoming connection indications (a request to
   *     connect) is set to the backlog parameter. If a connection indication arrives when the queue
   *     is full, the connection is refused. Set to 0 or less for the default value.
   * @param bindAddr local IP address to bind to, pass null to bind to all
   * @param serverModel the server model
   * @param serverSocketFactory the factory class to generate the ServerSocket. Could be used to
   *     create SSLServerSockets. null = default
   */
  public ServerSap(
      int port,
      int backlog,
      InetAddress bindAddr,
      ServerModel serverModel,
      ServerSocketFactory serverSocketFactory) {
    this.port = port;
    this.backlog = backlog;
    this.bindAddr = bindAddr;
    this.serverSocketFactory = serverSocketFactory;
    this.serverModel = serverModel;
  }

  public int getPort() {
    return port;
  }

  /**
   * Sets local port to listen on for new connections.
   *
   * @param port local port to listen on for new connections
   */
  public void setPort(int port) {
    this.port = port;
  }

  public int getBacklog() {
    return backlog;
  }

  /**
   * Sets the maximum queue length for incoming connection indications (a request to connect) is set
   * to the backlog parameter. If a connection indication arrives when the queue is full, the
   * connection is refused. Set to 0 or less for the default value.
   *
   * @param backlog the maximum queue length for incoming connections.
   */
  public void setBacklog(int backlog) {
    this.backlog = backlog;
  }

  public InetAddress getBindAddress() {
    return bindAddr;
  }

  /**
   * Sets the local IP address to bind to, pass null to bind to all
   *
   * @param bindAddr the local IP address to bind to
   */
  public void setBindAddress(InetAddress bindAddr) {
    this.bindAddr = bindAddr;
  }

  /**
   * Sets the factory class to generate the ServerSocket. The ServerSocketFactory could be used to
   * create SSLServerSockets. Set to <code>null</code> to use <code>ServerSocketFactory.getDefault()
   * </code>.
   *
   * @param serverSocketFactory the factory class to generate the ServerSocket.
   */
  public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
    this.serverSocketFactory = serverSocketFactory;
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
   * Sets the maximum MMS PDU size in bytes that the server will support. If the client requires the
   * use of a smaller maximum MMS PDU size, then the smaller size will be accepted by the server.
   * The default size is 65000.
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
   * Set the maximum number of associations that are allowed in parallel by the server.
   *
   * @param maxAssociations the number of associations allowed (default is 100)
   */
  public void setMaxAssociations(int maxAssociations) {
    this.maxAssociations = maxAssociations;
  }

  /**
   * Sets the message fragment timeout. This is the timeout that the socket timeout is set to after
   * the first byte of a message has been received. If such a timeout is thrown, the
   * association/socket is closed.
   *
   * @param timeout the message fragment timeout in milliseconds. The default is 60000.
   */
  public void setMessageFragmentTimeout(int timeout) {
    acseSap.serverTSap.setMessageFragmentTimeout(timeout);
  }

  /**
   * Gets the ProposedMaxServOutstandingCalling parameter.
   *
   * @return the ProposedMaxServOutstandingCalling parameter.
   */
  public int getProposedMaxServOutstandingCalling() {
    return proposedMaxServOutstandingCalling;
  }

  /**
   * Sets the ProposedMaxServOutstandingCalling parameter. The given parameter has no affect on the
   * functionality of this server.
   *
   * @param maxCalling the ProposedMaxServOutstandingCalling parameter. The default is 5.
   */
  public void setProposedMaxServOutstandingCalling(int maxCalling) {
    proposedMaxServOutstandingCalling = maxCalling;
  }

  /**
   * Gets the ProposedMaxServOutstandingCalled parameter.
   *
   * @return the ProposedMaxServOutstandingCalled parameter.
   */
  public int getProposedMaxServOutstandingCalled() {
    return proposedMaxServOutstandingCalled;
  }

  /**
   * Sets the ProposedMaxServOutstandingCalled parameter.The given parameter has no affect on the
   * functionality of this server.
   *
   * @param maxCalled the ProposedMaxServOutstandingCalled parameter. The default is 5.
   */
  public void setProposedMaxServOutstandingCalled(int maxCalled) {
    proposedMaxServOutstandingCalled = maxCalled;
  }

  /**
   * Gets the ProposedDataStructureNestingLevel parameter.
   *
   * @return the ProposedDataStructureNestingLevel parameter.
   */
  public int getProposedDataStructureNestingLevel() {
    return proposedDataStructureNestingLevel;
  }

  /**
   * Sets the ProposedDataStructureNestingLevel parameter. The given parameter has no affect on the
   * functionality of this server.runServer
   *
   * @param nestingLevel the ProposedDataStructureNestingLevel parameter. The default is 10.
   */
  public void setProposedDataStructureNestingLevel(int nestingLevel) {
    proposedDataStructureNestingLevel = nestingLevel;
  }

  /**
   * Gets the ServicesSupportedCalled parameter.
   *
   * @return the ServicesSupportedCalled parameter.
   */
  public byte[] getServicesSupportedCalled() {
    return servicesSupportedCalled;
  }

  /**
   * Sets the SevicesSupportedCalled parameter. The given parameter has no affect on the
   * functionality of this server.
   *
   * @param services the ServicesSupportedCalled parameter
   */
  public void setServicesSupportedCalled(byte[] services) {
    if (services.length != 11) {
      throw new IllegalArgumentException("The services parameter needs to be of lenth 11");
    }
    servicesSupportedCalled = services;
  }

  /**
   * Creates a server socket waiting on the configured port for incoming association requests.
   *
   * @param serverEventListener the listener that is notified of incoming writes and when the server
   *     stopped listening for new connections.
   * @throws IOException if an error occurs binding to the port.
   */
  public void startListening(ServerEventListener serverEventListener) throws IOException {
    timer = new Timer();
    if (serverSocketFactory == null) {
      serverSocketFactory = ServerSocketFactory.getDefault();
    }
    acseSap =
        new ServerAcseSap(port, backlog, bindAddr, new AcseListener(this), serverSocketFactory);
    acseSap.serverTSap.setMaxConnections(maxAssociations);
    this.serverEventListener = serverEventListener;
    listening = true;
    acseSap.startListening();
  }

  /** Stops listening for new connections and closes all existing connections/associations. */
  public void stop() {
    acseSap.stopListening();
    synchronized (associations) {
      listening = false;
      for (ServerAssociation association : associations) {
        association.close();
      }
      associations.clear();
    }
    timer.cancel();
    timer.purge();
  }

  void connectionIndication(AcseAssociation acseAssociation, ByteBuffer psdu) {

    ServerAssociation association;
    synchronized (associations) {
      if (listening) {
        association = new ServerAssociation(this);
        associations.add(association);
      } else {
        acseAssociation.close();
        return;
      }
    }

    try {
      association.handleNewAssociation(acseAssociation, psdu);
    } catch (Exception e) {
      // Association closed because of an unexpected exception.
    }

    association.close();
    synchronized (associations) {
      associations.remove(association);
    }
  }

  void serverStoppedListeningIndication(IOException e) {
    if (serverEventListener != null) {
      serverEventListener.serverStoppedListening(this);
    }
  }

  public ServerModel getModelCopy() {
    return serverModel.copy();
  }

  public void setValues(List<BasicDataAttribute> bdas) {
    synchronized (serverModel) {
      for (BasicDataAttribute bda : bdas) {
        // if (bda.getFunctionalConstraint() != FunctionalConstraint.ST) {
        // logger.debug("fc:" + bda.getFunctionalConstraint());
        // throw new IllegalArgumentException(
        // "One can only set values of BDAs with Functional Constraint ST(status)");
        // }

        BasicDataAttribute bdaMirror = bda.mirror;

        if (bdaMirror.dchg && bdaMirror.chgRcbs.size() != 0 && !bda.equals(bdaMirror)) {
          bdaMirror.setValueFrom(bda);
          synchronized (bdaMirror.chgRcbs) {
            for (Urcb urcb : bdaMirror.chgRcbs) {
              if (bdaMirror.dupd && urcb.getTrgOps().isDataUpdate()) {
                urcb.report(bdaMirror, true, false, true);
              } else {
                urcb.report(bdaMirror, true, false, false);
              }
            }
          }
        } else if (bdaMirror.dupd && bdaMirror.dupdRcbs.size() != 0) {
          bdaMirror.setValueFrom(bda);
          synchronized (bdaMirror.dupdRcbs) {
            for (Urcb urcb : bdaMirror.dupdRcbs) {
              urcb.report(bdaMirror, false, false, true);
            }
          }
        } else if (bdaMirror.qchg && bdaMirror.chgRcbs.size() != 0 && !bda.equals(bdaMirror)) {
          bdaMirror.setValueFrom(bda);
          synchronized (bdaMirror.chgRcbs) {
            for (Urcb urcb : bdaMirror.chgRcbs) {
              urcb.report(bdaMirror, false, true, false);
            }
          }
        } else {
          bdaMirror.setValueFrom(bda);
        }
      }
    }
  }
}

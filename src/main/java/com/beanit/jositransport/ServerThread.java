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

import com.beanit.iec61850bean.internal.NamedThreadFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * This class extends Thread. It is started by ServerTSAP and listens on a socket for connections
 * and hands them to the ConnectionHandler class. It notifies ConnectionListener if the socket is
 * closed.
 *
 * @author Stefan Feuerhahn
 */
final class ServerThread extends Thread {

  private final ServerSocket serverSocket;
  private final int maxTPduSizeParam;
  private final int messageTimeout;
  private final int messageFragmentTimeout;
  private final int maxConnections;
  private final TConnectionListener connectionListener;

  private boolean stopServer = false;
  private int numConnections = 0;

  ServerThread(
      ServerSocket socket,
      int maxTPduSizeParam,
      int maxConnections,
      int messageTimeout,
      int messageFragmentTimeout,
      TConnectionListener connectionListener) {
    serverSocket = socket;
    this.maxTPduSizeParam = maxTPduSizeParam;
    this.maxConnections = maxConnections;
    this.messageTimeout = messageTimeout;
    this.messageFragmentTimeout = messageFragmentTimeout;
    this.connectionListener = connectionListener;
  }

  @Override
  public void run() {

    ExecutorService executor =
        Executors.newCachedThreadPool(new NamedThreadFactory("iec61850bean-server"));
    try {

      Socket clientSocket = null;

      while (true) {
        try {
          clientSocket = serverSocket.accept();
        } catch (IOException e) {
          if (stopServer == false) {
            connectionListener.serverStoppedListeningIndication(e);
          }
          return;
        }

        boolean startConnection = false;

        synchronized (this) {
          if (numConnections < maxConnections) {
            numConnections++;
            startConnection = true;
          }
        }

        if (startConnection) {
          executor.execute(new ConnectionHandler(clientSocket, this));
        } else {
          // Maximum number of connections reached. Ignoring connection request.
        }
      }
    } finally {
      executor.shutdown();
    }
  }

  void connectionClosedSignal() {
    synchronized (this) {
      numConnections--;
    }
  }

  /** Stops listening for new connections. Existing connections are not touched. */
  void stopServer() {
    stopServer = true;
    if (serverSocket.isBound()) {
      try {
        serverSocket.close();
      } catch (IOException e) {
        // there is nothing meaningful to be done when closing fails
      }
    }
  }

  public final class ConnectionHandler extends Thread {

    private final Socket socket;
    private final ServerThread serverThread;

    ConnectionHandler(Socket socket, ServerThread serverThread) {
      this.socket = socket;
      this.serverThread = serverThread;
    }

    @Override
    public void run() {

      TConnection tConnection;
      try {
        tConnection =
            new TConnection(
                socket, maxTPduSizeParam, messageTimeout, messageFragmentTimeout, serverThread);
      } catch (IOException e) {
        synchronized (ServerThread.this) {
          numConnections--;
        }
        return;
      }
      try {
        tConnection.listenForCR();
      } catch (IOException e) {
        tConnection.close();
        return;
      }
      connectionListener.connectionIndication(tConnection);
    }
  }
}

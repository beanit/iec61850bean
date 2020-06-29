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

import com.beanit.iec61850bean.internal.util.SequenceNumber;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class TConnection {

  // some servers do not like srcRef 0
  private static final SequenceNumber connectionCounter = new SequenceNumber(1, 1, 65519);
  private final Socket socket;
  private final DataOutputStream os;
  private final DataInputStream is;
  private final ServerThread serverThread;
  public byte[] tSelRemote = null;
  public byte[] tSelLocal = null;
  private int srcRef;
  private int dstRef;
  private int maxTPduSizeParam;
  private int maxTPduSize;
  private int messageTimeout;
  private int messageFragmentTimeout;
  private boolean closed = false;

  TConnection(
      Socket socket,
      int maxTPduSizeParam,
      int messageTimeout,
      int messageFragmentTimeout,
      ServerThread serverThread)
      throws IOException {
    if (maxTPduSizeParam < 7 || maxTPduSizeParam > 16) {
      throw new RuntimeException("maxTPduSizeParam is incorrect");
    }
    this.socket = socket;
    os = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    is = new DataInputStream(new BufferedInputStream(socket.getInputStream()));

    synchronized (connectionCounter) {
      srcRef = connectionCounter.getAndIncrement();
    }

    this.messageTimeout = messageTimeout;
    this.messageFragmentTimeout = messageFragmentTimeout;
    this.maxTPduSizeParam = maxTPduSizeParam;
    maxTPduSize = ClientTSap.getMaxTPDUSize(maxTPduSizeParam);

    this.serverThread = serverThread;
  }

  /**
   * This function is called once a client has connected to the server. It listens for a Connection
   * Request (CR). If this is successful it replies afterwards with a Connection Confirm (CC).
   * According to the norm a syntax error in the CR should be followed by an ER. This implementation
   * does not send an ER because it seems unnecessary.
   *
   * @throws IOException if an error occurs
   */
  void listenForCR() throws IOException {

    socket.setSoTimeout(messageFragmentTimeout);

    byte myByte;
    int lengthIndicator;
    int parameterLength;

    // start reading rfc 1006 header
    if (is.read() != 0x03) {
      throw new IOException();
    }
    if (is.read() != 0) {
      throw new IOException();
    }
    // read Packet length, but is not needed
    is.readShort();
    // reading rfc 1006 header finished

    lengthIndicator = is.read() & 0xff;
    // 0xe0 is the CR-Code
    if ((is.read() & 0xff) != 0xe0) {
      throw new IOException();
    }
    // DST-REF needs to be 0 in a CR packet
    if (is.readShort() != 0) {
      throw new IOException();
    }
    // read the srcRef which is the dstRef for this end-point
    dstRef = is.readShort() & 0xffff;
    // read class
    if ((is.read() & 0xff) != 0) {
      throw new IOException();
    }
    int variableBytesRead = 0;
    while (lengthIndicator > (6 + variableBytesRead)) {
      // read parameter code
      myByte = is.readByte();
      switch (myByte & 0xff) {
        case 0xc2:
          parameterLength = is.readByte() & 0xff;

          if (tSelLocal == null) {
            tSelLocal = new byte[parameterLength];
            is.readFully(tSelLocal);
          } else {
            if (parameterLength != tSelLocal.length) {
              throw new IOException("local T-SElECTOR is wrong.");
            }
            for (int i = 0; i < parameterLength; i++) {
              if ((tSelLocal[i] & 0xff) != is.read()) {
                throw new IOException("local T-SElECTOR is wrong.");
              }
            }
          }
          variableBytesRead += 2 + parameterLength;
          break;
        case 0xc1:
          parameterLength = is.readByte() & 0xff;

          if (tSelRemote == null) {
            tSelRemote = new byte[parameterLength];
            is.readFully(tSelRemote);
          } else {
            if (parameterLength != tSelRemote.length) {
              throw new IOException("remote T-SElECTOR is wrong.");
            }
            for (int i = 0; i < parameterLength; i++) {
              if ((tSelRemote[i] & 0xff) != is.read()) {
                throw new IOException("remote T-SElECTOR is wrong.");
              }
            }
          }
          variableBytesRead += 2 + parameterLength;
          break;

        case 0xc0:
          if ((is.readByte() & 0xff) != 1) {
            throw new IOException();
          }
          myByte = is.readByte();
          int newMaxTPDUSizeParam = (myByte & 0xff);
          if (newMaxTPDUSizeParam < 7 || newMaxTPDUSizeParam > 16) {
            throw new IOException("maxTPDUSizeParam is out of bound");
          } else {
            if (newMaxTPDUSizeParam < maxTPduSizeParam) {
              maxTPduSizeParam = newMaxTPDUSizeParam;
              maxTPduSize = ClientTSap.getMaxTPDUSize(maxTPduSizeParam);
            }
          }
          variableBytesRead += 3;
          break;
        default:
          throw new IOException();
      }
    }

    // write RFC 1006 Header
    os.write(0x03);
    os.write(0x00);
    // write complete packet length

    int variableLength = 3;

    if (tSelLocal != null) {
      variableLength += 2 + tSelLocal.length;
    }
    if (tSelRemote != null) {
      variableLength += 2 + tSelRemote.length;
    }
    os.writeShort(4 + 7 + variableLength);

    // write connection request (CR) TPDU (§13.3)

    // write length indicator
    os.write(6 + variableLength);

    // write fixed part
    // write CC CDT
    os.write(0xd0);
    // write DST-REF
    os.writeShort(dstRef);
    // write SRC-REF
    os.writeShort(srcRef);
    // write class
    os.write(0);

    // write variable part
    if (tSelLocal != null) {
      os.write(0xc2);
      os.write(tSelLocal.length);
      os.write(tSelLocal);
    }

    if (tSelRemote != null) {
      os.write(0xc1);
      os.write(tSelRemote.length);
      os.write(tSelRemote);
    }
    // write proposed maximum TPDU Size
    os.write(0xc0);
    os.write(1);
    os.write(maxTPduSizeParam);

    os.flush();
  }

  /**
   * Starts a connection, sends a CR, waits for a CC and throws an IOException if not successful
   *
   * @throws IOException if an error occurs
   */
  void startConnection() throws IOException {

    // write RFC 1006 Header
    os.write(0x03);
    os.write(0x00);

    // write complete packet length
    int variableLength = 3;

    if (tSelLocal != null) {
      variableLength += 2 + tSelLocal.length;
    }
    if (tSelRemote != null) {
      variableLength += 2 + tSelRemote.length;
    }
    os.writeShort(4 + 7 + variableLength);
    // writing RFC 1006 Header finished

    // write connection request (CR) TPDU (§13.3)

    // write length indicator
    os.write(6 + variableLength);

    // write fixed part
    // write CR CDT
    os.write(0xe0);
    // write DST-REF
    os.write(0);
    os.write(0);
    // write SRC-REF
    os.writeShort(srcRef);
    // write class
    os.write(0);

    // write variable part
    // write proposed maximum TPDU Size
    os.write(0xc0);
    os.write(1);
    os.write(maxTPduSizeParam);

    if (tSelRemote != null) {
      os.write(0xc2);
      os.write(tSelRemote.length);
      os.write(tSelRemote);
    }
    if (tSelLocal != null) {
      os.write(0xc1);
      os.write(tSelLocal.length);
      os.write(tSelLocal);
    }

    os.flush();

    socket.setSoTimeout(messageTimeout);

    byte myByte;
    int lengthIndicator;
    int parameterLength;
    if (is.readByte() != 0x03) {
      throw new IOException();
    }
    if (is.readByte() != 0) {
      throw new IOException();
    }
    // read packet length, but is not needed
    is.readShort();
    lengthIndicator = is.readByte() & 0xff;
    if ((is.readByte() & 0xff) != 0xd0) {
      throw new IOException();
    }
    // read the dstRef which is the srcRef for this end-point
    is.readShort();
    // read the srcRef which is the dstRef for this end-point
    dstRef = is.readShort() & 0xffff;
    // read class
    if (is.readByte() != 0) {
      throw new IOException();
    }

    int variableBytesRead = 0;
    while (lengthIndicator > (6 + variableBytesRead)) {
      // read parameter code
      myByte = is.readByte();
      switch (myByte & 0xff) {
        case 0xc1:
          parameterLength = is.readByte() & 0xff;

          if (tSelLocal == null) {
            tSelLocal = new byte[parameterLength];
            is.readFully(tSelLocal);
          } else {
            for (int i = 0; i < parameterLength; i++) {
              is.read();
            }
          }
          variableBytesRead += 2 + parameterLength;
          break;
        case 0xc2:
          parameterLength = is.readByte() & 0xff;

          if (tSelRemote == null) {
            tSelRemote = new byte[parameterLength];
            is.readFully(tSelRemote);
          } else {
            for (int i = 0; i < parameterLength; i++) {
              is.read();
            }
          }
          variableBytesRead += 2 + parameterLength;
          break;

        case 0xc0:
          if (is.readByte() != 1) {
            throw new IOException("maxTPduSizeParam size is not equal to 1");
          }
          myByte = is.readByte();
          if ((myByte & 0xff) < 7 || (myByte & 0xff) > maxTPduSizeParam) {
            throw new IOException("maxTPduSizeParam out of bound");
          } else {
            if ((myByte & 0xff) < maxTPduSizeParam) {
              maxTPduSizeParam = (myByte & 0xff);
            }
          }
          variableBytesRead += 4;
          break;
        default:
          throw new IOException();
      }
    }
  }

  public void send(List<byte[]> tsdus, List<Integer> offsets, List<Integer> lengths)
      throws IOException {

    int bytesLeft = 0;
    // for (byte[] tsdu : tsdus) {
    // bytesLeft += tsdu.length;
    // }
    for (int length : lengths) {
      bytesLeft += length;
    }
    int tsduOffset = 0;
    int byteArrayListIndex = 0;
    int numBytesToWrite;
    boolean lastPacket = false;
    int maxTSDUSize = maxTPduSize - 3;
    while (bytesLeft > 0) {

      if (bytesLeft > maxTSDUSize) {
        numBytesToWrite = maxTSDUSize;
      } else {
        numBytesToWrite = bytesLeft;
        lastPacket = true;
      }

      // --write RFC 1006 Header--
      // write Version
      os.write(0x03);
      // write reserved bits
      os.write(0);
      // write packet Length
      os.writeShort(numBytesToWrite + 7);

      // --write 8073 Header--
      // write Length Indicator of header
      os.write(0x02);
      // write TPDU Code for DT Data
      os.write(0xf0);
      // write TPDU-NR and EOT, TPDU-NR is always 0 for class 0
      if (lastPacket) {
        os.write(0x80);
      } else {
        os.write(0x00);
      }

      bytesLeft -= numBytesToWrite;
      while (numBytesToWrite > 0) {
        byte[] tsdu = tsdus.get(byteArrayListIndex);
        int length = lengths.get(byteArrayListIndex);
        int offset = offsets.get(byteArrayListIndex);

        int tsduWriteLength = length - tsduOffset;

        if (numBytesToWrite > tsduWriteLength) {
          os.write(tsdu, offset + tsduOffset, tsduWriteLength);
          numBytesToWrite -= tsduWriteLength;
          tsduOffset = 0;
          byteArrayListIndex++;
        } else {
          os.write(tsdu, offset + tsduOffset, numBytesToWrite);
          if (numBytesToWrite == tsduWriteLength) {
            tsduOffset = 0;
            byteArrayListIndex++;
          } else {
            tsduOffset += numBytesToWrite;
          }
          numBytesToWrite = 0;
        }
      }

      os.flush();
    }
  }

  public void send(byte[] tsdu, int offset, int length) throws IOException {
    List<byte[]> tsdus = new ArrayList<>();
    tsdus.add(tsdu);
    List<Integer> offsets = new ArrayList<>();
    offsets.add(offset);
    List<Integer> lengths = new ArrayList<>();
    lengths.add(length);
    send(tsdus, offsets, lengths);
  }

  public int getMessageTimeout() {
    return messageTimeout;
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

  public int getMessageFragmentTimeout() {
    return messageFragmentTimeout;
  }

  /**
   * Set the TConnection timeout for receiving data once the beginning of a message has been
   * received. Default is 60000 (60 seconds)
   *
   * @param messageFragmentTimeout in milliseconds
   */
  public void setMessageFragmentTimeout(int messageFragmentTimeout) {
    this.messageFragmentTimeout = messageFragmentTimeout;
  }

  /**
   * Listens for a new TPDU and writes the extracted TSDU into the passed buffer.
   *
   * @param tSduBuffer the buffer that is filled with the received TSDU data.
   * @throws EOFException if a Disconnect Request (DR) was received or the socket was simply closed
   * @throws SocketTimeoutException if a messageFragmentTimeout is thrown by the socket while
   *     receiving the remainder of a message
   * @throws IOException if an ErrorPDU (ER) was received, any syntax error in the received message
   *     header was detected or the tSduBuffer is too small to hold the complete PDU.
   * @throws TimeoutException this exception is thrown if the first byte of new message is not
   *     received within the message timeout.
   */
  public void receive(ByteBuffer tSduBuffer)
      throws EOFException, SocketTimeoutException, IOException, TimeoutException {

    tSduBuffer.mark();

    int packetLength;
    int eot = 0;
    int li = 0;
    int tPduCode;

    socket.setSoTimeout(messageTimeout);
    byte version;
    try {
      version = is.readByte();
    } catch (SocketTimeoutException e) {
      throw (new TimeoutException());
    }
    socket.setSoTimeout(messageFragmentTimeout);

    do {
      // read version
      if (version != 3) {
        throw new IOException(
            "Syntax error at beginning of RFC1006 header: version not equal to 3");
      }

      // read reserved
      if (is.readByte() != 0) {
        throw new IOException(
            "Syntax errorat beginning of RFC1006 header: reserved not equal to 0");
      }

      // read packet length
      packetLength = is.readShort() & 0xffff;
      if (packetLength <= 7) {
        throw new IOException("Syntax error: packet length parameter < 7");
      }

      // read length indicator
      li = is.readByte() & 0xff;

      // read TPDU code
      tPduCode = is.readByte() & 0xff;

      if (tPduCode == 0xf0) {
        // Data Transfer (DT) Code

        if (li != 2) {
          throw new IOException("Syntax error: LI field does not equal 2");
        }

        // read EOT
        eot = is.readByte() & 0xff;
        if (eot != 0 && eot != 0x80) {
          throw new IOException("Syntax error: eot wrong");
        }

        if (packetLength - 7 > tSduBuffer.limit() - tSduBuffer.position()) {
          throw new IOException("tSduBuffer size is too small to hold the complete TSDU");
        }
        is.readFully(
            tSduBuffer.array(), tSduBuffer.arrayOffset() + tSduBuffer.position(), packetLength - 7);
        tSduBuffer.position(tSduBuffer.position() + packetLength - 7);
      } else if (tPduCode == 0x80) {
        // Disconnect Request (DR)

        if (li != 6) {
          throw new IOException("Syntax error: LI field does not equal 6");
        }

        // check if the DST-REF field is set to the reference of the
        // receiving entity -> srcRef
        if (is.readShort() != srcRef) {
          throw new IOException("Syntax error: srcRef wrong");
        }

        // check if the SRC-REF field is that of the entity sending
        // the DR
        if (is.readShort() != dstRef) {
          throw new IOException("Syntax error: dstRef wrong");
        }

        // check the reason field, for class 0 only between 1 and 4
        int reason = is.readByte() & 0xff;
        if (reason > 4) {
          throw new IOException("Syntax error: reason out of bound");
        }

        // Disconnect is valid, throw exception
        throw new EOFException("Disconnect request. Reason:" + reason);

      } else if (tPduCode == 0x70) {
        throw new IOException("Got TPDU error (ER) message");
      } else {
        throw new IOException("Syntax error: unknown TPDU code");
      }

      if (eot != 0x80) {
        version = is.readByte();
      }

    } while (eot != 0x80);

    tSduBuffer.limit(tSduBuffer.position());
    tSduBuffer.reset();
  }

  /** This function sends a Disconnect Request but does not wait for a Disconnect Confirm. */
  public void disconnect() {

    try {
      // write header for rfc
      // write version
      os.write(0x03);
      // write reserved
      os.write(0x00);

      // write packet length
      os.writeShort(4 + 7); // this does not include the variable part
      // which
      // contains additional user information for
      // disconnect

      // beginning of ISO 8073 header
      // write LI
      os.write(0x06);

      // write DR
      os.write(0x80);

      // write DST-REF
      os.writeShort(dstRef);

      // write SRC-REF
      os.writeShort(srcRef);

      // write reason - 0x00 corresponds to reason not specified. Can
      // write
      // the reasons as case structure, but need input from client
      os.write(0x00);

      os.flush();
    } catch (IOException ignored) {
      // io exceptions while disconnecting can be ignored
    } finally {
      close();
    }
  }

  /** Will close the TCP connection if its still open and free any resources of this connection. */
  public void close() {
    if (!closed) {
      closed = true;
      try {
        // will also close socket
        os.close();
      } catch (Exception e) {
        // there is nothing meaningful to be done if closing fails
      }
      try {
        is.close();
      } catch (Exception e) {
        // there is nothing meaningful to be done if closing fails
      }
      if (serverThread != null) {
        serverThread.connectionClosedSignal();
      }
    }
  }
}

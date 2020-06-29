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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.ber.types.BerNull;
import com.beanit.asn1bean.ber.types.string.BerGraphicString;
import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import com.beanit.iec61850bean.internal.BerBoolean;
import com.beanit.iec61850bean.internal.mms.asn1.AccessResult;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedRequestPDU;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedResponsePDU;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedServiceRequest;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedServiceResponse;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.DefineNamedVariableListRequest;
import com.beanit.iec61850bean.internal.mms.asn1.DeleteNamedVariableListRequest;
import com.beanit.iec61850bean.internal.mms.asn1.DeleteNamedVariableListRequest.ListOfVariableListName;
import com.beanit.iec61850bean.internal.mms.asn1.DeleteNamedVariableListResponse;
import com.beanit.iec61850bean.internal.mms.asn1.DirectoryEntry;
import com.beanit.iec61850bean.internal.mms.asn1.FileCloseRequest;
import com.beanit.iec61850bean.internal.mms.asn1.FileDeleteRequest;
import com.beanit.iec61850bean.internal.mms.asn1.FileDirectoryRequest;
import com.beanit.iec61850bean.internal.mms.asn1.FileDirectoryResponse;
import com.beanit.iec61850bean.internal.mms.asn1.FileName;
import com.beanit.iec61850bean.internal.mms.asn1.FileOpenRequest;
import com.beanit.iec61850bean.internal.mms.asn1.FileReadRequest;
import com.beanit.iec61850bean.internal.mms.asn1.GetNameListRequest;
import com.beanit.iec61850bean.internal.mms.asn1.GetNameListRequest.ObjectScope;
import com.beanit.iec61850bean.internal.mms.asn1.GetNameListResponse;
import com.beanit.iec61850bean.internal.mms.asn1.GetNamedVariableListAttributesRequest;
import com.beanit.iec61850bean.internal.mms.asn1.GetNamedVariableListAttributesResponse;
import com.beanit.iec61850bean.internal.mms.asn1.GetVariableAccessAttributesRequest;
import com.beanit.iec61850bean.internal.mms.asn1.Identifier;
import com.beanit.iec61850bean.internal.mms.asn1.InitiateRequestPDU;
import com.beanit.iec61850bean.internal.mms.asn1.InitiateResponsePDU;
import com.beanit.iec61850bean.internal.mms.asn1.Integer16;
import com.beanit.iec61850bean.internal.mms.asn1.Integer32;
import com.beanit.iec61850bean.internal.mms.asn1.Integer8;
import com.beanit.iec61850bean.internal.mms.asn1.MMSpdu;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectClass;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectName;
import com.beanit.iec61850bean.internal.mms.asn1.ParameterSupportOptions;
import com.beanit.iec61850bean.internal.mms.asn1.ReadRequest;
import com.beanit.iec61850bean.internal.mms.asn1.ReadResponse;
import com.beanit.iec61850bean.internal.mms.asn1.RejectPDU.RejectReason;
import com.beanit.iec61850bean.internal.mms.asn1.ServiceError.ErrorClass;
import com.beanit.iec61850bean.internal.mms.asn1.ServiceSupportOptions;
import com.beanit.iec61850bean.internal.mms.asn1.UnconfirmedPDU;
import com.beanit.iec61850bean.internal.mms.asn1.UnconfirmedService;
import com.beanit.iec61850bean.internal.mms.asn1.Unsigned32;
import com.beanit.iec61850bean.internal.mms.asn1.VariableAccessSpecification;
import com.beanit.iec61850bean.internal.mms.asn1.VariableDefs;
import com.beanit.iec61850bean.internal.mms.asn1.WriteRequest;
import com.beanit.iec61850bean.internal.mms.asn1.WriteRequest.ListOfData;
import com.beanit.iec61850bean.internal.mms.asn1.WriteResponse;
import com.beanit.josistack.AcseAssociation;
import com.beanit.josistack.ByteBufferInputStream;
import com.beanit.josistack.ClientAcseSap;
import com.beanit.josistack.DecodingException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Represents an association/connection to an IEC 61850 MMS server. An instance of <code>
 * ClientAssociation</code> is obtained using <code>ClientSap</code>. An association object can be
 * used to execute the IEC 61850 ACSI services. Note that not all ACSI services have a corresponding
 * function in this API. For example all GetDirectory and GetDefinition services are covered by
 * <code>retrieveModel()</code>. The control services can be executed by using getDataValues and
 * setDataValues on the control objects in the data model.
 */
public final class ClientAssociation {

  private static final Integer16 version = new Integer16(new byte[] {(byte) 0x01, (byte) 0x01});
  private static final ParameterSupportOptions proposedParameterCbbBitString =
      new ParameterSupportOptions(new byte[] {0x03, 0x05, (byte) 0xf1, 0x00});
  private final ClientReceiver clientReceiver;
  private final BlockingQueue<MMSpdu> incomingResponses = new LinkedBlockingQueue<>();
  private final ReverseByteArrayOutputStream reverseOStream =
      new ReverseByteArrayOutputStream(500, true);
  ServerModel serverModel;
  private AcseAssociation acseAssociation = null;
  private int responseTimeout;

  private int invokeId = 0;

  private int negotiatedMaxPduSize;
  private ClientEventListener reportListener = null;

  private boolean closed = false;

  ClientAssociation(
      InetAddress address,
      int port,
      InetAddress localAddr,
      int localPort,
      String authenticationParameter,
      ClientAcseSap acseSap,
      int proposedMaxMmsPduSize,
      int proposedMaxServOutstandingCalling,
      int proposedMaxServOutstandingCalled,
      int proposedDataStructureNestingLevel,
      byte[] servicesSupportedCalling,
      int responseTimeout,
      int messageFragmentTimeout,
      ClientEventListener reportListener)
      throws IOException {

    this.responseTimeout = responseTimeout;

    acseSap.tSap.setMessageFragmentTimeout(messageFragmentTimeout);
    acseSap.tSap.setMessageTimeout(responseTimeout);

    negotiatedMaxPduSize = proposedMaxMmsPduSize;

    this.reportListener = reportListener;

    associate(
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
        servicesSupportedCalling);

    acseAssociation.setMessageTimeout(0);

    clientReceiver = new ClientReceiver(negotiatedMaxPduSize);
    clientReceiver.start();
  }

  private static ServiceError mmsDataAccessErrorToServiceError(BerInteger dataAccessError) {

    switch (dataAccessError.value.intValue()) {
      case 1:
        return new ServiceError(
            ServiceError.FAILED_DUE_TO_SERVER_CONSTRAINT, "MMS DataAccessError: hardware-fault");
      case 2:
        return new ServiceError(
            ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT,
            "MMS DataAccessError: temporarily-unavailable");
      case 3:
        return new ServiceError(
            ServiceError.ACCESS_VIOLATION, "MMS DataAccessError: object-access-denied");
      case 5:
        return new ServiceError(
            ServiceError.PARAMETER_VALUE_INCONSISTENT, "MMS DataAccessError: invalid-address");
      case 7:
        return new ServiceError(
            ServiceError.TYPE_CONFLICT, "MMS DataAccessError: type-inconsistent");
      case 10:
        return new ServiceError(
            ServiceError.INSTANCE_NOT_AVAILABLE, "MMS DataAccessError: object-non-existent");
      case 11:
        return new ServiceError(
            ServiceError.PARAMETER_VALUE_INCONSISTENT, "MMS DataAccessError: object-value-invalid");
      default:
        return new ServiceError(
            ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
            "MMS DataAccessError: " + dataAccessError.value);
    }
  }

  private static void testForErrorResponse(MMSpdu mmsResponsePdu) throws ServiceError {
    if (mmsResponsePdu.getConfirmedErrorPDU() == null) {
      return;
    }

    ErrorClass errClass = mmsResponsePdu.getConfirmedErrorPDU().getServiceError().getErrorClass();

    if (errClass != null) {
      if (errClass.getAccess() != null) {
        if (errClass.getAccess().value.intValue() == 3) {
          throw new ServiceError(
              ServiceError.ACCESS_VIOLATION,
              "MMS confirmed error: class: \"access\", error code: \"object-access-denied\"");
        } else if (errClass.getAccess().value.intValue() == 2) {

          throw new ServiceError(
              ServiceError.INSTANCE_NOT_AVAILABLE,
              "MMS confirmed error: class: \"access\", error code: \"object-non-existent\"");
        }

      } else if (errClass.getFile() != null) {
        if (errClass.getFile().value.intValue() == 7) {

          throw new ServiceError(
              ServiceError.FILE_NONE_EXISTENT,
              "MMS confirmed error: class: \"file\", error code: \"file-non-existent\"");
        }
      }
    }

    if (mmsResponsePdu.getConfirmedErrorPDU().getServiceError().getAdditionalDescription()
        != null) {
      throw new ServiceError(
          ServiceError.UNKNOWN,
          "MMS confirmed error. Description: "
              + mmsResponsePdu
                  .getConfirmedErrorPDU()
                  .getServiceError()
                  .getAdditionalDescription()
                  .toString());
    }
    throw new ServiceError(ServiceError.UNKNOWN, "MMS confirmed error.");
  }

  private static void testForRejectResponse(MMSpdu mmsResponsePdu) throws ServiceError {
    if (mmsResponsePdu.getRejectPDU() == null) {
      return;
    }

    RejectReason rejectReason = mmsResponsePdu.getRejectPDU().getRejectReason();
    if (rejectReason != null) {
      if (rejectReason.getPduError() != null) {
        if (rejectReason.getPduError().value.intValue() == 1) {
          throw new ServiceError(
              ServiceError.PARAMETER_VALUE_INCONSISTENT,
              "MMS reject: type: \"pdu-error\", reject code: \"invalid-pdu\"");
        }
      }
    }
    throw new ServiceError(ServiceError.UNKNOWN, "MMS confirmed error.");
  }

  private static void testForInitiateErrorResponse(MMSpdu mmsResponsePdu) throws ServiceError {
    if (mmsResponsePdu.getInitiateErrorPDU() != null) {

      ErrorClass errClass = mmsResponsePdu.getInitiateErrorPDU().getErrorClass();
      if (errClass != null) {
        if (errClass.getVmdState() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"vmd_state\" with val: " + errClass.getVmdState().value);
        }
        if (errClass.getApplicationReference() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"application_reference\" with val: "
                  + errClass.getApplicationReference().value);
        }
        if (errClass.getDefinition() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"definition\" with val: " + errClass.getDefinition().value);
        }
        if (errClass.getResource() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"resource\" with val: " + errClass.getResource().value);
        }
        if (errClass.getService() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"service\" with val: " + errClass.getService().value);
        }
        if (errClass.getServicePreempt() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"service_preempt\" with val: " + errClass.getServicePreempt().value);
        }
        if (errClass.getTimeResolution() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"time_resolution\" with val: " + errClass.getTimeResolution().value);
        }
        if (errClass.getAccess() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"access\" with val: " + errClass.getAccess().value);
        }
        if (errClass.getInitiate() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"initiate\" with val: " + errClass.getInitiate().value);
        }
        if (errClass.getConclude() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"conclude\" with val: " + errClass.getConclude());
        }
        if (errClass.getCancel() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"cancel\" with val: " + errClass.getCancel().value);
        }
        if (errClass.getFile() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"file\" with val: " + errClass.getFile().value);
        }
        if (errClass.getOthers() != null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "error class \"others\" with val: " + errClass.getOthers().value);
        }
      }

      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "unknown error class");
    }
  }

  private static MMSpdu constructInitRequestPdu(
      int proposedMaxPduSize,
      int proposedMaxServOutstandingCalling,
      int proposedMaxServOutstandingCalled,
      int proposedDataStructureNestingLevel,
      byte[] servicesSupportedCalling) {

    InitiateRequestPDU.InitRequestDetail initRequestDetail =
        new InitiateRequestPDU.InitRequestDetail();
    initRequestDetail.setProposedVersionNumber(version);
    initRequestDetail.setProposedParameterCBB(proposedParameterCbbBitString);
    initRequestDetail.setServicesSupportedCalling(
        new ServiceSupportOptions(servicesSupportedCalling, 85));

    InitiateRequestPDU initiateRequestPdu = new InitiateRequestPDU();
    initiateRequestPdu.setLocalDetailCalling(new Integer32(proposedMaxPduSize));
    initiateRequestPdu.setProposedMaxServOutstandingCalling(
        new Integer16(proposedMaxServOutstandingCalling));
    initiateRequestPdu.setProposedMaxServOutstandingCalled(
        new Integer16(proposedMaxServOutstandingCalled));
    initiateRequestPdu.setProposedDataStructureNestingLevel(
        new Integer8(proposedDataStructureNestingLevel));
    initiateRequestPdu.setInitRequestDetail(initRequestDetail);

    MMSpdu initiateRequestMMSpdu = new MMSpdu();
    initiateRequestMMSpdu.setInitiateRequestPDU(initiateRequestPdu);

    return initiateRequestMMSpdu;
  }

  /**
   * Gets the response timeout. The response timeout is used whenever a request is sent to the
   * server. The client will wait for this amount of time for the server's response before throwing
   * a ServiceError.TIMEOUT. Responses received after the timeout will be automatically discarded.
   *
   * @return the response timeout in milliseconds.
   */
  public int getResponseTimeout() {
    return responseTimeout;
  }

  /**
   * Sets the response timeout. The response timeout is used whenever a request is sent to the
   * server. The client will wait for this amount of time for the server's response before throwing
   * a ServiceError.TIMEOUT. Responses received after the timeout will be automatically discarded.
   *
   * @param timeout the response timeout in milliseconds.
   */
  public void setResponseTimeout(int timeout) {
    responseTimeout = timeout;
  }

  private int getInvokeId() {
    invokeId = (invokeId + 1) % 2147483647;
    return invokeId;
  }

  private ConfirmedServiceResponse encodeWriteReadDecode(ConfirmedServiceRequest serviceRequest)
      throws ServiceError, IOException {

    int currentInvokeId = getInvokeId();

    ConfirmedRequestPDU confirmedRequestPdu = new ConfirmedRequestPDU();
    confirmedRequestPdu.setInvokeID(new Unsigned32(currentInvokeId));
    confirmedRequestPdu.setService(serviceRequest);

    MMSpdu requestPdu = new MMSpdu();
    requestPdu.setConfirmedRequestPDU(confirmedRequestPdu);

    reverseOStream.reset();

    try {
      requestPdu.encode(reverseOStream);
    } catch (Exception e) {
      IOException e2 = new IOException("Error encoding MmsPdu.", e);
      clientReceiver.close(e2);
      throw e2;
    }

    clientReceiver.setResponseExpected(currentInvokeId);
    try {
      acseAssociation.send(reverseOStream.getByteBuffer());
    } catch (IOException e) {
      IOException e2 = new IOException("Error sending packet.", e);
      clientReceiver.close(e2);
      throw e2;
    }

    MMSpdu decodedResponsePdu = null;

    try {
      if (responseTimeout == 0) {
        decodedResponsePdu = incomingResponses.take();
      } else {
        decodedResponsePdu = incomingResponses.poll(responseTimeout, TimeUnit.MILLISECONDS);
      }
    } catch (InterruptedException e) {
      // TODO can this ever be interrupted?
    }

    if (decodedResponsePdu == null) {
      decodedResponsePdu = clientReceiver.removeExpectedResponse();
      if (decodedResponsePdu == null) {
        throw new ServiceError(ServiceError.TIMEOUT);
      }
    }

    if (decodedResponsePdu.getConfirmedRequestPDU() != null) {
      incomingResponses.add(decodedResponsePdu);
      throw new IOException("connection was closed", clientReceiver.getLastIOException());
    }

    testForInitiateErrorResponse(decodedResponsePdu);
    testForErrorResponse(decodedResponsePdu);
    testForRejectResponse(decodedResponsePdu);

    ConfirmedResponsePDU confirmedResponsePdu = decodedResponsePdu.getConfirmedResponsePDU();
    if (confirmedResponsePdu == null) {
      throw new IllegalStateException("Response PDU is not a confirmed response pdu");
    }

    return confirmedResponsePdu.getService();
  }

  private void associate(
      InetAddress address,
      int port,
      InetAddress localAddr,
      int localPort,
      String authenticationParameter,
      ClientAcseSap acseSap,
      int proposedMaxPduSize,
      int proposedMaxServOutstandingCalling,
      int proposedMaxServOutstandingCalled,
      int proposedDataStructureNestingLevel,
      byte[] servicesSupportedCalling)
      throws IOException {

    MMSpdu initiateRequestMMSpdu =
        constructInitRequestPdu(
            proposedMaxPduSize,
            proposedMaxServOutstandingCalling,
            proposedMaxServOutstandingCalled,
            proposedDataStructureNestingLevel,
            servicesSupportedCalling);

    ReverseByteArrayOutputStream reverseOStream = new ReverseByteArrayOutputStream(500, true);
    initiateRequestMMSpdu.encode(reverseOStream);

    try {
      acseAssociation =
          acseSap.associate(
              address,
              port,
              localAddr,
              localPort,
              authenticationParameter,
              reverseOStream.getByteBuffer());

      ByteBuffer initResponse = acseAssociation.getAssociateResponseAPdu();

      MMSpdu initiateResponseMmsPdu = new MMSpdu();

      initiateResponseMmsPdu.decode(new ByteBufferInputStream(initResponse), null);

      handleInitiateResponse(
          initiateResponseMmsPdu,
          proposedMaxPduSize,
          proposedMaxServOutstandingCalling,
          proposedMaxServOutstandingCalled,
          proposedDataStructureNestingLevel);
    } catch (IOException e) {
      if (acseAssociation != null) {
        acseAssociation.close();
      }
      throw e;
    }
  }

  private void handleInitiateResponse(
      MMSpdu responsePdu,
      int proposedMaxPduSize,
      int proposedMaxServOutstandingCalling,
      int proposedMaxServOutstandingCalled,
      int proposedDataStructureNestingLevel)
      throws IOException {

    if (responsePdu.getInitiateErrorPDU() != null) {
      throw new IOException(
          "Got response error of class: " + responsePdu.getInitiateErrorPDU().getErrorClass());
    }

    if (responsePdu.getInitiateResponsePDU() == null) {
      acseAssociation.disconnect();
      throw new IOException("Error decoding InitiateResponse Pdu");
    }

    InitiateResponsePDU initiateResponsePdu = responsePdu.getInitiateResponsePDU();

    if (initiateResponsePdu.getLocalDetailCalled() != null) {
      negotiatedMaxPduSize = initiateResponsePdu.getLocalDetailCalled().intValue();
    }

    int negotiatedMaxServOutstandingCalling =
        initiateResponsePdu.getNegotiatedMaxServOutstandingCalling().intValue();
    int negotiatedMaxServOutstandingCalled =
        initiateResponsePdu.getNegotiatedMaxServOutstandingCalled().intValue();

    int negotiatedDataStructureNestingLevel;
    if (initiateResponsePdu.getNegotiatedDataStructureNestingLevel() != null) {
      negotiatedDataStructureNestingLevel =
          initiateResponsePdu.getNegotiatedDataStructureNestingLevel().intValue();
    } else {
      negotiatedDataStructureNestingLevel = proposedDataStructureNestingLevel;
    }

    if (negotiatedMaxPduSize < ClientSap.MINIMUM_MMS_PDU_SIZE
        || negotiatedMaxPduSize > proposedMaxPduSize
        || negotiatedMaxServOutstandingCalling > proposedMaxServOutstandingCalling
        || negotiatedMaxServOutstandingCalling < 0
        || negotiatedMaxServOutstandingCalled > proposedMaxServOutstandingCalled
        || negotiatedMaxServOutstandingCalled < 0
        || negotiatedDataStructureNestingLevel > proposedDataStructureNestingLevel
        || negotiatedDataStructureNestingLevel < 0) {
      acseAssociation.disconnect();
      throw new IOException("Error negotiating parameters");
    }

    int version =
        initiateResponsePdu.getInitResponseDetail().getNegotiatedVersionNumber().intValue();
    if (version != 1) {
      throw new IOException("Unsupported version number was negotiated.");
    }

    byte[] servicesSupported =
        initiateResponsePdu.getInitResponseDetail().getServicesSupportedCalled().value;
    if ((servicesSupported[0] & 0x40) != 0x40) {
      throw new IOException("Obligatory services are not supported by the server.");
    }
  }

  /**
   * Set the server model instead of retrieving it from the server device.
   *
   * @param model the server model
   */
  public void setServerModel(ServerModel model) {
    this.serverModel = model;
  }

  /**
   * Triggers all GetDirectory and GetDefinition ACSI services needed to get the complete server
   * model. Because in MMS SubDataObjects cannot be distinguished from Constructed Data Attributes
   * they will always be represented as Constructed Data Attributes in the returned model.
   *
   * @return the ServerModel that is the root node of the complete server model.
   * @throws ServiceError if a ServiceError occurs while calling any of the ASCI services.
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public ServerModel retrieveModel() throws ServiceError, IOException {

    List<String> ldNames = retrieveLogicalDevices();
    List<List<String>> lnNames = new ArrayList<>(ldNames.size());

    for (int i = 0; i < ldNames.size(); i++) {
      lnNames.add(retrieveLogicalNodeNames(ldNames.get(i)));
    }
    List<LogicalDevice> lds = new ArrayList<>();
    for (int i = 0; i < ldNames.size(); i++) {
      List<LogicalNode> lns = new ArrayList<>();
      for (int j = 0; j < lnNames.get(i).size(); j++) {
        lns.add(
            retrieveDataDefinitions(
                new ObjectReference(ldNames.get(i) + "/" + lnNames.get(i).get(j))));
      }
      lds.add(new LogicalDevice(new ObjectReference(ldNames.get(i)), lns));
    }

    serverModel = new ServerModel(lds, null);

    updateDataSets();

    return serverModel;
  }

  private List<String> retrieveLogicalDevices() throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructGetServerDirectoryRequest();
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    return decodeGetServerDirectoryResponse(confirmedServiceResponse);
  }

  private ConfirmedServiceRequest constructGetServerDirectoryRequest() {
    ObjectClass objectClass = new ObjectClass();
    objectClass.setBasicObjectClass(new BerInteger(9));

    GetNameListRequest.ObjectScope objectScope = new GetNameListRequest.ObjectScope();
    objectScope.setVmdSpecific(new BerNull());

    GetNameListRequest getNameListRequest = new GetNameListRequest();
    getNameListRequest.setObjectClass(objectClass);
    getNameListRequest.setObjectScope(objectScope);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setGetNameList(getNameListRequest);

    return confirmedServiceRequest;
  }

  private List<String> decodeGetServerDirectoryResponse(
      ConfirmedServiceResponse confirmedServiceResponse) throws ServiceError {

    if (confirmedServiceResponse.getGetNameList() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding Get Server Directory Response Pdu");
    }

    List<Identifier> identifiers =
        confirmedServiceResponse.getGetNameList().getListOfIdentifier().getIdentifier();
    ArrayList<String> objectRefs = new ArrayList<>(); // ObjectReference[identifiers.size()];

    for (BerVisibleString identifier : identifiers) {
      objectRefs.add(identifier.toString());
    }

    return objectRefs;
  }

  private List<String> retrieveLogicalNodeNames(String ld) throws ServiceError, IOException {
    List<String> lns = new ArrayList<>();
    String continueAfterRef = "";
    do {
      ConfirmedServiceRequest serviceRequest =
          constructGetDirectoryRequest(ld, continueAfterRef, true);
      ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
      continueAfterRef = decodeGetDirectoryResponse(confirmedServiceResponse, lns);

    } while (!continueAfterRef.isEmpty());
    return lns;
  }

  private ConfirmedServiceRequest constructGetDirectoryRequest(
      String ldRef, String continueAfter, boolean logicalDevice) {

    ObjectClass objectClass = new ObjectClass();

    if (logicalDevice) {
      objectClass.setBasicObjectClass(new BerInteger(0));
    } else { // for data sets
      objectClass.setBasicObjectClass(new BerInteger(2));
    }

    GetNameListRequest getNameListRequest;

    ObjectScope objectScopeChoiceType = new ObjectScope();
    objectScopeChoiceType.setDomainSpecific(new Identifier(ldRef.getBytes(UTF_8)));

    getNameListRequest = new GetNameListRequest();
    getNameListRequest.setObjectClass(objectClass);
    getNameListRequest.setObjectScope(objectScopeChoiceType);
    if (!continueAfter.isEmpty()) {
      getNameListRequest.setContinueAfter(new Identifier(continueAfter.getBytes(UTF_8)));
    }

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setGetNameList(getNameListRequest);
    return confirmedServiceRequest;
  }

  /**
   * Decodes an MMS response which contains the structure of a LD and its LNs including names of
   * DOs.
   */
  private String decodeGetDirectoryResponse(
      ConfirmedServiceResponse confirmedServiceResponse, List<String> lns) throws ServiceError {

    if (confirmedServiceResponse.getGetNameList() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "decodeGetLDDirectoryResponse: Error decoding server response");
    }

    GetNameListResponse getNameListResponse = confirmedServiceResponse.getGetNameList();

    List<Identifier> identifiers = getNameListResponse.getListOfIdentifier().getIdentifier();

    if (identifiers.size() == 0) {
      throw new ServiceError(
          ServiceError.INSTANCE_NOT_AVAILABLE,
          "decodeGetLDDirectoryResponse: Instance not available");
    }

    BerVisibleString identifier = null;
    Iterator<Identifier> it = identifiers.iterator();

    String idString;

    while (it.hasNext()) {
      identifier = it.next();
      idString = identifier.toString();

      if (idString.indexOf('$') == -1) {
        lns.add(idString);
      }
    }

    if (getNameListResponse.getMoreFollows() != null
        && getNameListResponse.getMoreFollows().value == false) {
      return "";
    } else {
      return identifier.toString();
    }
  }

  private LogicalNode retrieveDataDefinitions(ObjectReference lnRef)
      throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructGetDataDefinitionRequest(lnRef);
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    return decodeGetDataDefinitionResponse(confirmedServiceResponse, lnRef);
  }

  private ConfirmedServiceRequest constructGetDataDefinitionRequest(ObjectReference lnRef) {

    ObjectName.DomainSpecific domainSpec = new ObjectName.DomainSpecific();
    domainSpec.setDomainID(new Identifier(lnRef.get(0).getBytes(UTF_8)));
    domainSpec.setItemID(new Identifier(lnRef.get(1).getBytes(UTF_8)));

    ObjectName objectName = new ObjectName();
    objectName.setDomainSpecific(domainSpec);

    GetVariableAccessAttributesRequest getVariableAccessAttributesRequest =
        new GetVariableAccessAttributesRequest();
    getVariableAccessAttributesRequest.setName(objectName);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setGetVariableAccessAttributes(getVariableAccessAttributesRequest);

    return confirmedServiceRequest;
  }

  private LogicalNode decodeGetDataDefinitionResponse(
      ConfirmedServiceResponse confirmedServiceResponse, ObjectReference lnRef)
      throws ServiceError {

    return DataDefinitionResParser.parseGetDataDefinitionResponse(confirmedServiceResponse, lnRef);
  }

  /**
   * The implementation of the GetDataValues ACSI service. Will send an MMS read request for the
   * given model node. After a successful return, the Basic Data Attributes of the passed model node
   * will contain the values read. If one of the Basic Data Attributes cannot be read then none of
   * the values will be read and a <code>ServiceError</code> will be thrown.
   *
   * @param modelNode the functionally constrained model node that is to be read.
   * @throws ServiceError if a ServiceError is returned by the server.
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public void getDataValues(FcModelNode modelNode) throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructGetDataValuesRequest(modelNode);
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    decodeGetDataValuesResponse(confirmedServiceResponse, modelNode);
  }

  private boolean decodeGetFileDirectoryResponse(
      ConfirmedServiceResponse confirmedServiceResponse, List<FileInformation> files)
      throws ServiceError {
    if (confirmedServiceResponse.getFileDirectory() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding GetFileDirectoryResponsePdu");
    }

    FileDirectoryResponse fileDirectoryRes = confirmedServiceResponse.getFileDirectory();

    List<DirectoryEntry> entries = fileDirectoryRes.getListOfDirectoryEntry().getDirectoryEntry();

    for (DirectoryEntry entry : entries) {
      List<BerGraphicString> graphicStrings = entry.getFileName().getBerGraphicString();

      StringBuilder filename = new StringBuilder();

      for (BerGraphicString bgs : graphicStrings) {
        filename.append(bgs.toString());
      }

      long fileSize = entry.getFileAttributes().getSizeOfFile().longValue();

      Calendar lastModified = null;

      try {

        if (entry.getFileAttributes().getLastModified() != null) {
          lastModified = entry.getFileAttributes().getLastModified().asCalendar();
        }

      } catch (ParseException e) {
        throw new ServiceError(
            ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
            "Error decoding GetFileDirectoryResponsePdu");
      }

      FileInformation fileInfo = new FileInformation(filename.toString(), fileSize, lastModified);

      files.add(fileInfo);
    }

    boolean moreFollows =
        (fileDirectoryRes.getMoreFollows() != null) && fileDirectoryRes.getMoreFollows().value;

    return moreFollows;
  }

  /**
   * Read the file directory of the server
   *
   * @param directoryName name of a directory or empty string for the root directory
   * @return the list of available
   * @throws ServiceError if a ServiceError is returned by the server or parsing of response failed.
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public List<FileInformation> getFileDirectory(String directoryName)
      throws ServiceError, IOException {
    List<FileInformation> files = new ArrayList<>();

    boolean moreFollows = true;

    String continueAfter = null;

    while (moreFollows) {

      FileDirectoryRequest fileDirectoryRequest = new FileDirectoryRequest();

      BerGraphicString berGraphicString = new BerGraphicString(directoryName.getBytes(UTF_8));

      FileName fileSpecification = new FileName();
      fileSpecification.getBerGraphicString().add(berGraphicString);

      fileDirectoryRequest.setFileSpecification(fileSpecification);

      if (continueAfter != null) {
        FileName continueAfterSpecification = new FileName();

        continueAfterSpecification
            .getBerGraphicString()
            .add(new BerGraphicString(continueAfter.getBytes(UTF_8)));

        fileDirectoryRequest.setContinueAfter(continueAfterSpecification);
      }

      ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
      confirmedServiceRequest.setFileDirectory(fileDirectoryRequest);

      ConfirmedServiceResponse confirmedServiceResponse =
          encodeWriteReadDecode(confirmedServiceRequest);

      moreFollows = decodeGetFileDirectoryResponse(confirmedServiceResponse, files);

      if (moreFollows) {
        continueAfter = files.get(files.size() - 1).getFilename();
      }
    }

    return files;
  }

  /**
   * Delete a file from the server
   *
   * @param filename name of the file to delete
   * @throws ServiceError if a ServiceError is returned by the server
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public void deleteFile(String filename) throws ServiceError, IOException {
    FileDeleteRequest fileDeleteRequest = new FileDeleteRequest();

    fileDeleteRequest.getBerGraphicString().add(new BerGraphicString(filename.getBytes(UTF_8)));

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setFileDelete(fileDeleteRequest);

    ConfirmedServiceResponse confirmedServiceResponse =
        encodeWriteReadDecode(confirmedServiceRequest);

    if (confirmedServiceResponse.getFileDelete() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding DeleteFileResponsePdu");
    }
  }

  private Integer32 openFile(String filename) throws ServiceError, IOException {
    FileOpenRequest fileOpenRequest = new FileOpenRequest();

    FileName fileSpecification = new FileName();
    fileSpecification.getBerGraphicString().add(new BerGraphicString(filename.getBytes(UTF_8)));

    fileOpenRequest.setFileName(fileSpecification);
    fileOpenRequest.setInitialPosition(new Unsigned32(0));

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setFileOpen(fileOpenRequest);

    ConfirmedServiceResponse confirmedServiceResponse =
        encodeWriteReadDecode(confirmedServiceRequest);

    if (confirmedServiceResponse.getFileOpen() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding FileOpenResponsePdu");
    }

    Integer32 frsmId = confirmedServiceResponse.getFileOpen().getFrsmID();

    return frsmId;
  }

  private boolean readNextFileDataBlock(Integer32 frsmId, GetFileListener listener)
      throws ServiceError, IOException {
    FileReadRequest fileReadRequest = new FileReadRequest(frsmId.longValue());

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setFileRead(fileReadRequest);

    ConfirmedServiceResponse confirmedServiceResponse =
        encodeWriteReadDecode(confirmedServiceRequest);

    if (confirmedServiceResponse.getFileRead() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding FileReadResponsePdu");
    }

    byte[] fileData = confirmedServiceResponse.getFileRead().getFileData().value;

    boolean moreFollows = true;

    if (confirmedServiceResponse.getFileRead().getMoreFollows() != null) {
      moreFollows = confirmedServiceResponse.getFileRead().getMoreFollows().value;
    }

    if (listener != null) {
      boolean continueRead = listener.dataReceived(fileData, moreFollows);

      if (moreFollows == true) {
        moreFollows = continueRead;
      }
    }

    return moreFollows;
  }

  private void closeFile(Integer32 frsmId) throws ServiceError, IOException {
    FileCloseRequest fileCloseRequest = new FileCloseRequest(frsmId.longValue());

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setFileClose(fileCloseRequest);

    ConfirmedServiceResponse confirmedServiceResponse =
        encodeWriteReadDecode(confirmedServiceRequest);

    if (confirmedServiceResponse.getFileClose() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding FileCloseResponsePdu");
    }
  }

  /**
   * Read a file from the server
   *
   * @param filename name of the file to delete
   * @param listener callback handler to receive fall data
   * @throws ServiceError if a ServiceError is returned by the server
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public void getFile(String filename, GetFileListener listener) throws ServiceError, IOException {
    Integer32 frsmId = openFile(filename);

    boolean moreFollows = true;

    while (moreFollows) {
      moreFollows = readNextFileDataBlock(frsmId, listener);
    }

    closeFile(frsmId);
  }

  /**
   * Will update all data inside the model except for control variables (those that have FC=CO).
   * Control variables are not meant to be read. Update is done by calling getDataValues on the
   * FCDOs below the Logical Nodes.
   *
   * @throws ServiceError if a ServiceError is returned by the server.
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public void getAllDataValues() throws ServiceError, IOException {
    for (ModelNode logicalDevice : serverModel.getChildren()) {
      for (ModelNode logicalNode : logicalDevice.getChildren()) {
        for (ModelNode dataObject : logicalNode.getChildren()) {
          FcModelNode fcdo = (FcModelNode) dataObject;
          if (fcdo.getFc() != Fc.CO && fcdo.getFc() != Fc.SE) {
            try {
              getDataValues(fcdo);
            } catch (ServiceError e) {
              throw new ServiceError(
                  e.getErrorCode(),
                  "service error retrieving "
                      + fcdo.getReference()
                      + "["
                      + fcdo.getFc()
                      + "]"
                      + ", "
                      + e.getMessage(),
                  e);
            }
          }
        }
      }
    }
  }

  private ConfirmedServiceRequest constructGetDataValuesRequest(FcModelNode modelNode) {
    VariableAccessSpecification varAccessSpec = constructVariableAccessSpecification(modelNode);

    ReadRequest readRequest = new ReadRequest();
    readRequest.setVariableAccessSpecification(varAccessSpec);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setRead(readRequest);

    return confirmedServiceRequest;
  }

  private void decodeGetDataValuesResponse(
      ConfirmedServiceResponse confirmedServiceResponse, ModelNode modelNode) throws ServiceError {

    if (confirmedServiceResponse.getRead() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding GetDataValuesReponsePdu");
    }

    List<AccessResult> listOfAccessResults =
        confirmedServiceResponse.getRead().getListOfAccessResult().getAccessResult();

    if (listOfAccessResults.size() != 1) {
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "Multiple results received.");
    }

    AccessResult accRes = listOfAccessResults.get(0);

    if (accRes.getFailure() != null) {
      throw mmsDataAccessErrorToServiceError(accRes.getFailure());
    }
    modelNode.setValueFromMmsDataObj(accRes.getSuccess());
  }

  /**
   * The implementation of the SetDataValues ACSI service. Will send an MMS write request with the
   * values of all Basic Data Attributes of the given model node. Will simply return if all values
   * have been successfully written. If one of the Basic Data Attributes could not be written then a
   * <code>ServiceError</code> will be thrown. In this case it is not possible to find out which of
   * several Basic Data Attributes could not be written.
   *
   * @param modelNode the functionally constrained model node that is to be written.
   * @throws ServiceError if a ServiceError is returned by the server.
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public void setDataValues(FcModelNode modelNode) throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructSetDataValuesRequest(modelNode);
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    decodeSetDataValuesResponse(confirmedServiceResponse);
  }

  private ConfirmedServiceRequest constructSetDataValuesRequest(FcModelNode modelNode)
      throws ServiceError {

    VariableAccessSpecification variableAccessSpecification =
        constructVariableAccessSpecification(modelNode);

    ListOfData listOfData = new ListOfData();
    List<Data> dataList = listOfData.getData();
    dataList.add(modelNode.getMmsDataObj());

    WriteRequest writeRequest = new WriteRequest();
    writeRequest.setListOfData(listOfData);
    writeRequest.setVariableAccessSpecification(variableAccessSpecification);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setWrite(writeRequest);

    return confirmedServiceRequest;
  }

  private VariableAccessSpecification constructVariableAccessSpecification(FcModelNode modelNode) {
    VariableDefs listOfVariable = new VariableDefs();

    List<VariableDefs.SEQUENCE> variableDefsSeqOf = listOfVariable.getSEQUENCE();
    variableDefsSeqOf.add(modelNode.getMmsVariableDef());

    VariableAccessSpecification variableAccessSpecification = new VariableAccessSpecification();
    variableAccessSpecification.setListOfVariable(listOfVariable);

    return variableAccessSpecification;
  }

  private void decodeSetDataValuesResponse(ConfirmedServiceResponse confirmedServiceResponse)
      throws ServiceError {

    WriteResponse writeResponse = confirmedServiceResponse.getWrite();

    if (writeResponse == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "SetDataValuesResponse: improper response");
    }

    WriteResponse.CHOICE subChoice = writeResponse.getCHOICE().get(0);

    if (subChoice.getFailure() != null) {
      throw mmsDataAccessErrorToServiceError(subChoice.getFailure());
    }
  }

  /**
   * This function will get the definition of all persistent DataSets from the server and update the
   * DataSets in the ServerModel that was returned by {@code retrieveModel} or set using {@code
   * setServerModel}. It will delete DataSets that have been deleted since the last update and add
   * any new DataSets
   *
   * @throws ServiceError if a ServiceError is returned by the server.
   * @throws IOException if a fatal association error occurs. The association object will be closed
   *     and can no longer be used after this exception is thrown.
   */
  public void updateDataSets() throws ServiceError, IOException {

    if (serverModel == null) {
      throw new IllegalStateException(
          "Before calling this function you have to get the ServerModel using the retrieveModel() function");
    }

    Collection<ModelNode> lds = serverModel.getChildren();

    for (ModelNode ld : lds) {
      ConfirmedServiceRequest serviceRequest =
          constructGetDirectoryRequest(ld.getName(), "", false);
      ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
      decodeAndRetrieveDsNamesAndDefinitions(confirmedServiceResponse, (LogicalDevice) ld);
    }
  }

  private void decodeAndRetrieveDsNamesAndDefinitions(
      ConfirmedServiceResponse confirmedServiceResponse, LogicalDevice ld)
      throws ServiceError, IOException {

    if (confirmedServiceResponse.getGetNameList() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "decodeGetDataSetResponse: Error decoding server response");
    }

    GetNameListResponse getNameListResponse = confirmedServiceResponse.getGetNameList();

    List<Identifier> identifiers = getNameListResponse.getListOfIdentifier().getIdentifier();

    if (identifiers.size() == 0) {
      return;
    }

    for (Identifier identifier : identifiers) {
      // TODO delete DataSets that no longer exist
      getDataSetDirectory(identifier, ld);
    }

    if (getNameListResponse.getMoreFollows() != null
        && getNameListResponse.getMoreFollows().value == true) {
      throw new ServiceError(ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT);
    }
  }

  private void getDataSetDirectory(Identifier dsId, LogicalDevice ld)
      throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructGetDataSetDirectoryRequest(dsId, ld);
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    decodeGetDataSetDirectoryResponse(confirmedServiceResponse, dsId, ld);
  }

  private ConfirmedServiceRequest constructGetDataSetDirectoryRequest(
      Identifier dsId, LogicalDevice ld) throws ServiceError {
    ObjectName.DomainSpecific domainSpecificObjectName = new ObjectName.DomainSpecific();
    domainSpecificObjectName.setDomainID(new Identifier(ld.getName().getBytes(UTF_8)));
    domainSpecificObjectName.setItemID(dsId);

    GetNamedVariableListAttributesRequest dataSetObj = new GetNamedVariableListAttributesRequest();
    dataSetObj.setDomainSpecific(domainSpecificObjectName);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setGetNamedVariableListAttributes(dataSetObj);

    return confirmedServiceRequest;
  }

  private void decodeGetDataSetDirectoryResponse(
      ConfirmedServiceResponse confirmedServiceResponse, BerVisibleString dsId, LogicalDevice ld)
      throws ServiceError {

    if (confirmedServiceResponse.getGetNamedVariableListAttributes() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "decodeGetDataSetDirectoryResponse: Error decoding server response");
    }

    GetNamedVariableListAttributesResponse getNamedVariableListAttResponse =
        confirmedServiceResponse.getGetNamedVariableListAttributes();
    boolean deletable = getNamedVariableListAttResponse.getMmsDeletable().value;
    List<VariableDefs.SEQUENCE> variables =
        getNamedVariableListAttResponse.getListOfVariable().getSEQUENCE();

    if (variables.size() == 0) {
      throw new ServiceError(
          ServiceError.INSTANCE_NOT_AVAILABLE,
          "decodeGetDataSetDirectoryResponse: Instance not available");
    }

    List<FcModelNode> dsMems = new ArrayList<>();

    for (VariableDefs.SEQUENCE variableDef : variables) {

      FcModelNode member;
      // TODO remove this try catch statement once all possible FCs are
      // supported
      // it is only there so that Functional Constraints such as GS will
      // be ignored and DataSet cotaining elements with these FCs are
      // ignored and not created.
      try {
        member = serverModel.getNodeFromVariableDef(variableDef);
      } catch (ServiceError e) {
        return;
      }
      if (member == null) {
        throw new ServiceError(
            ServiceError.INSTANCE_NOT_AVAILABLE,
            "decodeGetDataSetDirectoryResponse: data set memeber does not exist, you might have to call retrieveModel first");
      }
      dsMems.add(member);
    }

    String dsObjRef = ld.getName() + "/" + dsId.toString().replace('$', '.');

    DataSet dataSet = new DataSet(dsObjRef, dsMems, deletable);

    if (ld.getChild(dsId.toString().substring(0, dsId.toString().indexOf('$'))) == null) {
      throw new ServiceError(
          ServiceError.INSTANCE_NOT_AVAILABLE,
          "decodeGetDataSetDirectoryResponse: LN for returned DataSet is not available");
    }

    DataSet existingDs = serverModel.getDataSet(dsObjRef);
    if (existingDs == null) {
      serverModel.addDataSet(dataSet);
    } else if (!existingDs.isDeletable()) {
      return;
    } else {
      serverModel.removeDataSet(dsObjRef);
      serverModel.addDataSet(dataSet);
    }
  }

  /**
   * The client should create the data set first and add it to either the non-persistent list or to
   * the model. Then it should call this method for creation on the server side
   *
   * @param dataSet the data set to be created on the server side
   * @throws ServiceError if a ServiceError is returned by the server.
   * @throws IOException if a fatal IO error occurs. The association object will be closed and can
   *     no longer be used after this exception is thrown.
   */
  public void createDataSet(DataSet dataSet) throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructCreateDataSetRequest(dataSet);
    encodeWriteReadDecode(serviceRequest);
    handleCreateDataSetResponse(dataSet);
  }

  /**
   * dsRef = either LD/LN.DataSetName (persistent) or @DataSetname (non-persistent) Names in
   * dsMemberRef should be in the form: LD/LNName.DoName or LD/LNName.DoName.DaName
   */
  private ConfirmedServiceRequest constructCreateDataSetRequest(DataSet dataSet)
      throws ServiceError {

    VariableDefs listOfVariable = new VariableDefs();

    List<VariableDefs.SEQUENCE> variableDefs = listOfVariable.getSEQUENCE();
    for (FcModelNode dsMember : dataSet) {
      variableDefs.add(dsMember.getMmsVariableDef());
    }

    DefineNamedVariableListRequest createDSRequest = new DefineNamedVariableListRequest();
    createDSRequest.setVariableListName(dataSet.getMmsObjectName());
    createDSRequest.setListOfVariable(listOfVariable);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setDefineNamedVariableList(createDSRequest);

    return confirmedServiceRequest;
  }

  private void handleCreateDataSetResponse(DataSet dataSet) throws ServiceError {
    serverModel.addDataSet(dataSet);
  }

  public void deleteDataSet(DataSet dataSet) throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructDeleteDataSetRequest(dataSet);
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    decodeDeleteDataSetResponse(confirmedServiceResponse, dataSet);
  }

  private ConfirmedServiceRequest constructDeleteDataSetRequest(DataSet dataSet)
      throws ServiceError {

    ListOfVariableListName listOfVariableListName = new ListOfVariableListName();

    List<ObjectName> objectList = listOfVariableListName.getObjectName();
    objectList.add(dataSet.getMmsObjectName());

    DeleteNamedVariableListRequest requestDeleteDS = new DeleteNamedVariableListRequest();
    requestDeleteDS.setListOfVariableListName(listOfVariableListName);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setDeleteNamedVariableList(requestDeleteDS);

    return confirmedServiceRequest;
  }

  private void decodeDeleteDataSetResponse(
      ConfirmedServiceResponse confirmedServiceResponse, DataSet dataSet) throws ServiceError {

    if (confirmedServiceResponse.getDeleteNamedVariableList() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "decodeDeleteDataSetResponse: Error decoding server response");
    }

    DeleteNamedVariableListResponse deleteNamedVariableListResponse =
        confirmedServiceResponse.getDeleteNamedVariableList();

    if (deleteNamedVariableListResponse.getNumberDeleted().intValue() != 1) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT, "number deleted not 1");
    }

    if (serverModel.removeDataSet(dataSet.getReferenceStr()) == null) {
      throw new ServiceError(ServiceError.UNKNOWN, "unable to delete dataset locally");
    }
  }

  /**
   * The implementation of the GetDataSetValues ACSI service. After a successful return, the Basic
   * Data Attributes of the data set members will contain the values read. If one of the data set
   * members could not be read, this will be indicated in the returned list. The returned list will
   * have the same size as the member list of the data set. For each member it will contain <code>
   * null</code> if reading was successful and a ServiceError if reading of this member failed.
   *
   * @param dataSet the DataSet that is to be read.
   * @return a list indicating ServiceErrors that may have occurred.
   * @throws IOException if a fatal IO error occurs. The association object will be closed and can
   *     no longer be used after this exception is thrown.
   */
  public List<ServiceError> getDataSetValues(DataSet dataSet) throws IOException {

    ConfirmedServiceResponse confirmedServiceResponse;
    try {
      ConfirmedServiceRequest serviceRequest = constructGetDataSetValuesRequest(dataSet);
      confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    } catch (ServiceError e) {
      int dataSetSize = dataSet.getMembers().size();
      List<ServiceError> serviceErrors = new ArrayList<>(dataSetSize);
      for (int i = 0; i < dataSetSize; i++) {
        serviceErrors.add(e);
      }
      return serviceErrors;
    }
    return decodeGetDataSetValuesResponse(confirmedServiceResponse, dataSet);
  }

  private ConfirmedServiceRequest constructGetDataSetValuesRequest(DataSet dataSet)
      throws ServiceError {

    VariableAccessSpecification varAccSpec = new VariableAccessSpecification();
    varAccSpec.setVariableListName(dataSet.getMmsObjectName());

    ReadRequest getDataSetValuesRequest = new ReadRequest();
    getDataSetValuesRequest.setSpecificationWithResult(new BerBoolean(true));
    getDataSetValuesRequest.setVariableAccessSpecification(varAccSpec);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setRead(getDataSetValuesRequest);

    return confirmedServiceRequest;
  }

  private List<ServiceError> decodeGetDataSetValuesResponse(
      ConfirmedServiceResponse confirmedServiceResponse, DataSet ds) {

    int dataSetSize = ds.getMembers().size();
    List<ServiceError> serviceErrors = new ArrayList<>(dataSetSize);

    if (confirmedServiceResponse.getRead() == null) {
      ServiceError serviceError =
          new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "Error decoding GetDataValuesReponsePdu");
      for (int i = 0; i < dataSetSize; i++) {
        serviceErrors.add(serviceError);
      }
      return serviceErrors;
    }

    ReadResponse readResponse = confirmedServiceResponse.getRead();
    List<AccessResult> listOfAccessResults = readResponse.getListOfAccessResult().getAccessResult();

    if (listOfAccessResults.size() != ds.getMembers().size()) {
      ServiceError serviceError =
          new ServiceError(
              ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
              "Number of AccessResults does not match the number of DataSet members.");
      for (int i = 0; i < dataSetSize; i++) {
        serviceErrors.add(serviceError);
      }
      return serviceErrors;
    }

    Iterator<AccessResult> accessResultIterator = listOfAccessResults.iterator();

    for (FcModelNode dsMember : ds) {
      AccessResult accessResult = accessResultIterator.next();
      if (accessResult.getSuccess() != null) {
        try {
          dsMember.setValueFromMmsDataObj(accessResult.getSuccess());
        } catch (ServiceError e) {
          serviceErrors.add(e);
        }
        serviceErrors.add(null);
      } else {
        serviceErrors.add(mmsDataAccessErrorToServiceError(accessResult.getFailure()));
      }
    }

    return serviceErrors;
  }

  public List<ServiceError> setDataSetValues(DataSet dataSet) throws ServiceError, IOException {
    ConfirmedServiceRequest serviceRequest = constructSetDataSetValues(dataSet);
    ConfirmedServiceResponse confirmedServiceResponse = encodeWriteReadDecode(serviceRequest);
    return decodeSetDataSetValuesResponse(confirmedServiceResponse);
  }

  private ConfirmedServiceRequest constructSetDataSetValues(DataSet dataSet) throws ServiceError {
    VariableAccessSpecification varAccessSpec = new VariableAccessSpecification();
    varAccessSpec.setVariableListName(dataSet.getMmsObjectName());

    ListOfData listOfData = new ListOfData();
    List<Data> dataList = listOfData.getData();

    for (ModelNode member : dataSet) {
      dataList.add(member.getMmsDataObj());
    }

    WriteRequest writeRequest = new WriteRequest();
    writeRequest.setVariableAccessSpecification(varAccessSpec);
    writeRequest.setListOfData(listOfData);

    ConfirmedServiceRequest confirmedServiceRequest = new ConfirmedServiceRequest();
    confirmedServiceRequest.setWrite(writeRequest);

    return confirmedServiceRequest;
  }

  private List<ServiceError> decodeSetDataSetValuesResponse(
      ConfirmedServiceResponse confirmedServiceResponse) throws ServiceError {

    if (confirmedServiceResponse.getWrite() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Error decoding SetDataSetValuesReponsePdu");
    }

    WriteResponse writeResponse = confirmedServiceResponse.getWrite();
    List<WriteResponse.CHOICE> writeResChoiceType = writeResponse.getCHOICE();
    List<ServiceError> serviceErrors = new ArrayList<>(writeResChoiceType.size());

    for (WriteResponse.CHOICE accessResult : writeResChoiceType) {
      if (accessResult.getSuccess() != null) {
        serviceErrors.add(null);
      } else {
        serviceErrors.add(mmsDataAccessErrorToServiceError(accessResult.getFailure()));
      }
    }
    return serviceErrors;
  }

  public void getRcbValues(Rcb rcb) throws ServiceError, IOException {
    getDataValues(rcb);
  }

  public void reserveUrcb(Urcb urcb) throws ServiceError, IOException {
    BdaBoolean resvBda = urcb.getResv();
    resvBda.setValue(true);
    setDataValues(resvBda);
  }

  public void reserveBrcb(Brcb brcb, short resvTime) throws ServiceError, IOException {
    BdaInt16 resvTmsBda = brcb.getResvTms();
    resvTmsBda.setValue(resvTime);
    setDataValues(resvTmsBda);
  }

  public void cancelUrcbReservation(Urcb urcb) throws ServiceError, IOException {
    BdaBoolean resvBda = urcb.getResv();
    resvBda.setValue(false);
    setDataValues(resvBda);
  }

  public void enableReporting(Rcb rcb) throws ServiceError, IOException {
    BdaBoolean rptEnaBda = rcb.getRptEna();
    rptEnaBda.setValue(true);
    setDataValues(rptEnaBda);
  }

  public void disableReporting(Rcb rcb) throws ServiceError, IOException {
    BdaBoolean rptEnaBda = rcb.getRptEna();
    rptEnaBda.setValue(false);
    setDataValues(rptEnaBda);
  }

  public void startGi(Rcb rcb) throws ServiceError, IOException {
    BdaBoolean rptGiBda = (BdaBoolean) rcb.getChild("GI");
    rptGiBda.setValue(true);
    setDataValues(rptGiBda);
  }

  /**
   * Sets the selected values of the given report control block. Note that all these parameters may
   * only be set if the RCB has been reserved but reporting has not been enabled yet.
   *
   * <p>The data set reference as it is set in an RCB must contain a dollar sign instead of a dot to
   * separate the logical node from the data set name, e.g.: 'LDevice1/LNode$DataSetName'. Therefore
   * his method will check the reference for a dot and if necessary convert it to a '$' sign before
   * sending the request to the server.
   *
   * <p>The parameters PurgeBuf, EntryId are only applicable if the given rcb is of type BRCB.
   *
   * @param rcb the report control block
   * @param setRptId whether to set the report ID
   * @param setDatSet whether to set the data set
   * @param setOptFlds whether to set the optional fields
   * @param setBufTm whether to set the buffer time
   * @param setTrgOps whether to set the trigger options
   * @param setIntgPd whether to set the integrity period
   * @param setPurgeBuf whether to set purge buffer
   * @param setEntryId whether to set the entry ID
   * @return a list indicating ServiceErrors that may have occurred.
   * @throws IOException if a fatal IO error occurs. The association object will be closed and can
   *     no longer be used after this exception is thrown.
   */
  public List<ServiceError> setRcbValues(
      Rcb rcb,
      boolean setRptId,
      boolean setDatSet,
      boolean setOptFlds,
      boolean setBufTm,
      boolean setTrgOps,
      boolean setIntgPd,
      boolean setPurgeBuf,
      boolean setEntryId)
      throws IOException {

    List<FcModelNode> parametersToSet = new ArrayList<>(6);

    if (setRptId == true) {
      parametersToSet.add(rcb.getRptId());
    }
    if (setDatSet == true) {
      rcb.getDatSet().setValue(rcb.getDatSet().getStringValue().replace('.', '$'));
      parametersToSet.add(rcb.getDatSet());
    }
    if (setOptFlds == true) {
      parametersToSet.add(rcb.getOptFlds());
    }
    if (setBufTm == true) {
      parametersToSet.add(rcb.getBufTm());
    }
    if (setTrgOps == true) {
      parametersToSet.add(rcb.getTrgOps());
    }
    if (setIntgPd == true) {
      parametersToSet.add(rcb.getIntgPd());
    }
    if (rcb instanceof Brcb) {
      Brcb brcb = (Brcb) rcb;
      if (setPurgeBuf == true) {
        parametersToSet.add(brcb.getPurgeBuf());
      }
      if (setEntryId == true) {
        parametersToSet.add(brcb.getEntryId());
      }
    }

    List<ServiceError> serviceErrors = new ArrayList<>(parametersToSet.size());

    for (FcModelNode child : parametersToSet) {
      try {
        setDataValues(child);
        serviceErrors.add(null);
      } catch (ServiceError e) {
        serviceErrors.add(e);
      }
    }

    return serviceErrors;
  }

  private Report processReport(MMSpdu mmsPdu) throws ServiceError {

    if (mmsPdu.getUnconfirmedPDU() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "getReport: Error decoding server response");
    }

    UnconfirmedPDU unconfirmedRes = mmsPdu.getUnconfirmedPDU();

    if (unconfirmedRes.getService() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "getReport: Error decoding server response");
    }

    UnconfirmedService unconfirmedServ = unconfirmedRes.getService();

    if (unconfirmedServ.getInformationReport() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "getReport: Error decoding server response");
    }

    List<AccessResult> listRes =
        unconfirmedServ.getInformationReport().getListOfAccessResult().getAccessResult();

    int index = 0;

    if (listRes.get(index).getSuccess().getVisibleString() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "processReport: report does not contain RptID");
    }

    String rptId = listRes.get(index++).getSuccess().getVisibleString().toString();

    if (listRes.get(index).getSuccess().getBitString() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "processReport: report does not contain OptFlds");
    }

    BdaOptFlds optFlds = new BdaOptFlds(new ObjectReference("none"), null);
    optFlds.setValue(listRes.get(index++).getSuccess().getBitString().value);

    Integer sqNum = null;
    if (optFlds.isSequenceNumber()) {
      sqNum = listRes.get(index++).getSuccess().getUnsigned().intValue();
    }

    BdaEntryTime timeOfEntry = null;
    if (optFlds.isReportTimestamp()) {
      timeOfEntry = new BdaEntryTime(new ObjectReference("none"), null, "", false, false);
      timeOfEntry.setValueFromMmsDataObj(listRes.get(index++).getSuccess());
    }

    String dataSetRef = null;
    if (optFlds.isDataSetName()) {
      dataSetRef = listRes.get(index++).getSuccess().getVisibleString().toString();
    } else {
      for (Urcb urcb : serverModel.getUrcbs()) {
        if ((urcb.getRptId() != null && urcb.getRptId().getStringValue().equals(rptId))
            || urcb.getReference().toString().equals(rptId)) {
          dataSetRef = urcb.getDatSet().getStringValue();
          break;
        }
      }
      if (dataSetRef == null) {
        for (Brcb brcb : serverModel.getBrcbs()) {
          if ((brcb.getRptId() != null && brcb.getRptId().getStringValue().equals(rptId))
              || brcb.getReference().toString().equals(rptId)) {
            dataSetRef = brcb.getDatSet().getStringValue();
            break;
          }
        }
      }
    }
    if (dataSetRef == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "unable to find RCB that matches the given RptID in the report.");
    }
    dataSetRef = dataSetRef.replace('$', '.');

    DataSet dataSet = serverModel.getDataSet(dataSetRef);
    if (dataSet == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "unable to find data set that matches the given data set reference of the report.");
    }

    Boolean bufOvfl = null;
    if (optFlds.isBufferOverflow()) {
      bufOvfl = listRes.get(index++).getSuccess().getBool().value;
    }

    BdaOctetString entryId = null;
    if (optFlds.isEntryId()) {
      entryId = new BdaOctetString(new ObjectReference("none"), null, "", 8, false, false);
      entryId.setValue(listRes.get(index++).getSuccess().getOctetString().value);
    }

    Long confRev = null;
    if (optFlds.isConfigRevision()) {
      confRev = listRes.get(index++).getSuccess().getUnsigned().longValue();
    }

    Integer subSqNum = null;
    boolean moreSegmentsFollow = false;
    if (optFlds.isSegmentation()) {
      subSqNum = listRes.get(index++).getSuccess().getUnsigned().intValue();
      moreSegmentsFollow = listRes.get(index++).getSuccess().getBool().value;
    }

    boolean[] inclusionBitString =
        listRes.get(index++).getSuccess().getBitString().getValueAsBooleans();
    int numMembersReported = 0;
    for (boolean bit : inclusionBitString) {
      if (bit) {
        numMembersReported++;
      }
    }

    if (optFlds.isDataReference()) {
      // this is just to move the index to the right place
      // The next part will process the changes to the values
      // without the dataRefs
      index += numMembersReported;
    }

    List<FcModelNode> reportedDataSetMembers = new ArrayList<>(numMembersReported);
    int dataSetIndex = 0;
    for (FcModelNode dataSetMember : dataSet.getMembers()) {
      if (inclusionBitString[dataSetIndex]) {
        AccessResult accessRes = listRes.get(index++);
        FcModelNode dataSetMemberCopy = (FcModelNode) dataSetMember.copy();
        dataSetMemberCopy.setValueFromMmsDataObj(accessRes.getSuccess());
        reportedDataSetMembers.add(dataSetMemberCopy);
      }
      dataSetIndex++;
    }

    List<BdaReasonForInclusion> reasonCodes = null;
    if (optFlds.isReasonForInclusion()) {
      reasonCodes = new ArrayList<>(dataSet.getMembers().size());
      for (int i = 0; i < dataSet.getMembers().size(); i++) {
        if (inclusionBitString[i]) {
          BdaReasonForInclusion reasonForInclusion = new BdaReasonForInclusion(null);
          reasonCodes.add(reasonForInclusion);
          byte[] reason = listRes.get(index++).getSuccess().getBitString().value;
          reasonForInclusion.setValue(reason);
        }
      }
    }

    return new Report(
        rptId,
        sqNum,
        subSqNum,
        moreSegmentsFollow,
        dataSetRef,
        bufOvfl,
        confRev,
        timeOfEntry,
        entryId,
        inclusionBitString,
        reportedDataSetMembers,
        reasonCodes);
  }

  /**
   * Performs the Select ACSI Service of the control model on the given controllable Data Object
   * (DO). By selecting a controllable DO you can reserve it for exclusive control/operation. This
   * service is only applicable if the ctlModel Data Attribute is set to "sbo-with-normal-security"
   * (2).
   *
   * <p>The selection is canceled in one of the following events:
   *
   * <ul>
   *   <li>The "Cancel" ACSI service is issued.
   *   <li>The sboTimemout (select before operate timeout) runs out. If the given controlDataObject
   *       contains a sboTimeout Data Attribute it is possible to change the timeout after which the
   *       selection/reservation is automatically canceled by the server. Otherwise the timeout is a
   *       local issue of the server.
   *   <li>The connection to the server is closed.
   *   <li>An operate service failed because of some error
   *   <li>The sboClass is set to "operate-once" then the selection is also canceled after a
   *       successful operate service.
   * </ul>
   *
   * @param controlDataObject needs to be a controllable Data Object that contains a Data Attribute
   *     named "SBO".
   * @return false if the selection/reservation was not successful (because it is already selected
   *     by another client). Otherwise true is returned.
   * @throws ServiceError if a ServiceError is returned by the server.
   * @throws IOException if a fatal IO error occurs. The association object will be closed and can
   *     no longer be used after this exception is thrown.
   */
  public boolean select(FcModelNode controlDataObject) throws ServiceError, IOException {
    BdaVisibleString sbo;
    try {
      sbo = (BdaVisibleString) controlDataObject.getChild("SBO");
    } catch (Exception e) {
      throw new IllegalArgumentException(
          "ModelNode needs to conain a child node named SBO in order to select");
    }

    getDataValues(sbo);

    return sbo.getValue().length != 0;
  }

  /**
   * Executes the Operate ACSI Service on the given controllable Data Object (DO). The following
   * subnodes of the given control DO should be set according your needs before calling this
   * function. (Note that you can probably leave most attributes with their default value):
   *
   * <ul>
   *   <li>Oper.ctlVal - has to be set to actual control value that is to be written using the
   *       operate service.
   *   <li>Oper.operTm (type: BdaTimestamp) - is an optional sub data attribute of Oper (thus it may
   *       not exist). If it exists it can be used to set the timestamp when the operation shall be
   *       performed by the server. Thus the server will delay execution of the operate command
   *       until the given date is reached. Can be set to an empty byte array (new byte[0]) or null
   *       so that the server executes the operate command immediately. This is also the default.
   *   <li>Oper.check (type: BdaCheck) is used to tell the server whether to perform the
   *       synchrocheck and interlockcheck. By default they are turned off.
   *   <li>Oper.orign - contains the two data attributes orCat (origin category, type: BdaInt8) and
   *       orIdent (origin identifier, type BdaOctetString). Origin is optionally reflected in the
   *       status Data Attribute controlDO.origin. By reading this data attribute other clients can
   *       see who executed the last operate command. The default value for orCat is 0
   *       ("not-supported") and the default value for orIdent is ""(the empty string).
   *   <li>Oper.Test (BdaBoolean) - if true this operate command is sent for test purposes only.
   *       Default is false.
   * </ul>
   *
   * All other operate parameters are automatically handled by this function.
   *
   * @param controlDataObject needs to be a controllable Data Object that contains a Data Attribute
   *     named "Oper".
   * @throws ServiceError if a ServiceError is returned by the server
   * @throws IOException if a fatal IO error occurs. The association object will be closed and can
   *     no longer be used after this exception is thrown.
   */
  public void operate(FcModelNode controlDataObject) throws ServiceError, IOException {
    ConstructedDataAttribute oper;
    try {
      oper = (ConstructedDataAttribute) controlDataObject.getChild("Oper");
    } catch (Exception e) {
      throw new IllegalArgumentException("ModelNode needs to conain a child node named \"Oper\".");
    }

    ((BdaInt8U) oper.getChild("ctlNum")).setValue((short) 1);
    ((BdaTimestamp) oper.getChild("T")).setInstant(Instant.now());

    setDataValues(oper);
  }

  public boolean isOpen() {
    return !closed;
  }

  /** Will close the connection simply by closing the TCP socket. */
  public void close() {
    clientReceiver.close(new IOException("Connection closed by client"));
  }

  /** Will send a disconnect request first and then close the TCP socket. */
  public void disconnect() {
    clientReceiver.disconnect();
  }

  final class ClientReceiver extends Thread {

    private final ByteBuffer pduBuffer;
    private Integer expectedResponseId;
    private IOException lastIOException = null;

    public ClientReceiver(int maxMmsPduSize) {
      pduBuffer = ByteBuffer.allocate(maxMmsPduSize + 400);
    }

    @Override
    public void run() {
      try {
        while (true) {

          pduBuffer.clear();
          byte[] buffer;
          try {
            buffer = acseAssociation.receive(pduBuffer);
          } catch (TimeoutException e) {
            // Illegal state: A timeout exception was thrown.
            throw new IllegalStateException();
          } catch (DecodingException e) {
            // Error decoding the OSI headers of the received packet
            continue;
          }

          MMSpdu decodedResponsePdu = new MMSpdu();
          try {
            decodedResponsePdu.decode(new ByteArrayInputStream(buffer), null);
          } catch (IOException e) {
            // Error decoding the received MMS PDU
            continue;
          }

          if (decodedResponsePdu.getUnconfirmedPDU() != null) {
            if (decodedResponsePdu
                    .getUnconfirmedPDU()
                    .getService()
                    .getInformationReport()
                    .getVariableAccessSpecification()
                    .getListOfVariable()
                != null) {
              // Discarding LastApplError Report
            } else {
              if (reportListener != null) {
                final Report report = processReport(decodedResponsePdu);

                Thread t1 =
                    new Thread(
                        new Runnable() {
                          @Override
                          public void run() {
                            reportListener.newReport(report);
                          }
                        });
                t1.start();
              } else {
                // discarding report because no ReportListener was registered.
              }
            }
          } else if (decodedResponsePdu.getRejectPDU() != null) {
            synchronized (incomingResponses) {
              if (expectedResponseId == null) {
                // Discarding Reject MMS PDU because no listener for request was found.
                continue;
              } else if (decodedResponsePdu.getRejectPDU().getOriginalInvokeID().value.intValue()
                  != expectedResponseId) {
                // Discarding Reject MMS PDU because no listener with fitting invokeID was found.
                continue;
              } else {
                try {
                  incomingResponses.put(decodedResponsePdu);
                } catch (InterruptedException e) {
                  // TODO can this ever be interrupted?
                }
              }
            }
          } else if (decodedResponsePdu.getConfirmedErrorPDU() != null) {
            synchronized (incomingResponses) {
              if (expectedResponseId == null) {
                // Discarding ConfirmedError MMS PDU because no listener for request was found.
                continue;
              } else if (decodedResponsePdu.getConfirmedErrorPDU().getInvokeID().value.intValue()
                  != expectedResponseId) {
                // Discarding ConfirmedError MMS PDU because no listener with fitting invokeID was
                // found.
                continue;
              } else {
                try {
                  incomingResponses.put(decodedResponsePdu);
                } catch (InterruptedException e) {
                  // TODO can this ever be interrupted?
                }
              }
            }
          } else {
            synchronized (incomingResponses) {
              if (expectedResponseId == null) {
                // Discarding ConfirmedResponse MMS PDU because no listener for request was found.
                continue;
              } else if (decodedResponsePdu.getConfirmedResponsePDU().getInvokeID().value.intValue()
                  != expectedResponseId) {
                // Discarding ConfirmedResponse MMS PDU because no listener with fitting invokeID
                // was
                // found.
                continue;
              } else {
                try {
                  incomingResponses.put(decodedResponsePdu);
                } catch (InterruptedException e) {
                  // TODO can this ever be interrupted?
                }
              }
            }
          }
        }
      } catch (IOException e) {
        close(e);
      } catch (Exception e) {
        close(new IOException("unexpected exception while receiving", e));
      }
    }

    public void setResponseExpected(int invokeId) {
      expectedResponseId = invokeId;
    }

    private void disconnect() {
      synchronized (this) {
        if (closed == false) {
          closed = true;
          acseAssociation.disconnect();
          lastIOException = new IOException("Connection disconnected by client");
          if (reportListener != null) {
            Thread t1 =
                new Thread(
                    new Runnable() {
                      @Override
                      public void run() {
                        reportListener.associationClosed(lastIOException);
                      }
                    });
            t1.start();
          }

          MMSpdu mmsPdu = new MMSpdu();
          mmsPdu.setConfirmedRequestPDU(new ConfirmedRequestPDU());
          try {
            incomingResponses.put(mmsPdu);
          } catch (InterruptedException e1) {
            // TODO can this ever be interrupted?
          }
        }
      }
    }

    private void close(IOException e) {
      synchronized (this) {
        if (closed == false) {
          closed = true;
          acseAssociation.close();
          lastIOException = e;
          Thread t1 =
              new Thread(
                  new Runnable() {
                    @Override
                    public void run() {
                      reportListener.associationClosed(lastIOException);
                    }
                  });
          t1.start();

          MMSpdu mmsPdu = new MMSpdu();
          mmsPdu.setConfirmedRequestPDU(new ConfirmedRequestPDU());
          try {
            incomingResponses.put(mmsPdu);
          } catch (InterruptedException e1) {
            // TODO can this ever be interrupted?
          }
        }
      }
    }

    IOException getLastIOException() {
      return lastIOException;
    }

    MMSpdu removeExpectedResponse() {
      synchronized (incomingResponses) {
        expectedResponseId = null;
        return incomingResponses.poll();
      }
    }
  }
}

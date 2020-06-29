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
import com.beanit.asn1bean.ber.types.string.BerVisibleString;
import com.beanit.iec61850bean.internal.BerBoolean;
import com.beanit.iec61850bean.internal.NamedThreadFactory;
import com.beanit.iec61850bean.internal.mms.asn1.AccessResult;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedErrorPDU;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedRequestPDU;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedResponsePDU;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedServiceRequest;
import com.beanit.iec61850bean.internal.mms.asn1.ConfirmedServiceResponse;
import com.beanit.iec61850bean.internal.mms.asn1.Data;
import com.beanit.iec61850bean.internal.mms.asn1.DataAccessError;
import com.beanit.iec61850bean.internal.mms.asn1.DefineNamedVariableListRequest;
import com.beanit.iec61850bean.internal.mms.asn1.DefineNamedVariableListResponse;
import com.beanit.iec61850bean.internal.mms.asn1.DeleteNamedVariableListRequest;
import com.beanit.iec61850bean.internal.mms.asn1.DeleteNamedVariableListResponse;
import com.beanit.iec61850bean.internal.mms.asn1.GetNameListRequest;
import com.beanit.iec61850bean.internal.mms.asn1.GetNameListResponse;
import com.beanit.iec61850bean.internal.mms.asn1.GetNameListResponse.ListOfIdentifier;
import com.beanit.iec61850bean.internal.mms.asn1.GetNamedVariableListAttributesResponse;
import com.beanit.iec61850bean.internal.mms.asn1.GetVariableAccessAttributesRequest;
import com.beanit.iec61850bean.internal.mms.asn1.GetVariableAccessAttributesResponse;
import com.beanit.iec61850bean.internal.mms.asn1.Identifier;
import com.beanit.iec61850bean.internal.mms.asn1.InitiateRequestPDU;
import com.beanit.iec61850bean.internal.mms.asn1.InitiateResponsePDU;
import com.beanit.iec61850bean.internal.mms.asn1.Integer16;
import com.beanit.iec61850bean.internal.mms.asn1.Integer32;
import com.beanit.iec61850bean.internal.mms.asn1.Integer8;
import com.beanit.iec61850bean.internal.mms.asn1.MMSpdu;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectName;
import com.beanit.iec61850bean.internal.mms.asn1.ObjectName.DomainSpecific;
import com.beanit.iec61850bean.internal.mms.asn1.ParameterSupportOptions;
import com.beanit.iec61850bean.internal.mms.asn1.ReadRequest;
import com.beanit.iec61850bean.internal.mms.asn1.ReadResponse;
import com.beanit.iec61850bean.internal.mms.asn1.ReadResponse.ListOfAccessResult;
import com.beanit.iec61850bean.internal.mms.asn1.ServiceError.ErrorClass;
import com.beanit.iec61850bean.internal.mms.asn1.ServiceSupportOptions;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription.Structure;
import com.beanit.iec61850bean.internal.mms.asn1.TypeDescription.Structure.Components;
import com.beanit.iec61850bean.internal.mms.asn1.TypeSpecification;
import com.beanit.iec61850bean.internal.mms.asn1.Unsigned32;
import com.beanit.iec61850bean.internal.mms.asn1.VariableAccessSpecification;
import com.beanit.iec61850bean.internal.mms.asn1.VariableDefs;
import com.beanit.iec61850bean.internal.mms.asn1.WriteRequest;
import com.beanit.iec61850bean.internal.mms.asn1.WriteResponse;
import com.beanit.josistack.AcseAssociation;
import com.beanit.josistack.ByteBufferInputStream;
import com.beanit.josistack.DecodingException;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class ServerAssociation {

  private static final Logger logger = LoggerFactory.getLogger(ServerAssociation.class);

  private static final WriteResponse.CHOICE writeSuccess = new WriteResponse.CHOICE();
  private static String[] mmsFcs = {
    "MX", "ST", "CO", "CF", "DC", "SP", "SG", "RP", "LG", "BR", "GO", "GS", "SV", "SE", "EX", "SR",
    "OR", "BL"
  };

  static {
    writeSuccess.setSuccess(new BerNull());
  }

  final ServerModel serverModel;
  private final ServerSap serverSap;
  private final ReverseByteArrayOutputStream reverseOStream =
      new ReverseByteArrayOutputStream(500, true);
  ScheduledExecutorService executor = null;
  HashMap<String, DataSet> nonPersistentDataSets = new HashMap<>();
  List<FcModelNode> selects = new ArrayList<>();
  List<Urcb> rsvdURCBs = new ArrayList<>();
  private AcseAssociation acseAssociation = null;
  private int negotiatedMaxPduSize;
  private ByteBuffer pduBuffer;
  private boolean insertRef;
  private String continueAfter;

  public ServerAssociation(ServerSap serverSap) {
    this.serverSap = serverSap;
    serverModel = serverSap.serverModel;
    executor =
        Executors.newScheduledThreadPool(
            2, new NamedThreadFactory("iec61850bean-server-connection"));
  }

  private static void insertMmsRef(ModelNode node, List<String> mmsRefs, String parentRef) {
    String ref = parentRef + '$' + node.getName();
    mmsRefs.add(ref);
    if (!(node instanceof Array)) {
      for (ModelNode childNode : node) {
        insertMmsRef(childNode, mmsRefs, ref);
      }
    }
  }

  private static String convertToDataSetReference(ObjectName mmsObjectName) {
    if (mmsObjectName.getDomainSpecific() != null) {
      return mmsObjectName.getDomainSpecific().getDomainID().toString()
          + "/"
          + mmsObjectName.getDomainSpecific().getItemID().toString().replace('$', '.');
    } else if (mmsObjectName.getAaSpecific() != null) {
      // format is "@DataSetName"
      return mmsObjectName.getAaSpecific().toString();
    }
    return null;
  }

  public void handleNewAssociation(AcseAssociation acseAssociation, ByteBuffer associationRequest) {

    this.acseAssociation = acseAssociation;

    try {
      associate(acseAssociation, associationRequest);
    } catch (IOException e) {
      logger.warn("Error during association build up", e);
      return;
    }

    handleConnection();
  }

  private void associate(AcseAssociation acseAssociation, ByteBuffer associationRequest)
      throws IOException {

    MMSpdu mmsPdu = new MMSpdu();

    mmsPdu.decode(new ByteBufferInputStream(associationRequest), null);

    MMSpdu initiateResponseMmsPdu = constructAssociationResponsePdu(mmsPdu.getInitiateRequestPDU());

    initiateResponseMmsPdu.encode(reverseOStream);

    acseAssociation.accept(reverseOStream.getByteBuffer());
  }

  private MMSpdu constructAssociationResponsePdu(InitiateRequestPDU associationRequestMMSpdu) {

    negotiatedMaxPduSize = serverSap.getMaxMmsPduSize();

    if (associationRequestMMSpdu.getLocalDetailCalling() != null) {
      int proposedMaxMmsPduSize = associationRequestMMSpdu.getLocalDetailCalling().intValue();
      if (negotiatedMaxPduSize > proposedMaxMmsPduSize
          && proposedMaxMmsPduSize >= ServerSap.MINIMUM_MMS_PDU_SIZE) {
        negotiatedMaxPduSize = proposedMaxMmsPduSize;
      }
    }

    int negotiatedMaxServOutstandingCalling = serverSap.getProposedMaxServOutstandingCalling();
    int proposedMaxServOutstandingCalling =
        associationRequestMMSpdu.getProposedMaxServOutstandingCalling().intValue();

    if (negotiatedMaxServOutstandingCalling > proposedMaxServOutstandingCalling
        && proposedMaxServOutstandingCalling > 0) {
      negotiatedMaxServOutstandingCalling = proposedMaxServOutstandingCalling;
    }

    int negotiatedMaxServOutstandingCalled = serverSap.getProposedMaxServOutstandingCalled();
    int proposedMaxServOutstandingCalled =
        associationRequestMMSpdu.getProposedMaxServOutstandingCalled().intValue();

    if (negotiatedMaxServOutstandingCalled > proposedMaxServOutstandingCalled
        && proposedMaxServOutstandingCalled > 0) {
      negotiatedMaxServOutstandingCalled = proposedMaxServOutstandingCalled;
    }

    int negotiatedDataStructureNestingLevel = serverSap.getProposedDataStructureNestingLevel();

    if (associationRequestMMSpdu.getProposedDataStructureNestingLevel() != null) {
      int proposedDataStructureNestingLevel =
          associationRequestMMSpdu.getProposedDataStructureNestingLevel().intValue();
      if (negotiatedDataStructureNestingLevel > proposedDataStructureNestingLevel) {
        negotiatedDataStructureNestingLevel = proposedDataStructureNestingLevel;
      }
    }

    pduBuffer = ByteBuffer.allocate(negotiatedMaxPduSize + 500);

    byte[] negotiatedParameterCbbBitString = serverSap.cbbBitString;

    byte[] servicesSupportedCalledBitString = serverSap.servicesSupportedCalled;

    InitiateResponsePDU.InitResponseDetail initRespDetail =
        new InitiateResponsePDU.InitResponseDetail();
    initRespDetail.setNegotiatedVersionNumber(new Integer16(1));
    initRespDetail.setNegotiatedParameterCBB(
        new ParameterSupportOptions(
            negotiatedParameterCbbBitString, negotiatedParameterCbbBitString.length * 8 - 5));
    initRespDetail.setServicesSupportedCalled(
        new ServiceSupportOptions(
            servicesSupportedCalledBitString, servicesSupportedCalledBitString.length * 8 - 3));

    InitiateResponsePDU initRespPdu = new InitiateResponsePDU();
    initRespPdu.setLocalDetailCalled(new Integer32(negotiatedMaxPduSize));
    initRespPdu.setNegotiatedMaxServOutstandingCalling(
        new Integer16(negotiatedMaxServOutstandingCalling));
    initRespPdu.setNegotiatedMaxServOutstandingCalled(
        new Integer16(negotiatedMaxServOutstandingCalled));
    initRespPdu.setNegotiatedDataStructureNestingLevel(
        new Integer8(negotiatedDataStructureNestingLevel));
    initRespPdu.setInitResponseDetail(initRespDetail);

    MMSpdu initiateResponseMMSpdu = new MMSpdu();
    initiateResponseMMSpdu.setInitiateResponsePDU(initRespPdu);

    return initiateResponseMMSpdu;
  }

  private void handleConnection() {

    while (true) {

      MMSpdu mmsRequestPdu = listenForMmsRequest(acseAssociation);
      if (mmsRequestPdu == null) {
        return;
      }

      ConfirmedRequestPDU confirmedRequestPdu = mmsRequestPdu.getConfirmedRequestPDU();
      // Do not have to check whether confirmedRequestPdu is null because that was already done by
      // listenForMmsRequest()

      if (confirmedRequestPdu.getInvokeID() == null) {
        // cannot respond with ServiceError because no InvokeID was received
        logger.warn("Got unexpected MMS PDU or no invokeID");
        continue;
      }
      int invokeId = confirmedRequestPdu.getInvokeID().intValue();

      try {
        if (confirmedRequestPdu.getService() == null) {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "Got an invalid MMS packet: confirmedServiceRequest empty");
        }

        ConfirmedServiceRequest confirmedServiceRequest = confirmedRequestPdu.getService();

        ConfirmedServiceResponse confirmedServiceResponse = new ConfirmedServiceResponse();

        if (confirmedServiceRequest.getGetNameList() != null) {

          GetNameListRequest getNameListRequest = confirmedServiceRequest.getGetNameList();
          GetNameListResponse response = null;

          if (getNameListRequest.getObjectClass().getBasicObjectClass() == null) {
            throw new ServiceError(
                ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
                "Got an invalid MMS packet: ObjectClass was not selected in GetNameList request");
          }

          long basicObjectClass =
              getNameListRequest.getObjectClass().getBasicObjectClass().longValue();
          if (basicObjectClass == 9) {
            logger.debug("Got a GetServerDirectory (MMS GetNameList[DOMAIN]) request");
            response = handleGetServerDirectoryRequest();
          } else if (basicObjectClass == 0) {
            logger.debug("Got a Get{LD|LN}Directory (MMS GetNameList[NAMED_VARIABLE]) request");
            response = handleGetDirectoryRequest(getNameListRequest);
          } else if (basicObjectClass == 2) {
            logger.debug(
                "Got a GetLogicalNodeDirectory[DataSet] (MMS GetNameList[NAMED_VARIABLE_LIST]) request");
            response = handleGetDataSetNamesRequest(getNameListRequest);
          } else {
            throw new ServiceError(
                ServiceError.FAILED_DUE_TO_SERVER_CONSTRAINT,
                "Unable to handle Get directory request for basic object class: "
                    + basicObjectClass);
          }
          // else if (basicObjectClass == 8) {
          // logger.debug("Got a GetLogicalNodeDirectory[Log] (MMS GetNameList[JOURNAL]) request");
          // response =
          // handleGetNameListJournalRequest(getNameListRequest);
          // }

          confirmedServiceResponse.setGetNameList(response);

        } else if (confirmedServiceRequest.getGetVariableAccessAttributes() != null) {
          logger.debug(
              "Got a GetDataDirectory/GetDataDefinition (MMS GetVariableAccessAttributes) request");
          GetVariableAccessAttributesResponse response =
              handleGetVariableAccessAttributesRequest(
                  confirmedServiceRequest.getGetVariableAccessAttributes());

          confirmedServiceResponse.setGetVariableAccessAttributes(response);

        } else if (confirmedServiceRequest.getRead() != null) {
          // GetDataValues, GetDataSetValues, GetBRCBValues and GetURCBValues map to this
          ReadResponse response = handleGetDataValuesRequest(confirmedServiceRequest.getRead());

          confirmedServiceResponse.setRead(response);
        } else if (confirmedServiceRequest.getWrite() != null) {
          logger.debug("Got a Write request");

          WriteResponse response = handleSetDataValuesRequest(confirmedServiceRequest.getWrite());

          confirmedServiceResponse.setWrite(response);

        }
        // for Data Sets
        else if (confirmedServiceRequest.getDefineNamedVariableList() != null) {
          logger.debug("Got a CreateDataSet request");

          DefineNamedVariableListResponse response =
              handleCreateDataSetRequest(confirmedServiceRequest.getDefineNamedVariableList());

          confirmedServiceResponse.setDefineNamedVariableList(response);
        } else if (confirmedServiceRequest.getGetNamedVariableListAttributes() != null) {
          logger.debug("Got a GetDataSetDirectory request");
          GetNamedVariableListAttributesResponse response =
              handleGetDataSetDirectoryRequest(
                  confirmedServiceRequest.getGetNamedVariableListAttributes());

          confirmedServiceResponse.setGetNamedVariableListAttributes(response);

        } else if (confirmedServiceRequest.getDeleteNamedVariableList() != null) {
          logger.debug("Got a DeleteDataSet request");
          DeleteNamedVariableListResponse response =
              handleDeleteDataSetRequest(confirmedServiceRequest.getDeleteNamedVariableList());

          confirmedServiceResponse.setDeleteNamedVariableList(response);
        } else {
          throw new ServiceError(
              ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
              "invalid MMS packet: unknown request type.");
        }

        ConfirmedResponsePDU confirmedResponsePdu = new ConfirmedResponsePDU();
        confirmedResponsePdu.setInvokeID(confirmedRequestPdu.getInvokeID());
        confirmedResponsePdu.setService(confirmedServiceResponse);

        MMSpdu mmsResponsePdu = new MMSpdu();
        mmsResponsePdu.setConfirmedResponsePDU(confirmedResponsePdu);

        if (!sendAnMmsPdu(mmsResponsePdu)) {
          return;
        }
      } catch (ServiceError e) {
        logger.warn(e.getMessage());
        if (!sendAnMmsPdu(createServiceErrorResponse(e, invokeId))) {
          return;
        }
      }
    }
  }

  void cleanUpConnection() {
    synchronized (serverModel) {
      for (FcModelNode selectedCdo : selects) {
        selectedCdo.deselect();
      }
      for (Urcb rsvdUrcb : rsvdURCBs) {
        synchronized (rsvdUrcb) {
          if (rsvdUrcb.enabled) {
            rsvdUrcb.disable();
          }
          rsvdUrcb.reserved = null;
          rsvdUrcb.getResv().setValue(false);
        }
      }
    }
  }

  boolean sendAnMmsPdu(MMSpdu mmsResponsePdu) {

    synchronized (reverseOStream) {
      reverseOStream.reset();
      try {
        mmsResponsePdu.encode(reverseOStream);
      } catch (IOException e1) {
        logger.error("IOException while encoding MMS PDU. Closing association.", e1);
        return false;
      }
      try {
        acseAssociation.send(reverseOStream.getByteBuffer());
      } catch (IOException e) {
        logger.warn("IOException while sending MMS PDU. Closing association.", e);
        return false;
      }
    }
    return true;
  }

  private MMSpdu listenForMmsRequest(AcseAssociation acseAssociation) {

    while (true) {
      MMSpdu mmsRequestPdu;
      byte[] buffer;
      pduBuffer.clear();
      try {
        buffer = acseAssociation.receive(pduBuffer);
      } catch (EOFException e) {
        logger.debug("Connection was closed by client.");
        return null;
      } catch (SocketTimeoutException e) {
        logger.warn(
            "Message fragment timeout occured while receiving request. Closing association.", e);
        return null;
      } catch (IOException e) {
        logger.warn(
            "IOException at lower layers while listening for incoming request. Closing association.",
            e);
        return null;
      } catch (DecodingException e) {
        logger.error("Error decoding request at OSI layers.", e);
        continue;
      } catch (TimeoutException e) {
        logger.error(
            "Illegal state: message timeout while receiving request though this timeout should 0 and never be thrown",
            e);
        return null;
      }
      mmsRequestPdu = new MMSpdu();

      try {
        mmsRequestPdu.decode(new ByteArrayInputStream(buffer), null);
      } catch (IOException e) {
        logger.warn("IOException decoding received MMS request PDU.", e);
        continue;
      }

      if (mmsRequestPdu.getConfirmedRequestPDU() == null) {
        if (mmsRequestPdu.getConcludeRequestPDU() != null) {
          logger.debug("Got Conclude request, will close connection");
          return null;
        } else {
          logger.warn("Got unexpected MMS PDU, will ignore it");
          continue;
        }
      }

      return mmsRequestPdu;
    }
  }

  private MMSpdu createServiceErrorResponse(ServiceError e, int invokeId) {

    ErrorClass errClass = new ErrorClass();

    switch (e.getErrorCode()) {
      case ServiceError.NO_ERROR:
        break;
      case ServiceError.INSTANCE_NOT_AVAILABLE:
        errClass.setAccess(new BerInteger(e.getErrorCode()));
        break;
      case ServiceError.INSTANCE_IN_USE:
        errClass.setDefinition(new BerInteger(e.getErrorCode()));
        break;
      case ServiceError.ACCESS_VIOLATION:
        errClass.setAccess(new BerInteger(e.getErrorCode()));
        break;
      case ServiceError.ACCESS_NOT_ALLOWED_IN_CURRENT_STATE:
        errClass.setOthers(new BerInteger(e.getErrorCode()));
        break;
      case ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT:
        errClass.setFile(new BerInteger(2));
        break;
      case ServiceError.TYPE_CONFLICT:
        errClass.setFile(new BerInteger(4));
        break;
      default:
        errClass.setOthers(new BerInteger(e.getErrorCode()));
    }
    com.beanit.iec61850bean.internal.mms.asn1.ServiceError asn1ServiceError;

    asn1ServiceError = new com.beanit.iec61850bean.internal.mms.asn1.ServiceError();
    asn1ServiceError.setErrorClass(errClass);
    asn1ServiceError.setAdditionalDescription(new BerVisibleString(e.getMessage()));

    ConfirmedErrorPDU confirmedErrorPDU = new ConfirmedErrorPDU();
    confirmedErrorPDU.setInvokeID(new Unsigned32(invokeId));
    confirmedErrorPDU.setServiceError(asn1ServiceError);

    MMSpdu mmsPdu = new MMSpdu();
    mmsPdu.setConfirmedErrorPDU(confirmedErrorPDU);

    return mmsPdu;
  }

  private GetNameListResponse handleGetServerDirectoryRequest() throws ServiceError {

    ListOfIdentifier listOfIdentifier = new ListOfIdentifier();
    List<Identifier> identifiers = listOfIdentifier.getIdentifier();

    for (ModelNode ld : serverModel) {
      identifiers.add(new Identifier(ld.getName().getBytes(UTF_8)));
    }

    GetNameListResponse getNameListResponse = new GetNameListResponse();
    getNameListResponse.setListOfIdentifier(listOfIdentifier);
    getNameListResponse.setMoreFollows(new BerBoolean(false));

    return getNameListResponse;
  }

  private GetNameListResponse handleGetDirectoryRequest(GetNameListRequest getNameListRequest)
      throws ServiceError {

    // the ObjectScope can be vmdSpecific,domainSpecific, or aaSpecific. vmdSpecific and aaSpecific
    // are not part of
    // 61850-8-1 but are used by some IEC 61850 clients anyways. This stack will return an empty
    // list on vmdSpecific
    // and aaSpecific requests.
    if (getNameListRequest.getObjectScope().getAaSpecific() != null
        || getNameListRequest.getObjectScope().getVmdSpecific() != null) {
      ListOfIdentifier listOfIden = new ListOfIdentifier();
      listOfIden.getIdentifier();
      GetNameListResponse getNameListResponse = new GetNameListResponse();
      getNameListResponse.setListOfIdentifier(listOfIden);
      getNameListResponse.setMoreFollows(new BerBoolean(false));
      return getNameListResponse;
    }

    String mmsDomainId = getNameListRequest.getObjectScope().getDomainSpecific().toString();

    ModelNode logicalDeviceMn = serverModel.getChild(mmsDomainId);

    if (logicalDeviceMn == null) {
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
          "Got an invalid MMS request: given Domain name in GetNameList request is not a Logical Device name");
    }

    LogicalDevice logicalDevice = (LogicalDevice) logicalDeviceMn;

    insertRef = true;

    if (getNameListRequest.getContinueAfter() != null) {
      continueAfter = getNameListRequest.getContinueAfter().toString();
      insertRef = false;
    }

    List<String> mmsReferences = new ArrayList<>();

    for (ModelNode logicalNodeMn : logicalDevice) {
      LogicalNode logicalNode = (LogicalNode) logicalNodeMn;
      mmsReferences.add(logicalNode.getName());

      for (String mmsFC : mmsFcs) {
        Fc fc = Fc.fromString(mmsFC);
        if (fc != null) {

          List<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);
          if (fcDataObjects != null) {
            mmsReferences.add(logicalNode.getName() + "$" + mmsFC);
            for (FcDataObject dataObject : fcDataObjects) {
              insertMmsRef(dataObject, mmsReferences, logicalNode.getName() + "$" + mmsFC);
            }
          }
        }
      }
    }

    ListOfIdentifier listOfIden = new ListOfIdentifier();
    List<Identifier> identifiers = listOfIden.getIdentifier();

    int identifierSize = 0;
    boolean moreFollows = false;
    for (String mmsReference : mmsReferences) {
      if (insertRef == true) {
        if (identifierSize > negotiatedMaxPduSize - 200) {
          moreFollows = true;
          logger.debug(" ->maxMMSPduSize of " + negotiatedMaxPduSize + " Bytes reached");
          break;
        }

        Identifier identifier;

        identifier = new Identifier(mmsReference.getBytes(UTF_8));

        identifiers.add(identifier);
        identifierSize += mmsReference.length() + 2;
      } else {
        if (mmsReference.equals(continueAfter)) {
          insertRef = true;
        }
      }
    }

    GetNameListResponse getNameListResponse = new GetNameListResponse();
    getNameListResponse.setListOfIdentifier(listOfIden);
    getNameListResponse.setMoreFollows(new BerBoolean(moreFollows));

    return getNameListResponse;
  }

  /**
   * GetVariableAccessAttributes (GetDataDefinition/GetDataDirectory) can be called with different
   * kinds of references. Examples: 1. DGEN1 2. DGEN1$CF 3. DGEN1$CF$GnBlk
   */
  private GetVariableAccessAttributesResponse handleGetVariableAccessAttributesRequest(
      GetVariableAccessAttributesRequest getVariableAccessAttributesRequest) throws ServiceError {
    if (getVariableAccessAttributesRequest.getName() == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Got an invalid MMS packet: name is not selected in GetVariableAccessAttributesRequest");
    }

    DomainSpecific domainSpecific =
        getVariableAccessAttributesRequest.getName().getDomainSpecific();

    if (domainSpecific == null) {
      throw new ServiceError(
          ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
          "Got an invalid MMS packet: Domain specific is not selected in GetVariableAccessAttributesRequest");
    }

    ModelNode modelNode = serverModel.getChild(domainSpecific.getDomainID().toString());

    if (modelNode == null) {
      throw new ServiceError(
          ServiceError.INSTANCE_NOT_AVAILABLE,
          "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
              + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
              + " and ItemID "
              + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
              + " was found.");
    }

    String itemIdString = domainSpecific.getItemID().toString();

    int index1 = itemIdString.indexOf('$');

    LogicalNode logicalNode = null;

    if (index1 != -1) {
      logicalNode = (LogicalNode) modelNode.getChild(itemIdString.substring(0, index1));
      if (logicalNode == null) {
        throw new ServiceError(
            ServiceError.INSTANCE_NOT_AVAILABLE,
            "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                + " and ItemID "
                + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                + " was found.");
      }
      int index2 = itemIdString.indexOf('$', index1 + 2);
      if (index2 != -1) {
        Fc fc = Fc.fromString(itemIdString.substring(index1 + 1, index2));
        if (fc == null) {
          throw new ServiceError(
              ServiceError.INSTANCE_NOT_AVAILABLE,
              "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                  + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                  + " and ItemID "
                  + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                  + " was found.");
        }
        index1 = itemIdString.indexOf('$', index2 + 2);
        ModelNode subNode;
        if (index1 == -1) {
          subNode = logicalNode.getChild(itemIdString.substring(index2 + 1), fc);
          if (subNode == null) {
            throw new ServiceError(
                ServiceError.INSTANCE_NOT_AVAILABLE,
                "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                    + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                    + " and ItemID "
                    + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                    + " was found.");
          }
        } else {
          subNode = logicalNode.getChild(itemIdString.substring(index2 + 1, index1), fc);
          if (subNode == null) {
            throw new ServiceError(
                ServiceError.INSTANCE_NOT_AVAILABLE,
                "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                    + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                    + " and ItemID "
                    + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                    + " was found.");
          }
          index2 = itemIdString.indexOf('$', index1 + 2);
          while (index2 != -1) {
            subNode = subNode.getChild(itemIdString.substring(index1 + 1, index2));
            if (subNode == null) {
              throw new ServiceError(
                  ServiceError.INSTANCE_NOT_AVAILABLE,
                  "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                      + getVariableAccessAttributesRequest
                          .getName()
                          .getDomainSpecific()
                          .getDomainID()
                      + " and ItemID "
                      + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                      + " was found.");
            }
            index1 = index2;
            index2 = itemIdString.indexOf('$', index1 + 2);
          }
          subNode = subNode.getChild(itemIdString.substring(index1 + 1));
          if (subNode == null) {
            throw new ServiceError(
                ServiceError.INSTANCE_NOT_AVAILABLE,
                "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                    + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                    + " and ItemID "
                    + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                    + " was found.");
          }
        }

        GetVariableAccessAttributesResponse getVariableAccessAttributesResponse =
            new GetVariableAccessAttributesResponse();
        getVariableAccessAttributesResponse.setMmsDeletable(new BerBoolean(false));
        getVariableAccessAttributesResponse.setTypeDescription(subNode.getMmsTypeSpec());

        return getVariableAccessAttributesResponse;
      } else {
        Fc fc = Fc.fromString(itemIdString.substring(index1 + 1));

        if (fc == null) {
          throw new ServiceError(
              ServiceError.INSTANCE_NOT_AVAILABLE,
              "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                  + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                  + " and ItemID "
                  + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                  + " was found.");
        }

        List<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);

        if (fcDataObjects == null || fcDataObjects.size() == 0) {
          throw new ServiceError(
              ServiceError.INSTANCE_NOT_AVAILABLE,
              "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
                  + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
                  + " and ItemID "
                  + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
                  + " was found.");
        }

        Components comp = new Components();
        List<TypeDescription.Structure.Components.SEQUENCE> doStructComponents = comp.getSEQUENCE();
        for (ModelNode child : fcDataObjects) {
          TypeSpecification typeSpecification = new TypeSpecification();
          typeSpecification.setTypeDescription(child.getMmsTypeSpec());

          TypeDescription.Structure.Components.SEQUENCE structComponent =
              new TypeDescription.Structure.Components.SEQUENCE();
          structComponent.setComponentName(new Identifier(child.getName().getBytes(UTF_8)));
          structComponent.setComponentType(typeSpecification);
          doStructComponents.add(structComponent);
        }

        Structure struct = new Structure();
        struct.setComponents(comp);

        TypeDescription typeDescription = new TypeDescription();
        typeDescription.setStructure(struct);

        GetVariableAccessAttributesResponse getVariableAccessAttributesResponse =
            new GetVariableAccessAttributesResponse();
        getVariableAccessAttributesResponse.setMmsDeletable(new BerBoolean(false));
        getVariableAccessAttributesResponse.setTypeDescription(typeDescription);

        return getVariableAccessAttributesResponse;
      }
    }

    logicalNode = (LogicalNode) modelNode.getChild(itemIdString);
    if (logicalNode == null) {
      throw new ServiceError(
          ServiceError.INSTANCE_NOT_AVAILABLE,
          "GetVariableAccessAttributes (GetDataDefinition): no object with domainId "
              + getVariableAccessAttributesRequest.getName().getDomainSpecific().getDomainID()
              + " and ItemID "
              + getVariableAccessAttributesRequest.getName().getDomainSpecific().getItemID()
              + " was found.");
    }

    Components components = new Components();
    List<TypeDescription.Structure.Components.SEQUENCE> structComponents = components.getSEQUENCE();

    for (String mmsFc : mmsFcs) {
      Fc fc = Fc.fromString(mmsFc);
      if (fc != null) {

        Collection<FcDataObject> fcDataObjects = logicalNode.getChildren(fc);
        if (fcDataObjects == null) {
          continue;
        }

        Components comp = new Components();
        List<TypeDescription.Structure.Components.SEQUENCE> doStructComponents = comp.getSEQUENCE();

        for (ModelNode child : fcDataObjects) {
          TypeSpecification typeSpecification = new TypeSpecification();
          typeSpecification.setTypeDescription(child.getMmsTypeSpec());

          TypeDescription.Structure.Components.SEQUENCE doStructComponent =
              new TypeDescription.Structure.Components.SEQUENCE();
          doStructComponent.setComponentName(new Identifier(child.getName().getBytes(UTF_8)));
          doStructComponent.setComponentType(typeSpecification);

          doStructComponents.add(doStructComponent);
        }

        Structure struct = new Structure();
        struct.setComponents(comp);

        TypeDescription fcTypeSpec = new TypeDescription();
        fcTypeSpec.setStructure(struct);

        TypeSpecification typeSpecification = new TypeSpecification();
        typeSpecification.setTypeDescription(fcTypeSpec);

        TypeDescription.Structure.Components.SEQUENCE structCom =
            new TypeDescription.Structure.Components.SEQUENCE();
        structCom.setComponentName(new Identifier(mmsFc.getBytes(UTF_8)));
        structCom.setComponentType(typeSpecification);

        structComponents.add(structCom);
      }
    }

    Structure struct = new Structure();
    struct.setComponents(components);

    TypeDescription typeSpec = new TypeDescription();
    typeSpec.setStructure(struct);

    GetVariableAccessAttributesResponse getVariableAccessAttributesResponse =
        new GetVariableAccessAttributesResponse();
    getVariableAccessAttributesResponse.setMmsDeletable(new BerBoolean(false));
    getVariableAccessAttributesResponse.setTypeDescription(typeSpec);

    return getVariableAccessAttributesResponse;
  }

  private ReadResponse handleGetDataValuesRequest(ReadRequest mmsReadRequest) throws ServiceError {

    VariableAccessSpecification variableAccessSpecification =
        mmsReadRequest.getVariableAccessSpecification();

    if (mmsReadRequest.getSpecificationWithResult() == null
        || mmsReadRequest.getSpecificationWithResult().value == false) {

      if (variableAccessSpecification.getListOfVariable() == null) {
        throw new ServiceError(
            ServiceError.FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT,
            "handleGetDataValuesRequest: Got an invalid MMS packet");
      }

      List<VariableDefs.SEQUENCE> listOfVariable =
          variableAccessSpecification.getListOfVariable().getSEQUENCE();

      if (listOfVariable.size() < 1) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INCONSISTENT,
            "handleGetDataValuesRequest: less than one variableAccessSpecification is not allowed");
      }

      ListOfAccessResult listOfAccessResult = new ListOfAccessResult();
      List<AccessResult> accessResults = listOfAccessResult.getAccessResult();

      synchronized (serverModel) {
        for (VariableDefs.SEQUENCE variableDef : listOfVariable) {

          FcModelNode modelNode = serverModel.getNodeFromVariableDef(variableDef);

          if (modelNode == null) {
            logger.debug("Got a GetDataValues request for a non existent model node.");
            // 10 indicates error "object-non-existent"
            AccessResult accessResult = new AccessResult();
            accessResult.setFailure(new DataAccessError(10L));
            accessResults.add(accessResult);
          } else {
            if (logger.isDebugEnabled()) {
              logger.debug("Got a GetDataValues request for node: " + modelNode);
              if (!(modelNode instanceof BasicDataAttribute)) {
                for (BasicDataAttribute bda : modelNode.getBasicDataAttributes()) {
                  logger.debug("sub BDA is:" + bda);
                }
              }
            }
            accessResults.add(getReadResult(modelNode));
          }
        }
      }

      ReadResponse readResponse = new ReadResponse();
      readResponse.setListOfAccessResult(listOfAccessResult);
      return readResponse;
    } else {
      logger.debug("Got a GetDataSetValues request.");

      String dataSetReference =
          convertToDataSetReference(variableAccessSpecification.getVariableListName());

      if (dataSetReference == null) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INCONSISTENT,
            "handleGetDataSetValuesRequest: DataSet name incorrect");
      }

      ListOfAccessResult listOfAccessResult = new ListOfAccessResult();
      List<AccessResult> accessResults = listOfAccessResult.getAccessResult();

      if (dataSetReference.startsWith("@")) {
        DataSet dataSet = nonPersistentDataSets.get(dataSetReference);
        if (dataSet == null) {
          throw new ServiceError(
              ServiceError.PARAMETER_VALUE_INCONSISTENT,
              "handleGetDataSetValuesRequest: a DataSet with the given reference does not exist");
        }

        for (FcModelNode dsMember : dataSet) {
          accessResults.add(getReadResult(dsMember));
        }
      } else {
        synchronized (serverModel) {
          DataSet dataSet = serverModel.getDataSet(dataSetReference);

          if (dataSet == null) {
            throw new ServiceError(
                ServiceError.PARAMETER_VALUE_INCONSISTENT,
                "handleGetDataSetValuesRequest: a DataSet with the given reference does not exist");
          }

          for (FcModelNode dsMember : dataSet) {
            accessResults.add(getReadResult(dsMember));
          }
        }
      }
      ReadResponse readResponse = new ReadResponse();
      readResponse.setListOfAccessResult(listOfAccessResult);
      return readResponse;
    }
  }

  private AccessResult getReadResult(FcModelNode modelNode) {

    AccessResult accessResult = new AccessResult();

    if (modelNode.getFc() == Fc.CO && modelNode.getName().equals("SBO")) {
      // if (modelNode.getName().equals("SBO")) {
      FcModelNode cdcParent = (FcModelNode) modelNode.getParent();
      ModelNode ctlModelNode =
          serverModel.findModelNode(cdcParent.getReference(), Fc.CF).getChild("ctlModel");
      if (ctlModelNode == null
          || !(ctlModelNode instanceof BdaInt8)
          || ((BdaInt8) ctlModelNode).getValue() != 2) {
        logger.warn(
            "Selecting controle DO fails because ctlModel is not set to \"sbo-with-normal-security\"");
        // 3 indicates error "object_access_denied"
        accessResult.setFailure(new DataAccessError(3L));
        return accessResult;
      }
      if (!cdcParent.select(this, serverSap.timer)) {
        Data data = new Data();
        data.setVisibleString(new BerVisibleString(""));
        accessResult.setSuccess(data);
        return accessResult;
      }
      Data data = new Data();
      data.setVisibleString(new BerVisibleString("success"));
      accessResult.setSuccess(data);
      return accessResult;

      // }
      // else {
      // logger.warn("A client tried to read a control variable other than SBO. This is not
      // allowed.");
      // // 3 indicates error "object_access_denied"
      // return new AccessResult(new BerInteger(3L), null);
      // }

    }

    Data data = modelNode.getMmsDataObj();

    if (data == null) {
      // 11 indicates error "object_value_invalid"
      accessResult.setFailure(new DataAccessError(11L));
      return accessResult;
    }

    accessResult.setSuccess(data);
    return accessResult;
  }

  private WriteResponse handleSetDataValuesRequest(WriteRequest mmsWriteRequest)
      throws ServiceError {

    VariableAccessSpecification variableAccessSpecification =
        mmsWriteRequest.getVariableAccessSpecification();

    List<Data> listOfData = mmsWriteRequest.getListOfData().getData();

    WriteResponse writeResponse = new WriteResponse();
    List<WriteResponse.CHOICE> mmsResponseValues = writeResponse.getCHOICE();

    if (variableAccessSpecification.getListOfVariable() != null) {
      logger.debug("Got a SetDataValues request.");

      List<VariableDefs.SEQUENCE> listOfVariable =
          variableAccessSpecification.getListOfVariable().getSEQUENCE();

      if (listOfVariable.size() < 1
          || listOfData.size() < 1
          || listOfVariable.size() != listOfData.size()) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INCONSISTENT,
            "handleSetDataValuesRequest: less than one variableAccessSpecification or data element is not allowed, or listOfData ne listOfVar");
      }

      Iterator<Data> mmsDataIterator = listOfData.iterator();

      List<BasicDataAttribute> totalBdasToBeWritten = new ArrayList<>();
      int[] numBdas = new int[listOfData.size()];

      int i = -1;
      synchronized (serverModel) {
        for (VariableDefs.SEQUENCE variableDef : listOfVariable) {
          i++;
          Data mmsData = mmsDataIterator.next();

          FcModelNode modelNode = serverModel.getNodeFromVariableDef(variableDef);

          if (modelNode == null) {
            // 10 indicates error "object-non-existent"
            WriteResponse.CHOICE writeResponseChoice = new WriteResponse.CHOICE();
            writeResponseChoice.setFailure(new DataAccessError(10L));
            mmsResponseValues.add(writeResponseChoice);
          } else {

            getFirstWriteResults(
                mmsResponseValues, totalBdasToBeWritten, numBdas, i, modelNode, mmsData);
          }
        }

        writeAndFillMissingWriteResults(mmsResponseValues, totalBdasToBeWritten, numBdas);
      }

    } else if (variableAccessSpecification.getVariableListName() != null) {
      logger.debug("Got a SetDataSetValues request.");

      String dataSetRef =
          convertToDataSetReference(variableAccessSpecification.getVariableListName());

      // TODO handle non-persisten DataSets too

      DataSet dataSet = serverModel.getDataSet(dataSetRef);

      Iterator<Data> mmsDataIterator = listOfData.iterator();

      List<BasicDataAttribute> totalBdasToBeWritten = new ArrayList<>();
      int[] numBdas = new int[listOfData.size()];

      int i = -1;
      synchronized (serverModel) {
        for (FcModelNode dataSetMember : dataSet) {
          i++;
          Data mmsData = mmsDataIterator.next();

          getFirstWriteResults(
              mmsResponseValues, totalBdasToBeWritten, numBdas, i, dataSetMember, mmsData);
        }

        writeAndFillMissingWriteResults(mmsResponseValues, totalBdasToBeWritten, numBdas);
      }

    } else {
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
          "handleSetDataValuesRequest: invalid MMS request");
    }

    return writeResponse;
  }

  private void writeAndFillMissingWriteResults(
      List<WriteResponse.CHOICE> mmsResponseValues,
      List<BasicDataAttribute> totalBdasToBeWritten,
      int[] numBdas) {
    int i;
    if (totalBdasToBeWritten.size() != 0) {
      List<ServiceError> serviceErrors = serverSap.serverEventListener.write(totalBdasToBeWritten);
      ListIterator<WriteResponse.CHOICE> mmsResponseIterator = mmsResponseValues.listIterator();
      if (serviceErrors == null || serviceErrors.size() != totalBdasToBeWritten.size()) {
        while (mmsResponseIterator.hasNext()) {
          if (mmsResponseIterator.next() == null) {
            mmsResponseIterator.set(writeSuccess);
          }
        }
        for (BasicDataAttribute bda : totalBdasToBeWritten) {
          bda.mirror.setValueFrom(bda);
        }
      } else {
        i = -1;
        Iterator<ServiceError> serviceErrorIterator = serviceErrors.iterator();
        Iterator<BasicDataAttribute> bdaToBeWrittenIterator = totalBdasToBeWritten.iterator();
        while (mmsResponseIterator.hasNext()) {
          i++;
          if (mmsResponseIterator.next() == null) {
            for (int j = 0; j < numBdas[i]; j++) {
              ServiceError serviceError = serviceErrorIterator.next();
              BasicDataAttribute bda = bdaToBeWrittenIterator.next();
              if (serviceError != null) {
                WriteResponse.CHOICE writeResponseChoice = new WriteResponse.CHOICE();
                writeResponseChoice.setFailure(
                    new DataAccessError(serviceErrorToMmsError(serviceError)));
                mmsResponseIterator.set(writeResponseChoice);
              } else {
                bda.mirror.setValueFrom(bda);
              }
            }
          }
        }
      }
    }
  }

  private void getFirstWriteResults(
      List<WriteResponse.CHOICE> mmsResponseValues,
      List<BasicDataAttribute> totalBdasToBeWritten,
      int[] numBdas,
      int i,
      FcModelNode fcModelNode,
      Data mmsData) {
    WriteResponse.CHOICE writeResult = getWriteResult(fcModelNode, mmsData);
    if (writeResult == null) {
      FcModelNode fcModelNodeCopy = (FcModelNode) fcModelNode.copy();
      try {
        fcModelNodeCopy.setValueFromMmsDataObj(mmsData);
      } catch (ServiceError e) {
        logger.warn("SetDataValues failed because of data missmatch.", e);
        WriteResponse.CHOICE writeResponseChoice = new WriteResponse.CHOICE();
        writeResponseChoice.setFailure(new DataAccessError(serviceErrorToMmsError(e)));
        mmsResponseValues.add(writeResponseChoice);
        return;
      }

      if (fcModelNodeCopy.fc == Fc.CO) {
        // TODO timeactivate operate
        fcModelNodeCopy = (FcModelNode) fcModelNodeCopy.getChild("ctlVal");
        // TODO write origin and ctlNum if they exist
      } else {

      }

      List<BasicDataAttribute> bdas = fcModelNodeCopy.getBasicDataAttributes();
      totalBdasToBeWritten.addAll(bdas);
      numBdas[i] = bdas.size();
      mmsResponseValues.add(null);

    } else {
      mmsResponseValues.add(writeResult);
    }
  }

  // private WriteResponse.SubChoice operate(FcModelNode modelNode, Data mmsData) {
  // FcModelNode fcModelNodeCopy = (FcModelNode) modelNode.copy();
  // try {
  // fcModelNodeCopy.setValueFromMmsDataObj(mmsData);
  // } catch (ServiceError e) {
  // logger.warn("SetDataValues failed because of data missmatch.", e);
  // return new WriteResponse.SubChoice(new BerInteger(serviceErrorToMmsError(e)), null);
  // }
  //
  // // TODO timeactivate operate
  //
  // BasicDataAttribute ctlValBda = (BasicDataAttribute) fcModelNodeCopy.getChild("ctlVal");
  // List<BasicDataAttribute> bdas = new ArrayList<BasicDataAttribute>(1);
  // bdas.add(ctlValBda);
  // List<ServiceError> serviceErrors;
  // try {
  // serviceErrors = serverSap.serverEventListener.write(bdas);
  // } catch (ServiceError e) {
  // return new WriteResponse.SubChoice(new BerInteger(serviceErrorToMmsError(e)), null);
  // }
  // if (serviceErrors != null && serviceErrors.size() == bdas.size() && serviceErrors.get(1) !=
  // null) {
  // return new WriteResponse.SubChoice(new
  // BerInteger(serviceErrorToMmsError(serviceErrors.get(1))), null);
  // }
  //
  // ctlValBda.mirror.setValueFrom(ctlValBda);
  // // TODO write origin and ctlNum if they exist
  //
  // return writeSuccess;
  // }

  private WriteResponse.CHOICE getWriteResult(FcModelNode modelNode, Data mmsData) {

    WriteResponse.CHOICE writeResponse = new WriteResponse.CHOICE();

    Fc fc = modelNode.getFc();
    if (fc == Fc.ST || fc == Fc.MX || fc == Fc.OR || fc == Fc.EX) {
      // 3 indicates error "object_access_denied"
      writeResponse.setFailure(new DataAccessError(3L));
      return writeResponse;
    }

    if (fc == Fc.CO) {
      String nodeName = modelNode.getName();

      if (nodeName.equals("Oper")) {
        FcModelNode cdcParent = (FcModelNode) modelNode.getParent();
        ModelNode ctlModelNode =
            serverModel.findModelNode(cdcParent.getReference(), Fc.CF).getChild("ctlModel");
        if (ctlModelNode == null || !(ctlModelNode instanceof BdaInt8)) {
          logger.warn("Operatring controle DO failed because ctlModel is not set.");
          // 3 indicates error "object_access_denied"
          writeResponse.setFailure(new DataAccessError(3L));
          return writeResponse;
        }

        int ctlModel = ((BdaInt8) ctlModelNode).getValue();

        /* Direct control with normal security (direct-operate) */
        if (ctlModel == 1) {
          return null;
        }
        /* SBO control with normal security (operate-once or operate-many) */
        else if (ctlModel == 2) {
          if (cdcParent.isSelectedBy(this)) {
            return null;
          } else {
            // 3 indicates error "object_access_denied"
            writeResponse.setFailure(new DataAccessError(3L));
            return writeResponse;
          }

        } else {
          logger.warn("SetDataValues failed because of unsupported ctlModel: " + ctlModel);
          // 9 indicates error "object_access_unsupported"
          writeResponse.setFailure(new DataAccessError(9L));
          return writeResponse;
        }
      } else {
        logger.warn(
            "SetDataValues failed because of the operation is not allowed yet: "
                + modelNode.getName());
        // 9 indicates error "object_access_unsupported"
        writeResponse.setFailure(new DataAccessError(9L));
        return writeResponse;
      }
    } else if (fc == Fc.RP) {

      if (modelNode instanceof Rcb) {
        // 3 indicates error "object_access_denied"
        writeResponse.setFailure(new DataAccessError(3L));
        return writeResponse;
      }

      FcModelNode fcModelNodeCopy = (FcModelNode) modelNode.copy();

      try {
        fcModelNodeCopy.setValueFromMmsDataObj(mmsData);
      } catch (ServiceError e) {
        WriteResponse.CHOICE writeResponseChoice = new WriteResponse.CHOICE();
        writeResponseChoice.setFailure(new DataAccessError(serviceErrorToMmsError(e)));
        return writeResponseChoice;
      }

      Urcb urcb = (Urcb) modelNode.getParent();

      String nodeName = modelNode.getName();

      synchronized (urcb) {
        if (nodeName.equals("RptEna")) {
          BdaBoolean rptEnaNode = (BdaBoolean) fcModelNodeCopy;
          if (rptEnaNode.getValue()) {
            if (urcb.dataSet == null) {
              logger.info("client tried to enable RCB even though there is no configured data set");
              // 3 indicates error "object_access_denied"
              writeResponse.setFailure(new DataAccessError(3L));
              return writeResponse;
            }
            if (urcb.reserved == null) {
              urcb.reserved = this;
              urcb.enable();
              rsvdURCBs.add(urcb);
              ((BdaBoolean) modelNode).setValue(true);
              return writeSuccess;
            } else if (urcb.reserved == this) {
              urcb.enable();
              ((BdaBoolean) modelNode).setValue(true);
              return writeSuccess;
            } else {
              // 3 indicates error "object_access_denied"
              writeResponse.setFailure(new DataAccessError(3L));
              return writeResponse;
            }

          } else {
            // disable reporting
            if (urcb.reserved == this) {
              urcb.disable();
              ((BdaBoolean) modelNode).setValue(false);
              return writeSuccess;
            } else {
              // 3 indicates error "object_access_denied"
              writeResponse.setFailure(new DataAccessError(3L));
              return writeResponse;
            }
          }
        } else if (nodeName.equals("Resv")) {
          BdaBoolean rptResvNode = (BdaBoolean) fcModelNodeCopy;
          if (rptResvNode.getValue()) {

            if (urcb.reserved == null) {
              urcb.reserved = this;
              urcb.getResv().setValue(true);
              rsvdURCBs.add(urcb);
              return writeSuccess;
            } else if (urcb.reserved == this) {
              return writeSuccess;
            } else {
              // 3 indicates error "object_access_denied"
              writeResponse.setFailure(new DataAccessError(3L));
              return writeResponse;
            }
          } else {
            if (urcb.reserved == this) {
              urcb.reserved = null;
              urcb.getResv().setValue(false);
              rsvdURCBs.remove(urcb);
              return writeSuccess;
            } else {
              // 3 indicates error "object_access_denied"
              writeResponse.setFailure(new DataAccessError(3L));
              return writeResponse;
            }
          }

        } else if (nodeName.equals("DatSet")) {
          if ((urcb.reserved == null || urcb.reserved == this) && !urcb.enabled) {
            String dataSetRef =
                ((BdaVisibleString) fcModelNodeCopy).getStringValue().replace('$', '.');
            if (dataSetRef.isEmpty()) {
              urcb.dataSet = null;
              ((BasicDataAttribute) modelNode).setValueFrom((BasicDataAttribute) fcModelNodeCopy);
              return writeSuccess;

            } else {
              DataSet dataSet = serverModel.getDataSet(dataSetRef);
              if (dataSet == null) {
                dataSet = nonPersistentDataSets.get(dataSetRef);
              }
              if (dataSet != null) {
                urcb.dataSet = dataSet;
                ((BasicDataAttribute) modelNode).setValueFrom((BasicDataAttribute) fcModelNodeCopy);
                return writeSuccess;
              } else {
                logger.info(
                    "Client tried to set dataSetReference of URCB to non existant data set.");
                // 3 indicates error "object_access_denied"
                writeResponse.setFailure(new DataAccessError(3L));
                return writeResponse;
              }
            }
          } else {
            logger.info(
                "Client tried to write RCB parameter even though URCB is reserved by other client or already enabled.");
            // 3 indicates error "object_access_denied"
            writeResponse.setFailure(new DataAccessError(3L));
            return writeResponse;
          }
        } else if (nodeName.equals("OptFlds")) {
          if ((urcb.reserved == null || urcb.reserved == this) && !urcb.enabled) {
            if (!((BdaOptFlds) modelNode).isBufferOverflow()
                && !((BdaOptFlds) modelNode).isConfigRevision()
                && !((BdaOptFlds) modelNode).isDataReference()
                && !((BdaOptFlds) modelNode).isEntryId()) {
              ((BasicDataAttribute) modelNode).setValueFrom((BasicDataAttribute) fcModelNodeCopy);
              return writeSuccess;
            } else {
              logger.info("Client tried to write OptFlds with usupported field set to true.");
              // 3 indicates error "object_access_denied"
              writeResponse.setFailure(new DataAccessError(3L));
              return writeResponse;
            }
          } else {
            logger.info(
                "Client tried to write RCB parameter even though URCB is reserved by other client or already enabled.");
            // 3 indicates error "object_access_denied"
            writeResponse.setFailure(new DataAccessError(3L));
            return writeResponse;
          }

        } else if (nodeName.equals("GI")) {

          if ((urcb.reserved == this)
              && urcb.enabled
              && ((BdaTriggerConditions) urcb.getChild("TrgOps")).isGeneralInterrogation()) {
            urcb.generalInterrogation();
            return writeSuccess;
          } else {
            logger.info(
                "Client tried to initiate a general interrogation even though URCB is not enabled by this client or general interrogation is not enabled in the trigger options.");
            // 3 indicates error "object_access_denied"
            writeResponse.setFailure(new DataAccessError(3L));
            return writeResponse;
          }

        } else if (nodeName.equals("RptID")
            || nodeName.equals("BufTm")
            || nodeName.equals("TrgOps")
            || nodeName.equals("IntgPd")) {
          if ((urcb.reserved == null || urcb.reserved == this) && !urcb.enabled) {
            ((BasicDataAttribute) modelNode).setValueFrom((BasicDataAttribute) fcModelNodeCopy);
            return writeSuccess;
          } else {
            // 3 indicates error "object_access_denied"
            writeResponse.setFailure(new DataAccessError(3L));
            return writeResponse;
          }

        } else {
          // nodes sqnum, ConfRev, and owner may not be read
          // 3 indicates error "object_access_denied"
          writeResponse.setFailure(new DataAccessError(3L));
          return writeResponse;
        }
      }

    } else {

      return null;
    }
  }

  private int serviceErrorToMmsError(ServiceError e) {

    switch (e.getErrorCode()) {
      case ServiceError.FAILED_DUE_TO_SERVER_CONSTRAINT:
        return 1;
      case ServiceError.INSTANCE_LOCKED_BY_OTHER_CLIENT:
        return 2;
      case ServiceError.ACCESS_VIOLATION:
        return 3;
      case ServiceError.TYPE_CONFLICT:
        return 7;
      case ServiceError.INSTANCE_NOT_AVAILABLE:
        return 10;
      case ServiceError.PARAMETER_VALUE_INCONSISTENT:
        return 11;
      default:
        return 9;
    }
  }

  private GetNameListResponse handleGetDataSetNamesRequest(GetNameListRequest getNameListRequest)
      throws ServiceError {

    BerVisibleString domainSpecific = getNameListRequest.getObjectScope().getDomainSpecific();

    List<String> dsList = null;
    if (domainSpecific == null) {
      dsList = new ArrayList<>(nonPersistentDataSets.size());
      for (String dataSet : nonPersistentDataSets.keySet()) {
        dsList.add(dataSet);
      }
    } else {
      dsList = serverModel.getDataSetNames(domainSpecific.toString());
    }

    insertRef = true;
    if (getNameListRequest.getContinueAfter() != null) {
      continueAfter = getNameListRequest.getContinueAfter().toString();
      insertRef = false;
    }

    ListOfIdentifier listOf = new ListOfIdentifier();
    List<Identifier> identifiers = listOf.getIdentifier();

    int identifierSize = 0;
    boolean moreFollows = false;

    if (dsList != null) {
      for (String dsRef : dsList) {
        if (insertRef == true) {
          if (identifierSize > negotiatedMaxPduSize - 200) {
            moreFollows = true;
            logger.info("maxMMSPduSize reached");
            break;
          }
          identifiers.add(new Identifier(dsRef.getBytes(UTF_8)));
          identifierSize += dsRef.length() + 2;
        } else {
          if (dsRef.equals(continueAfter)) {
            insertRef = true;
          }
        }
      }
    }

    GetNameListResponse getNameListResponse = new GetNameListResponse();
    getNameListResponse.setListOfIdentifier(listOf);
    getNameListResponse.setMoreFollows(new BerBoolean(moreFollows));

    return getNameListResponse;
  }

  private GetNamedVariableListAttributesResponse handleGetDataSetDirectoryRequest(
      ObjectName mmsGetNamedVarListAttReq) throws ServiceError {

    String dataSetReference = convertToDataSetReference(mmsGetNamedVarListAttReq);

    DataSet dataSet;

    if (dataSetReference.startsWith("@")) {
      dataSet = nonPersistentDataSets.get(dataSetReference);
    } else {
      dataSet = serverModel.getDataSet(dataSetReference);
    }

    if (dataSet == null) {
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INAPPROPRIATE,
          "DataSet with that reference is does not exist.");
    }

    VariableDefs variableDefs = new VariableDefs();

    List<VariableDefs.SEQUENCE> listOfVariable = variableDefs.getSEQUENCE();

    for (FcModelNode member : dataSet) {
      listOfVariable.add(member.getMmsVariableDef());
    }

    GetNamedVariableListAttributesResponse getNamedVariableListAttributesResponse =
        new GetNamedVariableListAttributesResponse();
    getNamedVariableListAttributesResponse.setListOfVariable(variableDefs);
    getNamedVariableListAttributesResponse.setMmsDeletable(new BerBoolean(dataSet.isDeletable()));

    return getNamedVariableListAttributesResponse;
  }

  private DefineNamedVariableListResponse handleCreateDataSetRequest(
      DefineNamedVariableListRequest mmsDefineNamedVariableListRequest) throws ServiceError {
    String dataSetReference =
        convertToDataSetReference(mmsDefineNamedVariableListRequest.getVariableListName());
    if (dataSetReference == null) {
      throw new ServiceError(
          ServiceError.PARAMETER_VALUE_INCONSISTENT,
          "handleCreateDataSetRequest: invalid MMS request (No DataSet Name Specified)");
    }

    List<VariableDefs.SEQUENCE> nameList =
        mmsDefineNamedVariableListRequest.getListOfVariable().getSEQUENCE();

    List<FcModelNode> dataSetMembers = new ArrayList<>(nameList.size());

    for (VariableDefs.SEQUENCE variableDef : nameList) {
      dataSetMembers.add(serverModel.getNodeFromVariableDef(variableDef));
    }

    DataSet dataSet = new DataSet(dataSetReference, dataSetMembers, true);

    if (dataSetReference.startsWith("@")) {
      if (nonPersistentDataSets.containsKey(dataSetReference)) {
        throw new ServiceError(
            ServiceError.PARAMETER_VALUE_INAPPROPRIATE, "data set with that name exists already");
      }
      nonPersistentDataSets.put(dataSetReference, dataSet);
    } else {
      serverModel.addDataSet(dataSet);
    }

    return new DefineNamedVariableListResponse();
  }

  private DeleteNamedVariableListResponse handleDeleteDataSetRequest(
      DeleteNamedVariableListRequest mmsDelNamVarListReq) throws ServiceError {
    String dataSetReference =
        convertToDataSetReference(
            mmsDelNamVarListReq.getListOfVariableListName().getObjectName().get(0));

    DeleteNamedVariableListResponse deleteNamedVariableListResponse =
        new DeleteNamedVariableListResponse();

    if (dataSetReference.startsWith("@")) {
      if (nonPersistentDataSets.remove(dataSetReference) == null) {
        deleteNamedVariableListResponse.setNumberMatched(new Unsigned32(0));
        deleteNamedVariableListResponse.setNumberDeleted(new Unsigned32(0));
        return deleteNamedVariableListResponse;
      } else {
        deleteNamedVariableListResponse.setNumberMatched(new Unsigned32(1));
        deleteNamedVariableListResponse.setNumberDeleted(new Unsigned32(1));
        return deleteNamedVariableListResponse;
      }
    } else {
      synchronized (serverModel) {
        if (serverModel.removeDataSet(dataSetReference) == null) {
          if (serverModel.getDataSet(dataSetReference) == null) {
            // DataSet with the name does not exist.
            deleteNamedVariableListResponse.setNumberMatched(new Unsigned32(0));
            deleteNamedVariableListResponse.setNumberDeleted(new Unsigned32(0));
            return deleteNamedVariableListResponse;
          } else {
            // DataSet exists but is not deletable
            deleteNamedVariableListResponse.setNumberMatched(new Unsigned32(1));
            deleteNamedVariableListResponse.setNumberDeleted(new Unsigned32(0));
            return deleteNamedVariableListResponse;
          }
        } else {
          deleteNamedVariableListResponse.setNumberMatched(new Unsigned32(1));
          deleteNamedVariableListResponse.setNumberDeleted(new Unsigned32(1));
          return deleteNamedVariableListResponse;
        }
      }
    }
  }

  void close() {
    cleanUpConnection();
    executor.shutdown();
    if (acseAssociation != null) {
      acseAssociation.disconnect();
    }
  }
}

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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.beanit.asn1bean.ber.ReverseByteArrayOutputStream;
import com.beanit.asn1bean.ber.types.BerAny;
import com.beanit.asn1bean.ber.types.BerInteger;
import com.beanit.asn1bean.ber.types.BerObjectIdentifier;
import com.beanit.asn1bean.ber.types.string.BerGraphicString;
import com.beanit.josistack.internal.acse.asn1.AAREApdu;
import com.beanit.josistack.internal.acse.asn1.AARQApdu;
import com.beanit.josistack.internal.acse.asn1.ACSEApdu;
import com.beanit.josistack.internal.acse.asn1.ACSERequirements;
import com.beanit.josistack.internal.acse.asn1.AEQualifier;
import com.beanit.josistack.internal.acse.asn1.AEQualifierForm2;
import com.beanit.josistack.internal.acse.asn1.APTitle;
import com.beanit.josistack.internal.acse.asn1.APTitleForm2;
import com.beanit.josistack.internal.acse.asn1.AssociateResult;
import com.beanit.josistack.internal.acse.asn1.AssociateSourceDiagnostic;
import com.beanit.josistack.internal.acse.asn1.AssociationInformation;
import com.beanit.josistack.internal.acse.asn1.AuthenticationValue;
import com.beanit.josistack.internal.acse.asn1.MechanismName;
import com.beanit.josistack.internal.acse.asn1.Myexternal;
import com.beanit.josistack.internal.presentation.asn1.CPAPPDU;
import com.beanit.josistack.internal.presentation.asn1.CPType;
import com.beanit.josistack.internal.presentation.asn1.CalledPresentationSelector;
import com.beanit.josistack.internal.presentation.asn1.CallingPresentationSelector;
import com.beanit.josistack.internal.presentation.asn1.FullyEncodedData;
import com.beanit.josistack.internal.presentation.asn1.ModeSelector;
import com.beanit.josistack.internal.presentation.asn1.PDVList;
import com.beanit.josistack.internal.presentation.asn1.PresentationContextDefinitionList;
import com.beanit.josistack.internal.presentation.asn1.PresentationContextDefinitionResultList;
import com.beanit.josistack.internal.presentation.asn1.PresentationContextIdentifier;
import com.beanit.josistack.internal.presentation.asn1.RespondingPresentationSelector;
import com.beanit.josistack.internal.presentation.asn1.UserData;
import com.beanit.jositransport.ClientTSap;
import com.beanit.jositransport.TConnection;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

public final class AcseAssociation {

  // private static final Logger logger = LoggerFactory.getLogger(AcseAssociation.class);

  private static final PresentationContextDefinitionList context_list =
      new PresentationContextDefinitionList(
          new byte[] {
            (byte) 0x23,
            (byte) 0x30,
            (byte) 0x0f,
            (byte) 0x02,
            (byte) 0x01,
            (byte) 0x01,
            (byte) 0x06,
            (byte) 0x04,
            (byte) 0x52,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x01,
            (byte) 0x30,
            (byte) 0x04,
            (byte) 0x06,
            (byte) 0x02,
            (byte) 0x51,
            (byte) 0x01,
            (byte) 0x30,
            (byte) 0x10,
            (byte) 0x02,
            (byte) 0x01,
            (byte) 0x03,
            (byte) 0x06,
            (byte) 0x05,
            (byte) 0x28,
            (byte) 0xca,
            (byte) 0x22,
            (byte) 0x02,
            (byte) 0x01,
            (byte) 0x30,
            (byte) 0x04,
            (byte) 0x06,
            (byte) 0x02,
            (byte) 0x51,
            (byte) 0x01
          });
  private static final PresentationContextIdentifier acsePresentationContextId =
      new PresentationContextIdentifier(new byte[] {(byte) 0x01, (byte) 0x01});
  private static final ModeSelector normalModeSelector = new ModeSelector();
  private static final PresentationContextDefinitionResultList presentationResultList =
      new PresentationContextDefinitionResultList(
          new byte[] {
            (byte) 0x12,
            (byte) 0x30,
            (byte) 0x07,
            (byte) 0x80,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x81,
            (byte) 0x02,
            (byte) 0x51,
            (byte) 0x01,
            (byte) 0x30,
            (byte) 0x07,
            (byte) 0x80,
            (byte) 0x01,
            (byte) 0x00,
            (byte) 0x81,
            (byte) 0x02,
            (byte) 0x51,
            (byte) 0x01
          });
  private static final AssociateResult aareAccepted =
      new AssociateResult(new byte[] {(byte) 0x01, (byte) 0x00});
  private static final AssociateSourceDiagnostic associateSourceDiagnostic =
      new AssociateSourceDiagnostic(
          new byte[] {(byte) 0xa1, (byte) 0x03, (byte) 0x02, (byte) 0x01, (byte) 0x00});
  // is always equal to 1.0.9506.2.3 (MMS)
  private static final BerObjectIdentifier application_context_name =
      new BerObjectIdentifier(
          new byte[] {
            (byte) 0x05, (byte) 0x28, (byte) 0xca, (byte) 0x22, (byte) 0x02, (byte) 0x03
          });
  private static final BerObjectIdentifier directReference =
      new BerObjectIdentifier(new byte[] {(byte) 0x02, (byte) 0x51, (byte) 0x01});
  private static final BerInteger indirectReference =
      new BerInteger(new byte[] {(byte) 0x01, (byte) 0x03});
  private static final MechanismName default_mechanism_name =
      new MechanismName(new byte[] {0x03, 0x52, 0x03, 0x01});

  static {
    normalModeSelector.setModeValue(new BerInteger(BigInteger.ONE));
  }

  private final RespondingPresentationSelector pSelLocalBerOctetString;
  private boolean connected = false;
  private TConnection tConnection;
  private ByteBuffer associateResponseAPDU = null;

  AcseAssociation(TConnection tConnection, byte[] pSelLocal) {
    this.tConnection = tConnection;
    pSelLocalBerOctetString = new RespondingPresentationSelector(pSelLocal);
  }

  private static ByteBuffer decodePConResponse(ByteBuffer ppdu) throws IOException {

    CPAPPDU cpa_ppdu = new CPAPPDU();
    InputStream iStream = new ByteBufferInputStream(ppdu);
    cpa_ppdu.decode(iStream);

    iStream =
        new ByteArrayInputStream(
            cpa_ppdu
                .getNormalModeParameters()
                .getUserData()
                .getFullyEncodedData()
                .getPDVList()
                .get(0)
                .getPresentationDataValues()
                .getSingleASN1Type()
                .value);

    ACSEApdu acseApdu = new ACSEApdu();
    acseApdu.decode(iStream, null);
    return ByteBuffer.wrap(
        acseApdu
            .getAare()
            .getUserInformation()
            .getMyexternal()
            .get(0)
            .getEncoding()
            .getSingleASN1Type()
            .value);
  }

  private static UserData getPresentationUserDataField(byte[] userDataBytes) {
    PDVList.PresentationDataValues presDataValues = new PDVList.PresentationDataValues();
    presDataValues.setSingleASN1Type(new BerAny(userDataBytes));
    PDVList pdvList = new PDVList();
    pdvList.setPresentationContextIdentifier(acsePresentationContextId);
    pdvList.setPresentationDataValues(presDataValues);

    FullyEncodedData fullyEncodedData = new FullyEncodedData();
    List<PDVList> pdvListList = fullyEncodedData.getPDVList();
    pdvListList.add(pdvList);

    UserData userData = new UserData();
    userData.setFullyEncodedData(fullyEncodedData);
    return userData;
  }

  public static String getSPDUTypeString(byte spduType) {
    switch (spduType) {
      case 0:
        return "EXCEPTION REPORT (ER)";
      case 1:
        return "DATA TRANSFER (DT)";
      case 2:
        return "PLEASE TOKENS (PT)";
      case 5:
        return "EXPEDITED (EX)";
      case 7:
        return "PREPARE (PR)";
      case 8:
        return "NOT FINISHED (NF)";
      case 9:
        return "FINISH (FN)";
      case 10:
        return "DISCONNECT (DN)";
      case 12:
        return "REFUSE (RF)";
      case 13:
        return "CONNECT (CN)";
      case 14:
        return "ACCEPT (AC)";
      case 15:
        return "CONNECT DATA OVERFLOW (CDO)";
      case 16:
        return "OVERFLOW ACCEPT (OA)";
      case 21:
        return "GIVE TOKENS CONFIRM (GTC)";
      case 22:
        return "GIVE TOKENS ACK (GTA)";
      case 25:
        return "ABORT (AB)";
      case 26:
        return "ABORT ACCEPT (AA)";
      case 29:
        return "ACTIVITY RESUME (AR)";
      case 33:
        return "TYPED DATA (TD)";
      case 34:
        return "RESYNCHRONIZE ACK (RA)";
      case 41:
        return "MAJOR SYNC POINT (MAP)";
      case 42:
        return "MAJOR SYNC ACK (MAA)";
      case 45:
        return "ACTIVITY START (AS)";
      case 48:
        return "EXCEPTION DATA (ED)";
      case 49:
        return "MINOR SYNC POINT (MIP)";
      case 50:
        return "MINOR SYNC ACK (MIA)";
      case 53:
        return "RESYNCHRONIZE (RS)";
      case 57:
        return "ACTIVITY DISCARD (AD)";
      case 58:
        return "ACTIVITY DISCARD ACK (ADA)";
      case 61:
        return "CAPABILITY DATA (CD)";
      case 62:
        return "CAPABILITY DATA ACK (CDA)";
      case 64:
        return "UNIT DATA (UD)";
      default:
        return "<unknown SPDU type>";
    }
  }

  /**
   * A server that got an Association Request Indication may use this function to accept the
   * association.
   *
   * @param payload the payload to send with the accept message
   * @throws IOException if an error occures accepting the association
   */
  public void accept(ByteBuffer payload) throws IOException {

    BerAny anyPayload =
        new BerAny(
            Arrays.copyOfRange(
                payload.array(), payload.arrayOffset() + payload.position(), payload.limit()));

    Myexternal.Encoding encoding = new Myexternal.Encoding();
    encoding.setSingleASN1Type(anyPayload);

    Myexternal myExternal = new Myexternal();
    myExternal.setDirectReference(directReference);
    myExternal.setIndirectReference(indirectReference);
    myExternal.setEncoding(encoding);

    AssociationInformation userInformation = new AssociationInformation();
    List<Myexternal> externalList = userInformation.getMyexternal();
    externalList.add(myExternal);

    AAREApdu aare = new AAREApdu();
    aare.setApplicationContextName(application_context_name);
    aare.setResult(aareAccepted);
    aare.setResultSourceDiagnostic(associateSourceDiagnostic);
    aare.setUserInformation(userInformation);

    ACSEApdu acse = new ACSEApdu();
    acse.setAare(aare);

    ReverseByteArrayOutputStream reverseOStream = new ReverseByteArrayOutputStream(100, true);
    acse.encode(reverseOStream);

    UserData userData = getPresentationUserDataField(reverseOStream.getArray());
    CPAPPDU.NormalModeParameters normalModeParameters = new CPAPPDU.NormalModeParameters();
    normalModeParameters.setRespondingPresentationSelector(pSelLocalBerOctetString);
    normalModeParameters.setPresentationContextDefinitionResultList(presentationResultList);
    normalModeParameters.setUserData(userData);

    CPAPPDU cpaPPdu = new CPAPPDU();
    cpaPPdu.setModeSelector(normalModeSelector);
    cpaPPdu.setNormalModeParameters(normalModeParameters);

    reverseOStream.reset();
    cpaPPdu.encode(reverseOStream, true);

    List<byte[]> ssduList = new ArrayList<>();
    List<Integer> ssduOffsets = new ArrayList<>();
    List<Integer> ssduLengths = new ArrayList<>();

    ssduList.add(reverseOStream.buffer);
    ssduOffsets.add(reverseOStream.index + 1);
    ssduLengths.add(reverseOStream.buffer.length - (reverseOStream.index + 1));

    writeSessionAccept(ssduList, ssduOffsets, ssduLengths);

    connected = true;
  }

  private void writeSessionAccept(
      List<byte[]> ssdu, List<Integer> ssduOffsets, List<Integer> ssduLengths) throws IOException {
    byte[] sduAcceptHeader = new byte[20];
    int idx = 0;

    int ssduLength = 0;
    for (int ssduElementLength : ssduLengths) {
      ssduLength += ssduElementLength;
    }

    // write ISO 8327-1 Header
    // SPDU Type: ACCEPT (14)
    sduAcceptHeader[idx++] = 0x0e;
    // Length: length of session user data + 22 ( header data after length
    // field )
    sduAcceptHeader[idx++] = (byte) ((ssduLength + 18) & 0xff);

    // -- start Connect Accept Item
    // Parameter type: Connect Accept Item (5)
    sduAcceptHeader[idx++] = 0x05;
    // Parameter length
    sduAcceptHeader[idx++] = 0x06;

    // Protocol options:
    // Parameter Type: Protocol Options (19)
    sduAcceptHeader[idx++] = 0x13;
    // Parameter length
    sduAcceptHeader[idx++] = 0x01;
    // flags: (.... ...0 = Able to receive extended concatenated SPDU:
    // False)
    sduAcceptHeader[idx++] = 0x00;

    // Version number:
    // Parameter type: Version Number (22)
    sduAcceptHeader[idx++] = 0x16;
    // Parameter length
    sduAcceptHeader[idx++] = 0x01;
    // flags: (.... ..1. = Protocol Version 2: True)
    sduAcceptHeader[idx++] = 0x02;
    // -- end Connect Accept Item

    // Session Requirement
    // Parameter type: Session Requirement (20)
    sduAcceptHeader[idx++] = 0x14;
    // Parameter length
    sduAcceptHeader[idx++] = 0x02;
    // flags: (.... .... .... ..1. = Duplex functional unit: True)
    sduAcceptHeader[idx++] = 0x00;
    sduAcceptHeader[idx++] = 0x02;

    // Called Session Selector
    // Parameter type: Called Session Selector (52)
    sduAcceptHeader[idx++] = 0x34;
    // Parameter length
    sduAcceptHeader[idx++] = 0x02;
    // Called Session Selector
    sduAcceptHeader[idx++] = 0x00;
    sduAcceptHeader[idx++] = 0x01;

    // Session user data
    // Parameter type: Session user data (193)
    sduAcceptHeader[idx++] = (byte) 0xc1;

    // Parameter length
    sduAcceptHeader[idx] = (byte) ssduLength;

    ssdu.add(0, sduAcceptHeader);
    ssduOffsets.add(0, 0);
    ssduLengths.add(0, sduAcceptHeader.length);

    tConnection.send(ssdu, ssduOffsets, ssduLengths);
  }

  public ByteBuffer getAssociateResponseAPdu() {
    ByteBuffer returnBuffer = associateResponseAPDU;
    associateResponseAPDU = null;
    return returnBuffer;
  }

  void startAssociation(
      ByteBuffer payload,
      InetAddress address,
      int port,
      InetAddress localAddr,
      int localPort,
      String authenticationParameter,
      byte[] sSelRemote,
      byte[] sSelLocal,
      byte[] pSelRemote,
      ClientTSap tSAP,
      int[] apTitleCalled,
      int[] apTitleCalling,
      int aeQualifierCalled,
      int aeQualifierCalling)
      throws IOException {
    if (connected) {
      throw new IOException();
    }

    APTitle called_ap_title = new APTitle();
    called_ap_title.setApTitleForm2(new APTitleForm2(apTitleCalled));
    APTitle calling_ap_title = new APTitle();
    calling_ap_title.setApTitleForm2(new APTitleForm2(apTitleCalling));

    AEQualifier called_ae_qualifier = new AEQualifier();
    called_ae_qualifier.setAeQualifierForm2(new AEQualifierForm2(aeQualifierCalled));
    AEQualifier calling_ae_qualifier = new AEQualifier();
    calling_ae_qualifier.setAeQualifierForm2(new AEQualifierForm2(aeQualifierCalling));

    Myexternal.Encoding encoding = new Myexternal.Encoding();
    encoding.setSingleASN1Type(
        new BerAny(
            Arrays.copyOfRange(
                payload.array(), payload.arrayOffset() + payload.position(), payload.limit())));

    Myexternal myExternal = new Myexternal();
    myExternal.setDirectReference(directReference);
    myExternal.setIndirectReference(indirectReference);
    myExternal.setEncoding(encoding);

    AssociationInformation userInformation = new AssociationInformation();
    List<Myexternal> externalList = userInformation.getMyexternal();
    externalList.add(myExternal);

    ACSERequirements sender_acse_requirements = null;
    MechanismName mechanism_name = null;
    AuthenticationValue authentication_value = null;
    if (authenticationParameter != null) {
      sender_acse_requirements =
          new ACSERequirements(new byte[] {(byte) 0x02, (byte) 0x07, (byte) 0x80});
      mechanism_name = default_mechanism_name;
      authentication_value = new AuthenticationValue();
      authentication_value.setCharstring(
          new BerGraphicString(authenticationParameter.getBytes(UTF_8)));
    }

    AARQApdu aarq = new AARQApdu();
    aarq.setApplicationContextName(application_context_name);
    aarq.setCalledAPTitle(called_ap_title);
    aarq.setCalledAEQualifier(called_ae_qualifier);
    aarq.setCallingAPTitle(calling_ap_title);
    aarq.setCallingAEQualifier(calling_ae_qualifier);
    aarq.setSenderAcseRequirements(sender_acse_requirements);
    aarq.setMechanismName(mechanism_name);
    aarq.setCallingAuthenticationValue(authentication_value);
    aarq.setUserInformation(userInformation);

    ACSEApdu acse = new ACSEApdu();
    acse.setAarq(aarq);

    ReverseByteArrayOutputStream reverseOStream = new ReverseByteArrayOutputStream(200, true);
    acse.encode(reverseOStream);

    UserData userData = getPresentationUserDataField(reverseOStream.getArray());

    CPType.NormalModeParameters normalModeParameter = new CPType.NormalModeParameters();
    normalModeParameter.setCallingPresentationSelector(
        new CallingPresentationSelector(pSelLocalBerOctetString.value));
    normalModeParameter.setCalledPresentationSelector(new CalledPresentationSelector(pSelRemote));
    normalModeParameter.setPresentationContextDefinitionList(context_list);
    normalModeParameter.setUserData(userData);

    CPType cpType = new CPType();
    cpType.setModeSelector(normalModeSelector);
    cpType.setNormalModeParameters(normalModeParameter);

    reverseOStream.reset();
    cpType.encode(reverseOStream, true);

    List<byte[]> ssduList = new ArrayList<>();
    List<Integer> ssduOffsets = new ArrayList<>();
    List<Integer> ssduLengths = new ArrayList<>();

    ssduList.add(reverseOStream.buffer);
    ssduOffsets.add(reverseOStream.index + 1);
    ssduLengths.add(reverseOStream.buffer.length - (reverseOStream.index + 1));

    ByteBuffer res =
        startSConnection(
            ssduList,
            ssduOffsets,
            ssduLengths,
            address,
            port,
            localAddr,
            localPort,
            tSAP,
            sSelRemote,
            sSelLocal);

    associateResponseAPDU = decodePConResponse(res);
  }

  private ByteBuffer startSConnection(
      List<byte[]> ssduList,
      List<Integer> ssduOffsets,
      List<Integer> ssduLengths,
      InetAddress address,
      int port,
      InetAddress localAddr,
      int localPort,
      ClientTSap tSAP,
      byte[] sSelRemote,
      byte[] sSelLocal)
      throws IOException {
    if (connected) {
      throw new IOException();
    }

    byte[] spduHeader = new byte[24];
    int idx = 0;
    // byte[] res = null;

    int ssduLength = 0;
    for (int ssduElementLength : ssduLengths) {
      ssduLength += ssduElementLength;
    }

    // write ISO 8327-1 Header
    // SPDU Type: CONNECT (13)
    spduHeader[idx++] = 0x0d;
    // Length: length of session user data + 22 ( header data after
    // length field )
    spduHeader[idx++] = (byte) ((ssduLength + 22) & 0xff);

    // -- start Connect Accept Item
    // Parameter type: Connect Accept Item (5)
    spduHeader[idx++] = 0x05;
    // Parameter length
    spduHeader[idx++] = 0x06;

    // Protocol options:
    // Parameter Type: Protocol Options (19)
    spduHeader[idx++] = 0x13;
    // Parameter length
    spduHeader[idx++] = 0x01;
    // flags: (.... ...0 = Able to receive extended concatenated SPDU:
    // False)
    spduHeader[idx++] = 0x00;

    // Version number:
    // Parameter type: Version Number (22)
    spduHeader[idx++] = 0x16;
    // Parameter length
    spduHeader[idx++] = 0x01;
    // flags: (.... ..1. = Protocol Version 2: True)
    spduHeader[idx++] = 0x02;
    // -- end Connect Accept Item

    // Session Requirement
    // Parameter type: Session Requirement (20)
    spduHeader[idx++] = 0x14;
    // Parameter length
    spduHeader[idx++] = 0x02;
    // flags: (.... .... .... ..1. = Duplex functional unit: True)
    spduHeader[idx++] = 0x00;
    spduHeader[idx++] = 0x02;

    // Calling Session Selector
    // Parameter type: Calling Session Selector (51)
    spduHeader[idx++] = 0x33;
    // Parameter length
    spduHeader[idx++] = 0x02;
    // Calling Session Selector
    spduHeader[idx++] = sSelRemote[0];
    spduHeader[idx++] = sSelRemote[1];

    // Called Session Selector
    // Parameter type: Called Session Selector (52)
    spduHeader[idx++] = 0x34;
    // Parameter length
    spduHeader[idx++] = 0x02;
    // Called Session Selector
    spduHeader[idx++] = sSelLocal[0];
    spduHeader[idx++] = sSelLocal[1];

    // Session user data
    // Parameter type: Session user data (193)
    spduHeader[idx++] = (byte) 0xc1;
    // Parameter length
    spduHeader[idx] = (byte) (ssduLength & 0xff);
    // write session user data

    ssduList.add(0, spduHeader);
    ssduOffsets.add(0, 0);
    ssduLengths.add(0, spduHeader.length);

    tConnection = tSAP.connectTo(address, port, localAddr, localPort);

    tConnection.send(ssduList, ssduOffsets, ssduLengths);

    // TODO how much should be allocated here?
    ByteBuffer pduBuffer = ByteBuffer.allocate(500);

    try {
      tConnection.receive(pduBuffer);
    } catch (TimeoutException e) {
      throw new IOException("ResponseTimeout waiting for connection response.", e);
    }

    // read ISO 8327-1 Header
    // SPDU Type: ACCEPT (14)
    byte spduType = pduBuffer.get();
    if (spduType != 0x0e) {
      throw new IOException(
          "ISO 8327-1 header wrong SPDU type, expected ACCEPT (14), got "
              + getSPDUTypeString(spduType)
              + " ("
              + spduType
              + ")");
    }
    pduBuffer.get(); // skip length byte

    parameter_loop:
    while (true) {
      // read parameter type
      int parameterType = pduBuffer.get() & 0xff;
      // read parameter length
      int parameterLength = pduBuffer.get() & 0xff;

      switch (parameterType) {
          // Connect Accept Item (5)
        case 0x05:
          int bytesToRead = parameterLength;
          while (bytesToRead > 0) {
            // read parameter type
            int ca_parameterType = pduBuffer.get();
            // read parameter length
            // int ca_parameterLength = res[idx++];
            pduBuffer.get();

            bytesToRead -= 2;

            switch (ca_parameterType & 0xff) {
                // Protocol Options (19)
              case 0x13:
                // flags: .... ...0 = Able to receive extended
                // concatenated SPDU: False
                byte protocolOptions = pduBuffer.get();
                if (protocolOptions != 0x00) {
                  throw new IOException(
                      "SPDU Connect Accept Item/Protocol Options is "
                          + protocolOptions
                          + ", expected 0");
                }

                bytesToRead--;
                break;
                // Version Number
              case 0x16:
                // flags .... ..1. = Protocol Version 2: True
                byte versionNumber = pduBuffer.get();
                if (versionNumber != 0x02) {
                  throw new IOException(
                      "SPDU Connect Accept Item/Version Number is "
                          + versionNumber
                          + ", expected 2");
                }

                bytesToRead--;
                break;
              default:
                throw new IOException(
                    "SPDU Connect Accept Item: parameter not implemented: " + ca_parameterType);
            }
          }
          break;
          // Session Requirement (20)
        case 0x14:
          // flags: (.... .... .... ..1. = Duplex functional unit: True)
          long sessionRequirement = extractInteger(pduBuffer, parameterLength);
          if (sessionRequirement != 0x02) {
            throw new IOException(
                "SPDU header parameter 'Session Requirement (20)' is "
                    + sessionRequirement
                    + ", expected 2");
          }
          break;
          // Calling Session Selector (51)
        case 0x33:
          long css = extractInteger(pduBuffer, parameterLength);
          if (css != 0x01) {
            throw new IOException(
                "SPDU header parameter 'Calling Session Selector (51)' is " + css + ", expected 1");
          }
          break;
          // Called Session Selector (52)
        case 0x34:
          long calledSessionSelector = extractInteger(pduBuffer, parameterLength);
          if (calledSessionSelector != 0x01) {
            throw new IOException(
                "SPDU header parameter 'Called Session Selector (52)' is "
                    + calledSessionSelector
                    + ", expected 1");
          }
          break;
          // Session user data (193)
        case 0xc1:
          break parameter_loop;
        default:
          throw new IOException("SPDU header parameter type " + parameterType + " not implemented");
      }
    }

    // got correct ACCEPT (AC) from the server

    connected = true;

    return pduBuffer;
  }

  public void send(ByteBuffer payload) throws IOException {

    List<byte[]> ssduList = new ArrayList<>();
    List<Integer> ssduOffsets = new ArrayList<>();
    List<Integer> ssduLengths = new ArrayList<>();

    encodePresentationLayer(payload, ssduList, ssduOffsets, ssduLengths);

    encodeSessionLayer(ssduList, ssduOffsets, ssduLengths);

    tConnection.send(ssduList, ssduOffsets, ssduLengths);
  }

  private void encodePresentationLayer(
      ByteBuffer payload,
      List<byte[]> ssduList,
      List<Integer> ssduOffsets,
      List<Integer> ssduLengths)
      throws IOException {
    PDVList pdv_list = new PDVList();
    pdv_list.setPresentationContextIdentifier(new PresentationContextIdentifier(3L));

    PDVList.PresentationDataValues presentationDataValues = new PDVList.PresentationDataValues();
    presentationDataValues.setSingleASN1Type(
        new BerAny(
            Arrays.copyOfRange(
                payload.array(), payload.arrayOffset() + payload.position(), payload.limit())));
    pdv_list.setPresentationDataValues(presentationDataValues);

    FullyEncodedData fully_encoded_data = new FullyEncodedData();
    List<PDVList> pdv_list_list = fully_encoded_data.getPDVList();
    pdv_list_list.add(pdv_list);

    UserData user_data = new UserData();
    user_data.setFullyEncodedData(fully_encoded_data);

    ReverseByteArrayOutputStream reverseOStream = new ReverseByteArrayOutputStream(200, true);
    user_data.encode(reverseOStream);

    ssduList.add(reverseOStream.buffer);
    ssduOffsets.add(reverseOStream.index + 1);
    ssduLengths.add(reverseOStream.buffer.length - (reverseOStream.index + 1));
  }

  private void encodeSessionLayer(
      List<byte[]> ssduList, List<Integer> ssduOffsets, List<Integer> ssduLengths) {

    byte[] spduHeader = new byte[4];
    // --write iso 8327-1 Header--
    // write SPDU Type: give tokens PDU
    spduHeader[0] = 0x01;
    // length 0
    spduHeader[1] = 0;
    // write SPDU Type: DATA TRANSFER (DT)
    spduHeader[2] = 0x01;
    // length 0
    spduHeader[3] = 0;

    ssduList.add(0, spduHeader);
    ssduOffsets.add(0, 0);
    ssduLengths.add(0, spduHeader.length);
  }

  /**
   * Listens for a new PDU and writes it into the given buffer. Decodes all ACSE and lower layer
   * headers. The resulting buffer's position points to the beginning of the ACSE SDU. The limit
   * will point to the byte after the last byte of the ACSE SDU.
   *
   * @param pduBuffer buffer to write the received pdu into
   * @return the received PDU
   * @throws DecodingException if a decoding error occurs
   * @throws IOException if a non recoverable error occurs. Afterwards the association should be
   *     closed by the user
   * @throws TimeoutException if a timeout occurs
   */
  public byte[] receive(ByteBuffer pduBuffer)
      throws DecodingException, IOException, TimeoutException {
    if (!connected) {
      throw new IllegalStateException("ACSE Association not connected");
    }
    tConnection.receive(pduBuffer);

    decodeSessionLayer(pduBuffer);

    return decodePresentationLayer(pduBuffer);
  }

  private byte[] decodePresentationLayer(ByteBuffer pduBuffer) throws DecodingException {
    // decode PPDU header
    UserData user_data = new UserData();

    try {
      user_data.decode(new ByteBufferInputStream(pduBuffer), null);
    } catch (IOException e) {
      throw new DecodingException("error decoding PPDU header", e);
    }

    return user_data
        .getFullyEncodedData()
        .getPDVList()
        .get(0)
        .getPresentationDataValues()
        .getSingleASN1Type()
        .value;
  }

  private void decodeSessionLayer(ByteBuffer pduBuffer) throws EOFException, DecodingException {
    int firstByte = pduBuffer.get();

    if (firstByte == 25) {
      // got an ABORT SPDU
      throw new EOFException("Received an ABORT SPDU");
    }

    // -- read ISO 8327-1 header
    // SPDU type: Give tokens PDU (1)
    if (firstByte != 0x01) {
      throw new DecodingException("SPDU header syntax errror: first SPDU type not 1");
    }
    // length
    if (pduBuffer.get() != 0) {
      throw new DecodingException("SPDU header syntax errror: first SPDU type length not 0");
    }
    // SPDU Type: DATA TRANSFER (DT) SPDU (1)
    if (pduBuffer.get() != 0x01) {
      throw new DecodingException("SPDU header syntax errror: second SPDU type not 1");
    }
    // length
    if (pduBuffer.get() != 0) {
      throw new DecodingException("SPDU header syntax errror: second SPDU type length not 0");
    }
  }

  /**
   * Disconnects by sending a disconnect request at the Transport Layer and then closing the socket.
   */
  public void disconnect() {
    connected = false;
    if (tConnection != null) {
      tConnection.disconnect();
    }
  }

  /** Closes the connection simply by closing the socket. */
  public void close() {
    connected = false;
    if (tConnection != null) {
      tConnection.close();
    }
  }

  private long extractInteger(ByteBuffer buffer, int size) throws IOException {
    switch (size) {
      case 1:
        return buffer.get();
      case 2:
        return buffer.getShort();
      case 4:
        return buffer.getInt();
      case 8:
        return buffer.getLong();
      default:
        throw new IOException("invalid length for reading numeric value");
    }
  }

  ByteBuffer listenForCn(ByteBuffer pduBuffer) throws IOException, TimeoutException {
    if (connected) {
      throw new IllegalStateException("ACSE Association is already connected");
    }
    int parameter;
    int parameterLength;

    tConnection.receive(pduBuffer);
    // start reading ISO 8327-1 header
    // SPDU Type: CONNECT (CN) SPDU (13)
    byte spduType = pduBuffer.get();
    if (spduType != 0x0d) {
      throw new IOException(
          "ISO 8327-1 header wrong SPDU type, expected CONNECT (13), got "
              + getSPDUTypeString(spduType)
              + " ("
              + spduType
              + ")");
    }
    pduBuffer.get(); // skip lenght byte

    parameter_loop:
    while (true) {
      // read parameter code
      parameter = pduBuffer.get() & 0xff;
      // read parameter length
      parameterLength = pduBuffer.get() & 0xff;
      switch (parameter) {
          // Connect Accept Item (5)
        case 0x05:
          int bytesToRead = parameterLength;
          while (bytesToRead > 0) {
            // read parameter type
            int ca_parameterType = pduBuffer.get();
            // read parameter length
            pduBuffer.get();

            bytesToRead -= 2;

            switch (ca_parameterType & 0xff) {
                // Protocol Options (19)
              case 0x13:
                // flags: .... ...0 = Able to receive extended
                // concatenated SPDU: False
                byte protocolOptions = pduBuffer.get();
                if (protocolOptions != 0x00) {
                  throw new IOException(
                      "SPDU Connect Accept Item/Protocol Options is "
                          + protocolOptions
                          + ", expected 0");
                }

                bytesToRead--;
                break;
                // Version Number
              case 0x16:
                // flags .... ..1. = Protocol Version 2: True
                byte versionNumber = pduBuffer.get();
                if (versionNumber != 0x02) {
                  throw new IOException(
                      "SPDU Connect Accept Item/Version Number is "
                          + versionNumber
                          + ", expected 2");
                }

                bytesToRead--;
                break;
              default:
                throw new IOException(
                    "SPDU Connect Accept Item: parameter not implemented: " + ca_parameterType);
            }
          }
          break;
          // Session Requirement (20)
        case 0x14:
          // flags: (.... .... .... ..1. = Duplex functional unit: True)
          long sessionRequirement = extractInteger(pduBuffer, parameterLength);
          if (sessionRequirement != 0x02) {
            throw new IOException(
                "SPDU header parameter 'Session Requirement (20)' is "
                    + sessionRequirement
                    + ", expected 2");
          }
          break;
          // Calling Session Selector (51)
        case 0x33:
          extractInteger(pduBuffer, parameterLength);
          break;
          // Called Session Selector (52)
        case 0x34:
          long calledSessionSelector = extractInteger(pduBuffer, parameterLength);
          if (calledSessionSelector != 0x01) {
            throw new IOException(
                "SPDU header parameter 'Called Session Selector (52)' is "
                    + calledSessionSelector
                    + ", expected 1");
          }
          break;
          // Session user data (193)
        case 0xc1:
          break parameter_loop;
        default:
          throw new IOException("SPDU header parameter type " + parameter + " not implemented");
      }
    }

    CPType cpType = new CPType();
    InputStream iStream = new ByteBufferInputStream(pduBuffer);
    cpType.decode(iStream, true);

    iStream =
        new ByteArrayInputStream(
            cpType
                .getNormalModeParameters()
                .getUserData()
                .getFullyEncodedData()
                .getPDVList()
                .get(0)
                .getPresentationDataValues()
                .getSingleASN1Type()
                .value);

    ACSEApdu acseApdu = new ACSEApdu();
    acseApdu.decode(iStream, null);
    return ByteBuffer.wrap(
        acseApdu
            .getAarq()
            .getUserInformation()
            .getMyexternal()
            .get(0)
            .getEncoding()
            .getSingleASN1Type()
            .value);
  }

  public int getMessageTimeout() {
    return tConnection.getMessageTimeout();
  }

  public void setMessageTimeout(int i) {
    tConnection.setMessageTimeout(i);
  }
}

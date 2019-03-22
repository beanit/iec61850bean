/** This class file was automatically generated by jASN1 v1.9.1-SNAPSHOT (http://www.openmuc.org) */
package org.openmuc.openiec61850.internal.mms.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;

public class ConfirmedErrorPDU implements Serializable {

  public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
  private static final long serialVersionUID = 1L;
  public byte[] code = null;
  private Unsigned32 invokeID = null;
  private Unsigned32 modifierPosition = null;
  private ServiceError serviceError = null;

  public ConfirmedErrorPDU() {}

  public ConfirmedErrorPDU(byte[] code) {
    this.code = code;
  }

  public Unsigned32 getInvokeID() {
    return invokeID;
  }

  public void setInvokeID(Unsigned32 invokeID) {
    this.invokeID = invokeID;
  }

  public Unsigned32 getModifierPosition() {
    return modifierPosition;
  }

  public void setModifierPosition(Unsigned32 modifierPosition) {
    this.modifierPosition = modifierPosition;
  }

  public ServiceError getServiceError() {
    return serviceError;
  }

  public void setServiceError(ServiceError serviceError) {
    this.serviceError = serviceError;
  }

  public int encode(OutputStream os) throws IOException {
    return encode(os, true);
  }

  public int encode(OutputStream os, boolean withTag) throws IOException {

    if (code != null) {
      for (int i = code.length - 1; i >= 0; i--) {
        os.write(code[i]);
      }
      if (withTag) {
        return tag.encode(os) + code.length;
      }
      return code.length;
    }

    int codeLength = 0;
    codeLength += serviceError.encode(os, false);
    // write tag: CONTEXT_CLASS, CONSTRUCTED, 2
    os.write(0xA2);
    codeLength += 1;

    if (modifierPosition != null) {
      codeLength += modifierPosition.encode(os, false);
      // write tag: CONTEXT_CLASS, PRIMITIVE, 1
      os.write(0x81);
      codeLength += 1;
    }

    codeLength += invokeID.encode(os, false);
    // write tag: CONTEXT_CLASS, PRIMITIVE, 0
    os.write(0x80);
    codeLength += 1;

    codeLength += BerLength.encodeLength(os, codeLength);

    if (withTag) {
      codeLength += tag.encode(os);
    }

    return codeLength;
  }

  public int decode(InputStream is) throws IOException {
    return decode(is, true);
  }

  public int decode(InputStream is, boolean withTag) throws IOException {
    int codeLength = 0;
    int subCodeLength = 0;
    BerTag berTag = new BerTag();

    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

    BerLength length = new BerLength();
    codeLength += length.decode(is);

    int totalLength = length.val;
    codeLength += totalLength;

    subCodeLength += berTag.decode(is);
    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
      invokeID = new Unsigned32();
      subCodeLength += invokeID.decode(is, false);
      subCodeLength += berTag.decode(is);
    } else {
      throw new IOException("Tag does not match the mandatory sequence element tag.");
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
      modifierPosition = new Unsigned32();
      subCodeLength += modifierPosition.decode(is, false);
      subCodeLength += berTag.decode(is);
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 2)) {
      serviceError = new ServiceError();
      subCodeLength += serviceError.decode(is, false);
      if (subCodeLength == totalLength) {
        return codeLength;
      }
    }
    throw new IOException(
        "Unexpected end of sequence, length tag: "
            + totalLength
            + ", actual sequence length: "
            + subCodeLength);
  }

  public void encodeAndSave(int encodingSizeGuess) throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(encodingSizeGuess);
    encode(os, false);
    code = os.getArray();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendAsString(sb, 0);
    return sb.toString();
  }

  public void appendAsString(StringBuilder sb, int indentLevel) {

    sb.append("{");
    sb.append("\n");
    for (int i = 0; i < indentLevel + 1; i++) {
      sb.append("\t");
    }
    if (invokeID != null) {
      sb.append("invokeID: ").append(invokeID);
    } else {
      sb.append("invokeID: <empty-required-field>");
    }

    if (modifierPosition != null) {
      sb.append(",\n");
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("modifierPosition: ").append(modifierPosition);
    }

    sb.append(",\n");
    for (int i = 0; i < indentLevel + 1; i++) {
      sb.append("\t");
    }
    if (serviceError != null) {
      sb.append("serviceError: ");
      serviceError.appendAsString(sb, indentLevel + 1);
    } else {
      sb.append("serviceError: <empty-required-field>");
    }

    sb.append("\n");
    for (int i = 0; i < indentLevel; i++) {
      sb.append("\t");
    }
    sb.append("}");
  }
}

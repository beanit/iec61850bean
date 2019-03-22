/** This class file was automatically generated by jASN1 v1.9.0 (http://www.openmuc.org) */
package org.openmuc.josistack.internal.presentation.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;

public class FullyEncodedData implements Serializable {

  public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
  private static final long serialVersionUID = 1L;
  public byte[] code = null;
  private List<PDVList> seqOf = null;

  public FullyEncodedData() {
    seqOf = new ArrayList<>();
  }

  public FullyEncodedData(byte[] code) {
    this.code = code;
  }

  public List<PDVList> getPDVList() {
    if (seqOf == null) {
      seqOf = new ArrayList<>();
    }
    return seqOf;
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
    for (int i = (seqOf.size() - 1); i >= 0; i--) {
      codeLength += seqOf.get(i).encode(os, true);
    }

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
    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

    BerLength length = new BerLength();
    codeLength += length.decode(is);
    int totalLength = length.val;

    while (subCodeLength < totalLength) {
      PDVList element = new PDVList();
      subCodeLength += element.decode(is, true);
      seqOf.add(element);
    }
    if (subCodeLength != totalLength) {
      throw new IOException(
          "Decoded SequenceOf or SetOf has wrong length. Expected "
              + totalLength
              + " but has "
              + subCodeLength);
    }
    codeLength += subCodeLength;

    return codeLength;
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

    sb.append("{\n");
    for (int i = 0; i < indentLevel + 1; i++) {
      sb.append("\t");
    }
    if (seqOf == null) {
      sb.append("null");
    } else {
      Iterator<PDVList> it = seqOf.iterator();
      if (it.hasNext()) {
        it.next().appendAsString(sb, indentLevel + 1);
        while (it.hasNext()) {
          sb.append(",\n");
          for (int i = 0; i < indentLevel + 1; i++) {
            sb.append("\t");
          }
          it.next().appendAsString(sb, indentLevel + 1);
        }
      }
    }

    sb.append("\n");
    for (int i = 0; i < indentLevel; i++) {
      sb.append("\t");
    }
    sb.append("}");
  }
}

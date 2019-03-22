/** This class file was automatically generated by jASN1 v1.9.0 (http://www.openmuc.org) */
package org.openmuc.josistack.internal.acse.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;

public class ACSEApdu implements Serializable {

  private static final long serialVersionUID = 1L;

  public byte[] code = null;
  private AARQApdu aarq = null;
  private AAREApdu aare = null;
  private RLRQApdu rlrq = null;
  private RLREApdu rlre = null;

  public ACSEApdu() {}

  public ACSEApdu(byte[] code) {
    this.code = code;
  }

  public AARQApdu getAarq() {
    return aarq;
  }

  public void setAarq(AARQApdu aarq) {
    this.aarq = aarq;
  }

  public AAREApdu getAare() {
    return aare;
  }

  public void setAare(AAREApdu aare) {
    this.aare = aare;
  }

  public RLRQApdu getRlrq() {
    return rlrq;
  }

  public void setRlrq(RLRQApdu rlrq) {
    this.rlrq = rlrq;
  }

  public RLREApdu getRlre() {
    return rlre;
  }

  public void setRlre(RLREApdu rlre) {
    this.rlre = rlre;
  }

  public int encode(OutputStream os) throws IOException {

    if (code != null) {
      for (int i = code.length - 1; i >= 0; i--) {
        os.write(code[i]);
      }
      return code.length;
    }

    int codeLength = 0;
    if (rlre != null) {
      codeLength += rlre.encode(os, true);
      return codeLength;
    }

    if (rlrq != null) {
      codeLength += rlrq.encode(os, true);
      return codeLength;
    }

    if (aare != null) {
      codeLength += aare.encode(os, true);
      return codeLength;
    }

    if (aarq != null) {
      codeLength += aarq.encode(os, true);
      return codeLength;
    }

    throw new IOException("Error encoding CHOICE: No element of CHOICE was selected.");
  }

  public int decode(InputStream is) throws IOException {
    return decode(is, null);
  }

  public int decode(InputStream is, BerTag berTag) throws IOException {

    int codeLength = 0;
    BerTag passedTag = berTag;

    if (berTag == null) {
      berTag = new BerTag();
      codeLength += berTag.decode(is);
    }

    if (berTag.equals(AARQApdu.tag)) {
      aarq = new AARQApdu();
      codeLength += aarq.decode(is, false);
      return codeLength;
    }

    if (berTag.equals(AAREApdu.tag)) {
      aare = new AAREApdu();
      codeLength += aare.decode(is, false);
      return codeLength;
    }

    if (berTag.equals(RLRQApdu.tag)) {
      rlrq = new RLRQApdu();
      codeLength += rlrq.decode(is, false);
      return codeLength;
    }

    if (berTag.equals(RLREApdu.tag)) {
      rlre = new RLREApdu();
      codeLength += rlre.decode(is, false);
      return codeLength;
    }

    if (passedTag != null) {
      return 0;
    }

    throw new IOException("Error decoding CHOICE: Tag " + berTag + " matched to no item.");
  }

  public void encodeAndSave(int encodingSizeGuess) throws IOException {
    ReverseByteArrayOutputStream os = new ReverseByteArrayOutputStream(encodingSizeGuess);
    encode(os);
    code = os.getArray();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    appendAsString(sb, 0);
    return sb.toString();
  }

  public void appendAsString(StringBuilder sb, int indentLevel) {

    if (aarq != null) {
      sb.append("aarq: ");
      aarq.appendAsString(sb, indentLevel + 1);
      return;
    }

    if (aare != null) {
      sb.append("aare: ");
      aare.appendAsString(sb, indentLevel + 1);
      return;
    }

    if (rlrq != null) {
      sb.append("rlrq: ");
      rlrq.appendAsString(sb, indentLevel + 1);
      return;
    }

    if (rlre != null) {
      sb.append("rlre: ");
      rlre.appendAsString(sb, indentLevel + 1);
      return;
    }

    sb.append("<none>");
  }
}

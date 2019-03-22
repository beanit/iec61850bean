/** This class file was automatically generated by jASN1 v1.9.1-SNAPSHOT (http://www.openmuc.org) */
package org.openmuc.openiec61850.internal.mms.asn1;

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
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;

public class DeleteNamedVariableListRequest implements Serializable {

  public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
  private static final long serialVersionUID = 1L;
  public byte[] code = null;
  private BerInteger scopeOfDelete = null;
  private ListOfVariableListName listOfVariableListName = null;
  private DomainName domainName = null;
  public DeleteNamedVariableListRequest() {}
  public DeleteNamedVariableListRequest(byte[] code) {
    this.code = code;
  }

  public BerInteger getScopeOfDelete() {
    return scopeOfDelete;
  }

  public void setScopeOfDelete(BerInteger scopeOfDelete) {
    this.scopeOfDelete = scopeOfDelete;
  }

  public ListOfVariableListName getListOfVariableListName() {
    return listOfVariableListName;
  }

  public void setListOfVariableListName(ListOfVariableListName listOfVariableListName) {
    this.listOfVariableListName = listOfVariableListName;
  }

  public DomainName getDomainName() {
    return domainName;
  }

  public void setDomainName(DomainName domainName) {
    this.domainName = domainName;
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
    int sublength;

    if (domainName != null) {
      sublength = domainName.encode(os);
      codeLength += sublength;
      codeLength += BerLength.encodeLength(os, sublength);
      // write tag: CONTEXT_CLASS, CONSTRUCTED, 2
      os.write(0xA2);
      codeLength += 1;
    }

    if (listOfVariableListName != null) {
      codeLength += listOfVariableListName.encode(os, false);
      // write tag: CONTEXT_CLASS, CONSTRUCTED, 1
      os.write(0xA1);
      codeLength += 1;
    }

    if (scopeOfDelete != null) {
      codeLength += scopeOfDelete.encode(os, false);
      // write tag: CONTEXT_CLASS, PRIMITIVE, 0
      os.write(0x80);
      codeLength += 1;
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
    BerTag berTag = new BerTag();

    if (withTag) {
      codeLength += tag.decodeAndCheck(is);
    }

    BerLength length = new BerLength();
    codeLength += length.decode(is);

    int totalLength = length.val;
    codeLength += totalLength;

    if (totalLength == 0) {
      return codeLength;
    }
    subCodeLength += berTag.decode(is);
    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
      scopeOfDelete = new BerInteger();
      subCodeLength += scopeOfDelete.decode(is, false);
      if (subCodeLength == totalLength) {
        return codeLength;
      }
      subCodeLength += berTag.decode(is);
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 1)) {
      listOfVariableListName = new ListOfVariableListName();
      subCodeLength += listOfVariableListName.decode(is, false);
      if (subCodeLength == totalLength) {
        return codeLength;
      }
      subCodeLength += berTag.decode(is);
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 2)) {
      subCodeLength += length.decode(is);
      domainName = new DomainName();
      subCodeLength += domainName.decode(is, null);
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
    boolean firstSelectedElement = true;
    if (scopeOfDelete != null) {
      sb.append("\n");
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("scopeOfDelete: ").append(scopeOfDelete);
      firstSelectedElement = false;
    }

    if (listOfVariableListName != null) {
      if (!firstSelectedElement) {
        sb.append(",\n");
      }
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("listOfVariableListName: ");
      listOfVariableListName.appendAsString(sb, indentLevel + 1);
      firstSelectedElement = false;
    }

    if (domainName != null) {
      if (!firstSelectedElement) {
        sb.append(",\n");
      }
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("domainName: ");
      domainName.appendAsString(sb, indentLevel + 1);
      firstSelectedElement = false;
    }

    sb.append("\n");
    for (int i = 0; i < indentLevel; i++) {
      sb.append("\t");
    }
    sb.append("}");
  }

  public static class ListOfVariableListName implements Serializable {

    public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
    private static final long serialVersionUID = 1L;
    public byte[] code = null;
    private List<ObjectName> seqOf = null;

    public ListOfVariableListName() {
      seqOf = new ArrayList<>();
    }

    public ListOfVariableListName(byte[] code) {
      this.code = code;
    }

    public List<ObjectName> getObjectName() {
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
        codeLength += seqOf.get(i).encode(os);
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
        ObjectName element = new ObjectName();
        subCodeLength += element.decode(is, null);
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
        Iterator<ObjectName> it = seqOf.iterator();
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

  public static class DomainName implements Serializable {

    private static final long serialVersionUID = 1L;

    public byte[] code = null;
    private BasicIdentifier basic = null;

    public DomainName() {}

    public DomainName(byte[] code) {
      this.code = code;
    }

    public BasicIdentifier getBasic() {
      return basic;
    }

    public void setBasic(BasicIdentifier basic) {
      this.basic = basic;
    }

    public int encode(OutputStream os) throws IOException {

      if (code != null) {
        for (int i = code.length - 1; i >= 0; i--) {
          os.write(code[i]);
        }
        return code.length;
      }

      int codeLength = 0;
      if (basic != null) {
        codeLength += basic.encode(os, true);
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

      if (berTag.equals(BerVisibleString.tag)) {
        basic = new BasicIdentifier();
        codeLength += basic.decode(is, false);
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

      if (basic != null) {
        sb.append("basic: ").append(basic);
        return;
      }

      sb.append("<none>");
    }
  }
}

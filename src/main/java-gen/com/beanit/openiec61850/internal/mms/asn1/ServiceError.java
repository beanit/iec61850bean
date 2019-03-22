/** This class file was automatically generated by jASN1 v1.9.1-SNAPSHOT (http://www.openmuc.org) */
package com.beanit.openiec61850.internal.mms.asn1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import org.openmuc.jasn1.ber.BerLength;
import org.openmuc.jasn1.ber.BerTag;
import org.openmuc.jasn1.ber.ReverseByteArrayOutputStream;
import org.openmuc.jasn1.ber.types.BerInteger;
import org.openmuc.jasn1.ber.types.string.BerVisibleString;

public class ServiceError implements Serializable {

  public static final BerTag tag = new BerTag(BerTag.UNIVERSAL_CLASS, BerTag.CONSTRUCTED, 16);
  private static final long serialVersionUID = 1L;
  public byte[] code = null;
  private ErrorClass errorClass = null;
  private BerInteger additionalCode = null;
  private BerVisibleString additionalDescription = null;

  public ServiceError() {}

  public ServiceError(byte[] code) {
    this.code = code;
  }

  public ErrorClass getErrorClass() {
    return errorClass;
  }

  public void setErrorClass(ErrorClass errorClass) {
    this.errorClass = errorClass;
  }

  public BerInteger getAdditionalCode() {
    return additionalCode;
  }

  public void setAdditionalCode(BerInteger additionalCode) {
    this.additionalCode = additionalCode;
  }

  public BerVisibleString getAdditionalDescription() {
    return additionalDescription;
  }

  public void setAdditionalDescription(BerVisibleString additionalDescription) {
    this.additionalDescription = additionalDescription;
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

    if (additionalDescription != null) {
      codeLength += additionalDescription.encode(os, false);
      // write tag: CONTEXT_CLASS, PRIMITIVE, 2
      os.write(0x82);
      codeLength += 1;
    }

    if (additionalCode != null) {
      codeLength += additionalCode.encode(os, false);
      // write tag: CONTEXT_CLASS, PRIMITIVE, 1
      os.write(0x81);
      codeLength += 1;
    }

    sublength = errorClass.encode(os);
    codeLength += sublength;
    codeLength += BerLength.encodeLength(os, sublength);
    // write tag: CONTEXT_CLASS, CONSTRUCTED, 0
    os.write(0xA0);
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
    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.CONSTRUCTED, 0)) {
      subCodeLength += length.decode(is);
      errorClass = new ErrorClass();
      subCodeLength += errorClass.decode(is, null);
      if (subCodeLength == totalLength) {
        return codeLength;
      }
      subCodeLength += berTag.decode(is);
    } else {
      throw new IOException("Tag does not match the mandatory sequence element tag.");
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
      additionalCode = new BerInteger();
      subCodeLength += additionalCode.decode(is, false);
      if (subCodeLength == totalLength) {
        return codeLength;
      }
      subCodeLength += berTag.decode(is);
    }

    if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 2)) {
      additionalDescription = new BerVisibleString();
      subCodeLength += additionalDescription.decode(is, false);
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
    if (errorClass != null) {
      sb.append("errorClass: ");
      errorClass.appendAsString(sb, indentLevel + 1);
    } else {
      sb.append("errorClass: <empty-required-field>");
    }

    if (additionalCode != null) {
      sb.append(",\n");
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("additionalCode: ").append(additionalCode);
    }

    if (additionalDescription != null) {
      sb.append(",\n");
      for (int i = 0; i < indentLevel + 1; i++) {
        sb.append("\t");
      }
      sb.append("additionalDescription: ").append(additionalDescription);
    }

    sb.append("\n");
    for (int i = 0; i < indentLevel; i++) {
      sb.append("\t");
    }
    sb.append("}");
  }

  public static class ErrorClass implements Serializable {

    private static final long serialVersionUID = 1L;

    public byte[] code = null;
    private BerInteger vmdState = null;
    private BerInteger applicationReference = null;
    private BerInteger definition = null;
    private BerInteger resource = null;
    private BerInteger service = null;
    private BerInteger servicePreempt = null;
    private BerInteger timeResolution = null;
    private BerInteger access = null;
    private BerInteger initiate = null;
    private BerInteger conclude = null;
    private BerInteger cancel = null;
    private BerInteger file = null;
    private BerInteger others = null;

    public ErrorClass() {}

    public ErrorClass(byte[] code) {
      this.code = code;
    }

    public BerInteger getVmdState() {
      return vmdState;
    }

    public void setVmdState(BerInteger vmdState) {
      this.vmdState = vmdState;
    }

    public BerInteger getApplicationReference() {
      return applicationReference;
    }

    public void setApplicationReference(BerInteger applicationReference) {
      this.applicationReference = applicationReference;
    }

    public BerInteger getDefinition() {
      return definition;
    }

    public void setDefinition(BerInteger definition) {
      this.definition = definition;
    }

    public BerInteger getResource() {
      return resource;
    }

    public void setResource(BerInteger resource) {
      this.resource = resource;
    }

    public BerInteger getService() {
      return service;
    }

    public void setService(BerInteger service) {
      this.service = service;
    }

    public BerInteger getServicePreempt() {
      return servicePreempt;
    }

    public void setServicePreempt(BerInteger servicePreempt) {
      this.servicePreempt = servicePreempt;
    }

    public BerInteger getTimeResolution() {
      return timeResolution;
    }

    public void setTimeResolution(BerInteger timeResolution) {
      this.timeResolution = timeResolution;
    }

    public BerInteger getAccess() {
      return access;
    }

    public void setAccess(BerInteger access) {
      this.access = access;
    }

    public BerInteger getInitiate() {
      return initiate;
    }

    public void setInitiate(BerInteger initiate) {
      this.initiate = initiate;
    }

    public BerInteger getConclude() {
      return conclude;
    }

    public void setConclude(BerInteger conclude) {
      this.conclude = conclude;
    }

    public BerInteger getCancel() {
      return cancel;
    }

    public void setCancel(BerInteger cancel) {
      this.cancel = cancel;
    }

    public BerInteger getFile() {
      return file;
    }

    public void setFile(BerInteger file) {
      this.file = file;
    }

    public BerInteger getOthers() {
      return others;
    }

    public void setOthers(BerInteger others) {
      this.others = others;
    }

    public int encode(OutputStream os) throws IOException {

      if (code != null) {
        for (int i = code.length - 1; i >= 0; i--) {
          os.write(code[i]);
        }
        return code.length;
      }

      int codeLength = 0;
      if (others != null) {
        codeLength += others.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 12
        os.write(0x8C);
        codeLength += 1;
        return codeLength;
      }

      if (file != null) {
        codeLength += file.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 11
        os.write(0x8B);
        codeLength += 1;
        return codeLength;
      }

      if (cancel != null) {
        codeLength += cancel.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 10
        os.write(0x8A);
        codeLength += 1;
        return codeLength;
      }

      if (conclude != null) {
        codeLength += conclude.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 9
        os.write(0x89);
        codeLength += 1;
        return codeLength;
      }

      if (initiate != null) {
        codeLength += initiate.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 8
        os.write(0x88);
        codeLength += 1;
        return codeLength;
      }

      if (access != null) {
        codeLength += access.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 7
        os.write(0x87);
        codeLength += 1;
        return codeLength;
      }

      if (timeResolution != null) {
        codeLength += timeResolution.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 6
        os.write(0x86);
        codeLength += 1;
        return codeLength;
      }

      if (servicePreempt != null) {
        codeLength += servicePreempt.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 5
        os.write(0x85);
        codeLength += 1;
        return codeLength;
      }

      if (service != null) {
        codeLength += service.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 4
        os.write(0x84);
        codeLength += 1;
        return codeLength;
      }

      if (resource != null) {
        codeLength += resource.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 3
        os.write(0x83);
        codeLength += 1;
        return codeLength;
      }

      if (definition != null) {
        codeLength += definition.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 2
        os.write(0x82);
        codeLength += 1;
        return codeLength;
      }

      if (applicationReference != null) {
        codeLength += applicationReference.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 1
        os.write(0x81);
        codeLength += 1;
        return codeLength;
      }

      if (vmdState != null) {
        codeLength += vmdState.encode(os, false);
        // write tag: CONTEXT_CLASS, PRIMITIVE, 0
        os.write(0x80);
        codeLength += 1;
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

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 0)) {
        vmdState = new BerInteger();
        codeLength += vmdState.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 1)) {
        applicationReference = new BerInteger();
        codeLength += applicationReference.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 2)) {
        definition = new BerInteger();
        codeLength += definition.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 3)) {
        resource = new BerInteger();
        codeLength += resource.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 4)) {
        service = new BerInteger();
        codeLength += service.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 5)) {
        servicePreempt = new BerInteger();
        codeLength += servicePreempt.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 6)) {
        timeResolution = new BerInteger();
        codeLength += timeResolution.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 7)) {
        access = new BerInteger();
        codeLength += access.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 8)) {
        initiate = new BerInteger();
        codeLength += initiate.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 9)) {
        conclude = new BerInteger();
        codeLength += conclude.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 10)) {
        cancel = new BerInteger();
        codeLength += cancel.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 11)) {
        file = new BerInteger();
        codeLength += file.decode(is, false);
        return codeLength;
      }

      if (berTag.equals(BerTag.CONTEXT_CLASS, BerTag.PRIMITIVE, 12)) {
        others = new BerInteger();
        codeLength += others.decode(is, false);
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

      if (vmdState != null) {
        sb.append("vmdState: ").append(vmdState);
        return;
      }

      if (applicationReference != null) {
        sb.append("applicationReference: ").append(applicationReference);
        return;
      }

      if (definition != null) {
        sb.append("definition: ").append(definition);
        return;
      }

      if (resource != null) {
        sb.append("resource: ").append(resource);
        return;
      }

      if (service != null) {
        sb.append("service: ").append(service);
        return;
      }

      if (servicePreempt != null) {
        sb.append("servicePreempt: ").append(servicePreempt);
        return;
      }

      if (timeResolution != null) {
        sb.append("timeResolution: ").append(timeResolution);
        return;
      }

      if (access != null) {
        sb.append("access: ").append(access);
        return;
      }

      if (initiate != null) {
        sb.append("initiate: ").append(initiate);
        return;
      }

      if (conclude != null) {
        sb.append("conclude: ").append(conclude);
        return;
      }

      if (cancel != null) {
        sb.append("cancel: ").append(cancel);
        return;
      }

      if (file != null) {
        sb.append("file: ").append(file);
        return;
      }

      if (others != null) {
        sb.append("others: ").append(others);
        return;
      }

      sb.append("<none>");
    }
  }
}

/** This class file was automatically generated by jASN1 v1.9.0 (http://www.openmuc.org) */
package org.openmuc.josistack.internal.acse.asn1;

import java.math.BigInteger;
import org.openmuc.jasn1.ber.types.BerInteger;

public class ReleaseResponseReason extends BerInteger {

  private static final long serialVersionUID = 1L;

  public ReleaseResponseReason() {}

  public ReleaseResponseReason(byte[] code) {
    super(code);
  }

  public ReleaseResponseReason(BigInteger value) {
    super(value);
  }

  public ReleaseResponseReason(long value) {
    super(value);
  }
}

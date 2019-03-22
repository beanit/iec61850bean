package org.openmuc.openiec61850.internal.cli;

public final class FatalActionException extends Exception {

  private static final long serialVersionUID = -8134353678567694515L;

  public FatalActionException() {
    super();
  }

  public FatalActionException(String s) {
    super(s);
  }

  public FatalActionException(Throwable cause) {
    super(cause);
  }

  public FatalActionException(String s, Throwable cause) {
    super(s, cause);
  }
}

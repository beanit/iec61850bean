package org.openmuc.openiec61850.internal.cli;

public final class ActionException extends Exception {

    private static final long serialVersionUID = 4806947065917148946L;

    public ActionException() {
        super();
    }

    public ActionException(String s) {
        super(s);
    }

    public ActionException(Throwable cause) {
        super(cause);
    }

    public ActionException(String s, Throwable cause) {
        super(s, cause);
    }

}

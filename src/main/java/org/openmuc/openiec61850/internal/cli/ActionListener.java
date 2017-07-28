package org.openmuc.openiec61850.internal.cli;

public interface ActionListener {

    public void actionCalled(String actionKey) throws ActionException;

    public void quit();

}

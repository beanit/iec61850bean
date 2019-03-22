package com.beanit.openiec61850.internal.cli;

public interface ActionListener {

  public void actionCalled(String actionKey) throws ActionException, FatalActionException;

  public void quit();
}

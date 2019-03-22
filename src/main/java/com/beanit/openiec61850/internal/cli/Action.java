package com.beanit.openiec61850.internal.cli;

public class Action {

  private final String key;
  private final String description;

  public Action(String key, String description) {
    this.key = key;
    this.description = description;
  }

  public String getKey() {
    return key;
  }

  public String getDescription() {
    return description;
  }
}

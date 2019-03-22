package com.beanit.openiec61850.internal.cli;

public class FlagCliParameter extends CliParameter {

  FlagCliParameter(CliParameterBuilder builder) {
    super(builder);
  }

  @Override
  int appendSynopsis(StringBuilder sb) {
    int length = 0;
    if (optional) {
      sb.append("[");
      length++;
    }
    sb.append(name);
    length += name.length();
    if (optional) {
      sb.append("]");
      length++;
    }
    return length;
  }

  @Override
  void appendDescription(StringBuilder sb) {
    sb.append("\t").append(name).append("\n\t    ").append(description);
  }

  @Override
  int parse(String[] args, int i) throws CliParseException {
    selected = true;
    return 1;
  }
}

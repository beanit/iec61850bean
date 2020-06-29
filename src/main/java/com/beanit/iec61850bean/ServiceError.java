/*
 * Copyright 2011 The IEC61850bean Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.beanit.iec61850bean;

public final class ServiceError extends Exception {

  public static final int NO_ERROR = 0;
  public static final int INSTANCE_NOT_AVAILABLE = 1;
  public static final int INSTANCE_IN_USE = 2;
  public static final int ACCESS_VIOLATION = 3;
  public static final int ACCESS_NOT_ALLOWED_IN_CURRENT_STATE = 4;
  public static final int PARAMETER_VALUE_INAPPROPRIATE = 5;
  public static final int PARAMETER_VALUE_INCONSISTENT = 6;
  public static final int CLASS_NOT_SUPPORTED = 7;
  public static final int INSTANCE_LOCKED_BY_OTHER_CLIENT = 8;
  public static final int CONTROL_MUST_BE_SELECTED = 9;
  public static final int TYPE_CONFLICT = 10;
  public static final int FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT = 11;
  public static final int FAILED_DUE_TO_SERVER_CONSTRAINT = 12;
  public static final int APPLICATION_UNREACHABLE = 13;
  public static final int CONNECTION_LOST = 14;
  public static final int MEMORY_UNAVAILABLE = 15;
  public static final int PROCESSOR_RESOURCE_UNAVAILABLE = 16;
  public static final int FILE_NONE_EXISTENT = 17;
  public static final int FATAL = 20;
  // added to handle data access errors mentioned in iec61850-8-1
  // public static final int DATA_ACCESS_ERROR = 21;
  // added to report timeouts
  public static final int TIMEOUT = 22;
  public static final int UNKNOWN = 23;

  private static final long serialVersionUID = 4290107163231828564L;
  private final int errorCode;

  public ServiceError(int errorCode) {
    this(errorCode, "", null);
  }

  public ServiceError(int errorCode, String s) {
    this(errorCode, s, null);
  }

  public ServiceError(int errorCode, Throwable cause) {
    this(errorCode, "", cause);
  }

  public ServiceError(int errorCode, String s, Throwable cause) {
    super(
        "Service error: "
            + getErrorName(errorCode)
            + "("
            + errorCode
            + ")"
            + (s.isEmpty() ? "" : (" " + s)),
        cause);
    this.errorCode = errorCode;
  }

  private static String getErrorName(int code) {
    switch (code) {
      case NO_ERROR:
        return "NO_ERROR";
      case INSTANCE_NOT_AVAILABLE:
        return "INSTANCE_NOT_AVAILABLE";
      case INSTANCE_IN_USE:
        return "INSTANCE_IN_USE";
      case ACCESS_VIOLATION:
        return "ACCESS_VIOLATION";
      case ACCESS_NOT_ALLOWED_IN_CURRENT_STATE:
        return "ACCESS_NOT_ALLOWED_IN_CURRENT_STATE";
      case PARAMETER_VALUE_INAPPROPRIATE:
        return "PARAMETER_VALUE_INAPPROPRIATE";
      case PARAMETER_VALUE_INCONSISTENT:
        return "PARAMETER_VALUE_INCONSISTENT";
      case CLASS_NOT_SUPPORTED:
        return "CLASS_NOT_SUPPORTED";
      case INSTANCE_LOCKED_BY_OTHER_CLIENT:
        return "INSTANCE_LOCKED_BY_OTHER_CLIENT";
      case CONTROL_MUST_BE_SELECTED:
        return "CONTROL_MUST_BE_SELECTED";
      case TYPE_CONFLICT:
        return "TYPE_CONFLICT";
      case FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT:
        return "FAILED_DUE_TO_COMMUNICATIONS_CONSTRAINT";
      case FAILED_DUE_TO_SERVER_CONSTRAINT:
        return "FAILED_DUE_TO_SERVER_CONSTRAINT";
      case APPLICATION_UNREACHABLE:
        return "APPLICATION_UNREACHABLE";
      case CONNECTION_LOST:
        return "CONNECTION_LOST";
      case MEMORY_UNAVAILABLE:
        return "MEMORY_UNAVAILABLE";
      case PROCESSOR_RESOURCE_UNAVAILABLE:
        return "PROCESSOR_RESOURCE_UNAVAILABLE";
      case FILE_NONE_EXISTENT:
        return "FILE_NONE_EXISTENT";
      case FATAL:
        return "FATAL";
      case TIMEOUT:
        return "TIMEOUT_ERROR";
      case UNKNOWN:
        return "UNKNOWN";
      default:
        return "Unknown ServiceError code";
    }
  }

  public int getErrorCode() {
    return errorCode;
  }
}

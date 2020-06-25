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

import java.util.Calendar;

/** Contains file information received by the GetFileDirectory service */
public class FileInformation {

  private final String filename;

  private final long fileSize;

  private final Calendar lastModified;

  public FileInformation(String filename, long fileSize, Calendar lastModified) {
    super();
    this.filename = filename;
    this.fileSize = fileSize;
    this.lastModified = lastModified;
  }

  public String getFilename() {
    return filename;
  }

  public long getFileSize() {
    return fileSize;
  }

  /**
   * Get the time stamp of last modification. As it is an optional attribute the return value can be
   * null
   *
   * @return the time stamp of last modification, or null if the time stamp is not present
   */
  public Calendar getLastModified() {
    return lastModified;
  }
}

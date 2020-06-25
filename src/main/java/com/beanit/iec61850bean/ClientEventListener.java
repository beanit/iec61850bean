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

import java.io.IOException;

/**
 * The listener interface for receiving incoming reports and association closed events. A listener
 * is registered through the {@link ClientSap#associate(java.net.InetAddress, int, String,
 * ClientEventListener) associate} method.
 *
 * @author Stefan Feuerhahn
 */
public interface ClientEventListener {

  /**
   * Invoked when a new report arrives. Note that the implementation of this method needs to be
   * thread safe as it can be called in parallel if a new report arrives while an old one is still
   * being processed.
   *
   * @param report the report that arrived.
   */
  void newReport(Report report);

  /**
   * Invoked when an IOException occurred for the association. An IOException implies that the
   * ClientAssociation that feeds this listener was automatically closed and can no longer be used
   * to receive reports.
   *
   * @param e the exception that occured.
   */
  void associationClosed(IOException e);
}

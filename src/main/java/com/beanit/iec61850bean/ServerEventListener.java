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

import java.util.List;

public interface ServerEventListener {

  /**
   * The write callback function is called if one of more basic data attributes are written using
   * either the setDataValue, setDataSetValues or control services. If the complete write process
   * was successful write returns either an empty list or null. If an error occurs writing one or
   * more attributes then a list shall be returned that is of equal size as the list of basic data
   * attributes. The returned list's element shall be null if writing the corresponding BDA was
   * successful and a service error otherwise.
   *
   * @param bdas the list of basic data attributes that are to be set.
   * @return a list of service errors indicating errors writing the corresponding basic data
   *     attributes.
   */
  List<ServiceError> write(List<BasicDataAttribute> bdas);

  void serverStoppedListening(ServerSap serverSAP);
}

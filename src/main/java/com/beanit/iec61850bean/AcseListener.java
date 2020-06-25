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

import com.beanit.josistack.AcseAssociation;
import com.beanit.josistack.AcseAssociationListener;
import java.io.IOException;
import java.nio.ByteBuffer;

final class AcseListener implements AcseAssociationListener {

  ServerSap serverSap;

  AcseListener(ServerSap serverSap) {
    this.serverSap = serverSap;
  }

  @Override
  public void connectionIndication(AcseAssociation acseAssociation, ByteBuffer psdu) {
    serverSap.connectionIndication(acseAssociation, psdu);
  }

  @Override
  public void serverStoppedListeningIndication(IOException e) {
    serverSap.serverStoppedListeningIndication(e);
  }
}

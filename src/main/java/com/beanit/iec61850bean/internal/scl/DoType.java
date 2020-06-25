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
package com.beanit.iec61850bean.internal.scl;

import com.beanit.iec61850bean.SclParseException;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public final class DoType extends AbstractType {

  // attributes not needed: cdc, iedType

  public List<Da> das = new ArrayList<>();
  public List<Sdo> sdos = new ArrayList<>();

  public DoType(Node xmlNode) throws SclParseException {

    super(xmlNode);

    if (xmlNode.getAttributes().getNamedItem("cdc") == null) {
      throw new SclParseException("Required attribute \"cdc\" not found in DOType!");
    }

    NodeList elements = xmlNode.getChildNodes();

    for (int i = 0; i < elements.getLength(); i++) {
      Node node = elements.item(i);
      if (node.getNodeName().equals("SDO")) {
        sdos.add(new Sdo(node));
      }
      if (node.getNodeName().equals("DA")) {
        das.add(new Da(node));
      }
    }
  }
}

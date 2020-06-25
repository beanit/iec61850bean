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

public final class LnType extends AbstractType {

  // attributes not needed: lnClass, iedType

  public List<Do> dos = new ArrayList<>();

  public LnType(Node xmlNode) throws SclParseException {

    super(xmlNode);

    if (xmlNode.getAttributes().getNamedItem("lnClass") == null) {
      throw new SclParseException("Required attribute \"lnClass\" not found in LNType!");
    }

    NodeList elements = xmlNode.getChildNodes();

    for (int i = 0; i < elements.getLength(); i++) {
      Node node = elements.item(i);
      if (node.getNodeName().equals("DO")) {
        dos.add(new Do(node));
      }
    }
  }
}

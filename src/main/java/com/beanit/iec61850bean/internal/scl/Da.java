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

import com.beanit.iec61850bean.Fc;
import com.beanit.iec61850bean.SclParseException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public final class Da extends AbstractDataAttribute {

  private Fc fc = null;
  private boolean dchg = false;
  private boolean qchg = false;
  private boolean dupd = false;

  public Da(Node xmlNode) throws SclParseException {

    super(xmlNode);

    NamedNodeMap attributes = xmlNode.getAttributes();

    for (int i = 0; i < attributes.getLength(); i++) {
      Node node = attributes.item(i);
      String nodeName = node.getNodeName();

      if (nodeName.equals("fc")) {
        fc = Fc.fromString(node.getNodeValue());
        if (fc == null) {
          throw new SclParseException("Invalid Functional Constraint");
        }
      } else if (nodeName.equals("dchg")) {
        dchg = "true".equals(node.getNodeValue());
      } else if (nodeName.equals("qchg")) {
        qchg = "true".equals(node.getNodeValue());
      } else if (nodeName.equals("dupd")) {
        dupd = "true".equals(node.getNodeValue());
      }
    }

    if (fc == null) {
      throw new SclParseException("Required attribute \"fc\" not found!");
    }
  }

  public Fc getFc() {
    return fc;
  }

  public boolean isDchg() {
    return dchg;
  }

  public boolean isQchg() {
    return qchg;
  }

  public boolean isDupd() {
    return dupd;
  }
}

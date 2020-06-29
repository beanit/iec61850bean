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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class AbstractDataAttribute extends AbstractElement {

  // attributes not needed: valKind

  public String value = null;
  private String sAddr = null; /* optional - short address */
  private String bType = null; /* mandatory - basic type */
  private String type = null; /* conditional - if bType = "Enum" or "Struct" */
  private int count = 0; /* optional - number of array elements */

  AbstractDataAttribute(Node xmlNode) throws SclParseException {
    super(xmlNode);

    NamedNodeMap attributes = xmlNode.getAttributes();

    for (int i = 0; i < attributes.getLength(); i++) {
      Node node = attributes.item(i);
      String nodeName = node.getNodeName();

      if (nodeName.equals("type")) {
        type = node.getNodeValue();
      } else if (nodeName.equals("sAddr")) {
        sAddr = node.getNodeValue();
      } else if (nodeName.equals("bType")) {
        bType = node.getNodeValue();
      } else if (nodeName.equals("count")) {
        count = Integer.parseInt(node.getNodeValue());
      }
    }

    if (bType == null) {
      throw new SclParseException("Required attribute \"bType\" not found!");
    }

    NodeList elements = xmlNode.getChildNodes();
    for (int i = 0; i < elements.getLength(); i++) {
      Node node = elements.item(i);
      if (node.getNodeName().equals("Val")) {
        value = node.getTextContent();
      }
    }
  }

  public String getsAddr() {
    return sAddr;
  }

  public String getbType() {
    return bType;
  }

  public String getType() {
    return type;
  }

  public int getCount() {
    return count;
  }
}

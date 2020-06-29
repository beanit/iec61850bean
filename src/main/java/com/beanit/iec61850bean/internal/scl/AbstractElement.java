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

public abstract class AbstractElement {

  private String name = null;
  private String desc = null;

  AbstractElement(String name, String desc) {
    this.name = name;
    this.desc = desc;
  }

  AbstractElement(Node xmlNode) throws SclParseException {
    NamedNodeMap attributes = xmlNode.getAttributes();

    Node node = attributes.getNamedItem("name");
    if (node == null) {
      throw new SclParseException("Required attribute \"name\" not found!");
    }
    name = node.getNodeValue();

    node = attributes.getNamedItem("desc");
    if (node != null) {
      desc = node.getNodeValue();
    }
  }

  public String getName() {
    return name;
  }

  public String getDesc() {
    return desc;
  }
}

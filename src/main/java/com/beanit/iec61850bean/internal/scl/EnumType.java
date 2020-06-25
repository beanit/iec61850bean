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

public final class EnumType extends AbstractType {

  public List<EnumVal> values;
  public int max = 0;
  public int min = 0;

  public EnumType(Node xmlNode) throws SclParseException {
    super(xmlNode);

    NodeList elements = xmlNode.getChildNodes();

    values = new ArrayList<>();

    for (int i = 0; i < elements.getLength(); i++) {
      Node node = elements.item(i);

      if (node.getNodeName().equals("EnumVal")) {
        EnumVal val = new EnumVal(node);
        if (val.getOrd() < min) {
          min = val.getOrd();
        } else if (val.getOrd() > max) {
          max = val.getOrd();
        }
        values.add(val);
      }
    }
  }

  public List<EnumVal> getValues() {
    return values;
  }
}

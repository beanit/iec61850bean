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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** ObjectReference syntax: LDName/LNName.DOName[.Name[. ...]] */
public final class ObjectReference implements Iterable<String> {

  private final String objectReference;
  private List<String> nodeNames = null;

  // if the ObjectReference contains an array index this variable will save
  // its position in the nodeNames List
  private int arrayIndexPosition = -1;

  public ObjectReference(String objectReference) {
    if (objectReference == null || objectReference.isEmpty()) {
      throw new IllegalArgumentException();
    }
    this.objectReference = objectReference;
  }

  /**
   * Returns name part of the reference.
   *
   * @return the name
   */
  public String getName() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return nodeNames.get(nodeNames.size() - 1);
  }

  public String getLdName() {
    if (nodeNames == null) {
      parseForNameList();
    }

    return nodeNames.get(0);
  }

  @Override
  public String toString() {
    return objectReference;
  }

  public boolean isLogicalDeviceRef() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return (nodeNames.size() == 1);
  }

  public boolean isLogicalNodeRef() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return (nodeNames.size() == 2);
  }

  public boolean isDataRef() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return (nodeNames.size() > 2);
  }

  int getArrayIndexPosition() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return arrayIndexPosition;
  }

  @Override
  public Iterator<String> iterator() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return nodeNames.iterator();
  }

  public String get(int i) {
    if (nodeNames == null) {
      parseForNameList();
    }
    return nodeNames.get(i);
  }

  public int size() {
    if (nodeNames == null) {
      parseForNameList();
    }
    return nodeNames.size();
  }

  private void parseForNameList() {

    nodeNames = new ArrayList<>();

    int lastDelim = -1;
    int nextDelim = objectReference.indexOf('/');
    if (nextDelim == -1) {
      nodeNames.add(objectReference.substring(lastDelim + 1));
      return;
    }

    nodeNames.add(objectReference.substring(lastDelim + 1, nextDelim));

    int dotIndex = -1;
    int openingbracketIndex = -1;
    int closingbracketIndex = -1;
    while (true) {
      lastDelim = nextDelim;
      if (dotIndex == -1) {
        dotIndex = objectReference.indexOf('.', lastDelim + 1);
        if (dotIndex == -1) {
          dotIndex = objectReference.length();
        }
      }
      if (openingbracketIndex == -1) {
        openingbracketIndex = objectReference.indexOf('(', lastDelim + 1);
        if (openingbracketIndex == -1) {
          openingbracketIndex = objectReference.length();
        }
      }
      if (closingbracketIndex == -1) {
        closingbracketIndex = objectReference.indexOf(')', lastDelim + 1);
        if (closingbracketIndex == -1) {
          closingbracketIndex = objectReference.length();
        }
      }

      if (dotIndex == openingbracketIndex && dotIndex == closingbracketIndex) {
        nodeNames.add(objectReference.substring(lastDelim + 1));
        return;
      }

      if (dotIndex < openingbracketIndex && dotIndex < closingbracketIndex) {
        nextDelim = dotIndex;
        dotIndex = -1;
      } else if (openingbracketIndex < dotIndex && openingbracketIndex < closingbracketIndex) {
        nextDelim = openingbracketIndex;
        openingbracketIndex = -1;
        arrayIndexPosition = nodeNames.size() + 1;
      } else if (closingbracketIndex < dotIndex && closingbracketIndex < openingbracketIndex) {
        if (closingbracketIndex == (objectReference.length() - 1)) {
          nodeNames.add(objectReference.substring(lastDelim + 1, closingbracketIndex));
          return;
        }
        nextDelim = closingbracketIndex + 1;
        closingbracketIndex = -1;
        dotIndex = -1;
        nodeNames.add(objectReference.substring(lastDelim + 1, nextDelim - 1));
        continue;
      }
      nodeNames.add(objectReference.substring(lastDelim + 1, nextDelim));
    }
  }
}

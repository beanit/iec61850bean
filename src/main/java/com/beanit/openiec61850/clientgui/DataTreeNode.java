/*
 * Copyright 2011 The OpenIEC61850 Authors
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
package com.beanit.openiec61850.clientgui;

import com.beanit.openiec61850.ClientAssociation;
import com.beanit.openiec61850.ServiceError;
import java.io.IOException;
import javax.swing.tree.TreeNode;

public interface DataTreeNode {

  BasicDataBind<?> getData();

  void reset(ClientAssociation association) throws ServiceError, IOException;

  void writeValues(ClientAssociation association) throws ServiceError, IOException;

  int getChildCount();

  TreeNode getChildAt(int index);

  boolean writable();

  boolean readable();
}

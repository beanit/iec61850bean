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
package com.beanit.iec61850bean.clientgui;

import com.beanit.iec61850bean.ClientAssociation;
import com.beanit.iec61850bean.ServiceError;
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

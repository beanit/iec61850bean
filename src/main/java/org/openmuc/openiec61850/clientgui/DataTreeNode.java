/*
 * Copyright 2011-17 Fraunhofer ISE, energy & meteo Systems GmbH and other contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.openmuc.openiec61850.clientgui;

import java.io.IOException;

import javax.swing.tree.TreeNode;

import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ServiceError;

public interface DataTreeNode {

    public abstract BasicDataBind<?> getData();

    public abstract void reset(ClientAssociation association) throws ServiceError, IOException;

    public abstract void writeValues(ClientAssociation association) throws ServiceError, IOException;

    public abstract int getChildCount();

    public abstract TreeNode getChildAt(int index);

    public abstract boolean writable();

    public abstract boolean readable();
}

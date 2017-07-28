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

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.openmuc.openiec61850.BasicDataAttribute;
import org.openmuc.openiec61850.BdaType;

public abstract class BasicDataBind<E extends BasicDataAttribute> {
    protected final E data;

    private JComponent valueField;

    public BasicDataBind(E data, BdaType type) {
        if (data.getBasicType() != type) {
            throw new IllegalArgumentException(data.getName() + " is no " + type);
        }
        this.data = data;
    }

    public JLabel getNameLabel() {
        return new JLabel(data.getName());
    }

    public JComponent getValueField() {
        if (valueField == null) {
            valueField = init();
        }

        return valueField;
    }

    public void reset() {
        if (valueField == null) {
            valueField = init();
        }

        resetImpl();
    }

    public void write() {
        if (valueField == null) {
            valueField = init();
        }

        writeImpl();
    }

    protected abstract JComponent init();

    protected abstract void resetImpl();

    protected abstract void writeImpl();
}

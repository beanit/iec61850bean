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
package com.beanit.iec61850bean.clientgui.databind;

import com.beanit.iec61850bean.BasicDataAttribute;
import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.clientgui.BasicDataBind;
import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

public abstract class TextFieldDataBind<E extends BasicDataAttribute> extends BasicDataBind<E> {

  private final DocumentFilter filter;
  protected JTextField inputField;

  TextFieldDataBind(E data, BdaType type, AbstractFilter filter) {
    super(data, type);
    this.filter = filter;
  }

  @Override
  protected JComponent init() {
    inputField = new JTextField();
    PlainDocument doc = (PlainDocument) inputField.getDocument();
    doc.setDocumentFilter(filter);
    resetImpl();
    return inputField;
  }

  protected abstract static class AbstractFilter extends DocumentFilter {
    @Override
    public final void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
        throws BadLocationException {

      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.insert(offset, string);

      if (test(sb.toString())) {
        super.insertString(fb, offset, string, attr);
      }
    }

    @Override
    public final void replace(
        FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
        throws BadLocationException {

      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.replace(offset, offset + length, text);

      if (test(sb.toString())) {
        super.replace(fb, offset, length, text, attrs);
      }
    }

    @Override
    public final void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      Document doc = fb.getDocument();
      StringBuilder sb = new StringBuilder();
      sb.append(doc.getText(0, doc.getLength()));
      sb.delete(offset, offset + length);

      if (test(sb.toString())) {
        super.remove(fb, offset, length);
      }
    }

    protected abstract boolean test(String text);
  }
}

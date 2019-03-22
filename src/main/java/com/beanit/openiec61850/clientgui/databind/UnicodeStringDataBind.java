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
package com.beanit.openiec61850.clientgui.databind;

import com.beanit.openiec61850.BdaType;
import com.beanit.openiec61850.BdaUnicodeString;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

public class UnicodeStringDataBind extends TextFieldDataBind<BdaUnicodeString> {

  private static final Charset UTF8 = Charset.forName("UTF-8");

  public UnicodeStringDataBind(BdaUnicodeString data) {
    super(data, BdaType.UNICODE_STRING, new Utf8Filter(data.getMaxLength()));
  }

  @Override
  protected void resetImpl() {
    inputField.setText(new String(data.getValue(), UTF8));
  }

  @Override
  protected void writeImpl() {
    data.setValue(UTF8.encode(inputField.getText()).array());
  }

  private static class Utf8Filter extends AbstractFilter {
    private final CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
    private final int maxBytes;

    public Utf8Filter(int maxBytes) {
      this.maxBytes = maxBytes;
    }

    @Override
    protected boolean test(String text) {
      try {
        byte[] codedString = encoder.encode(CharBuffer.wrap(text)).array();
        return codedString.length <= maxBytes;
      } catch (CharacterCodingException e) {
        return false;
      }
    }
  }
}

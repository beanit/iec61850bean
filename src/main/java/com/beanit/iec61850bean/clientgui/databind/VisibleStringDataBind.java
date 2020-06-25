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

import com.beanit.iec61850bean.BdaType;
import com.beanit.iec61850bean.BdaVisibleString;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

public class VisibleStringDataBind extends TextFieldDataBind<BdaVisibleString> {

  private static final Charset ASCII = StandardCharsets.US_ASCII;

  public VisibleStringDataBind(BdaVisibleString data) {
    super(data, BdaType.VISIBLE_STRING, new AsciiFilter(data.getMaxLength()));
  }

  @Override
  protected void resetImpl() {
    inputField.setText(new String(data.getValue(), ASCII));
  }

  @Override
  protected void writeImpl() {
    data.setValue(ASCII.encode(inputField.getText()).array());
  }

  private static class AsciiFilter extends TextFieldDataBind.AbstractFilter {
    private final CharsetEncoder encoder = StandardCharsets.US_ASCII.newEncoder();
    private final int maxBytes;

    public AsciiFilter(int maxBytes) {
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

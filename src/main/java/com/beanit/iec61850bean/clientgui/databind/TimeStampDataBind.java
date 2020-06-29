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

import com.beanit.iec61850bean.BdaTimestamp;
import com.beanit.iec61850bean.BdaType;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class TimeStampDataBind extends TextFieldDataBind<BdaTimestamp> {

  private static final TimestampFilter FILTER = new TimestampFilter();

  public TimeStampDataBind(BdaTimestamp data) {
    super(data, BdaType.TIMESTAMP, FILTER);
  }

  @Override
  protected void resetImpl() {
    inputField.setText(data.getInstant().toString());
  }

  @Override
  protected void writeImpl() {
    data.setInstant(Instant.parse(inputField.getText()));
  }

  private static class TimestampFilter extends AbstractFilter {
    @Override
    protected boolean test(String text) {
      try {
        Instant.parse(text);
        return true;
      } catch (DateTimeParseException e) {
        return false;
      }
    }
  }
}

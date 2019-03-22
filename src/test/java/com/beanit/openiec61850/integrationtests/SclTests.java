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
package com.beanit.openiec61850.integrationtests;

import com.beanit.openiec61850.SclParseException;
import com.beanit.openiec61850.SclParser;
import org.junit.Test;

public class SclTests {

  private static final String SCL_FILE_PATH_1 = "src/test/resources/testModel.icd";
  private static final String SCL_FILE_PATH_2 = "src/test/resources/testModel2.icd";

  @Test
  public void testClientServerCom() throws SclParseException {

    SclParser.parse(SCL_FILE_PATH_1);
    SclParser.parse(SCL_FILE_PATH_2);
  }
}

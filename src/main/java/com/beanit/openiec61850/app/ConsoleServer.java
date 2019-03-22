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
package com.beanit.openiec61850.app;

import com.beanit.openiec61850.BasicDataAttribute;
import com.beanit.openiec61850.BdaBoolean;
import com.beanit.openiec61850.BdaFloat32;
import com.beanit.openiec61850.BdaFloat64;
import com.beanit.openiec61850.BdaInt16;
import com.beanit.openiec61850.BdaInt16U;
import com.beanit.openiec61850.BdaInt32;
import com.beanit.openiec61850.BdaInt32U;
import com.beanit.openiec61850.BdaInt64;
import com.beanit.openiec61850.BdaInt8;
import com.beanit.openiec61850.BdaInt8U;
import com.beanit.openiec61850.Fc;
import com.beanit.openiec61850.ModelNode;
import com.beanit.openiec61850.SclParseException;
import com.beanit.openiec61850.SclParser;
import com.beanit.openiec61850.ServerEventListener;
import com.beanit.openiec61850.ServerModel;
import com.beanit.openiec61850.ServerSap;
import com.beanit.openiec61850.ServiceError;
import com.beanit.openiec61850.internal.cli.Action;
import com.beanit.openiec61850.internal.cli.ActionException;
import com.beanit.openiec61850.internal.cli.ActionListener;
import com.beanit.openiec61850.internal.cli.ActionProcessor;
import com.beanit.openiec61850.internal.cli.CliParameter;
import com.beanit.openiec61850.internal.cli.CliParameterBuilder;
import com.beanit.openiec61850.internal.cli.CliParseException;
import com.beanit.openiec61850.internal.cli.CliParser;
import com.beanit.openiec61850.internal.cli.IntCliParameter;
import com.beanit.openiec61850.internal.cli.StringCliParameter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ConsoleServer {

  private static final String WRITE_VALUE_KEY = "w";
  private static final String WRITE_VALUE_KEY_DESCRIPTION = "write value to model node";

  private static final String PRINT_SERVER_MODEL_KEY = "p";
  private static final String PRINT_SERVER_MODEL_KEY_DESCRIPTION = "print server's model";

  private static final IntCliParameter portParam =
      new CliParameterBuilder("-p")
          .setDescription(
              "The port to listen on. On unix based systems you need root privilages for ports < 1000.")
          .buildIntParameter("port", 102);

  private static final StringCliParameter modelFileParam =
      new CliParameterBuilder("-m")
          .setDescription("The SCL file that contains the server's information model.")
          .setMandatory()
          .buildStringParameter("model-file");
  private static final ActionProcessor actionProcessor = new ActionProcessor(new ActionExecutor());
  private static ServerModel serverModel;
  private static ServerSap serverSap = null;

  public static void main(String[] args) throws IOException {

    List<CliParameter> cliParameters = new ArrayList<>();
    cliParameters.add(modelFileParam);
    cliParameters.add(portParam);

    CliParser cliParser =
        new CliParser("openiec61850-console-server", "An IEC 61850 MMS console server.");
    cliParser.addParameters(cliParameters);

    try {
      cliParser.parseArguments(args);
    } catch (CliParseException e1) {
      System.err.println("Error parsing command line parameters: " + e1.getMessage());
      System.out.println(cliParser.getUsageString());
      System.exit(1);
    }

    List<ServerModel> serverModels = null;
    try {
      serverModels = SclParser.parse(modelFileParam.getValue());
    } catch (SclParseException e) {
      System.out.println("Error parsing SCL/ICD file: " + e.getMessage());
      return;
    }

    serverSap = new ServerSap(102, 0, null, serverModels.get(0), null);
    serverSap.setPort(portParam.getValue());

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                if (serverSap != null) {
                  serverSap.stop();
                }
                System.out.println("Server was stopped.");
              }
            });

    serverModel = serverSap.getModelCopy();

    serverSap.startListening(new EventListener());

    actionProcessor.addAction(
        new Action(PRINT_SERVER_MODEL_KEY, PRINT_SERVER_MODEL_KEY_DESCRIPTION));
    actionProcessor.addAction(new Action(WRITE_VALUE_KEY, WRITE_VALUE_KEY_DESCRIPTION));

    actionProcessor.start();
  }

  private static class EventListener implements ServerEventListener {

    @Override
    public void serverStoppedListening(ServerSap serverSap) {
      System.out.println("The SAP stopped listening");
    }

    @Override
    public List<ServiceError> write(List<BasicDataAttribute> bdas) {
      for (BasicDataAttribute bda : bdas) {
        System.out.println("got a write request: " + bda);
      }
      return null;
    }
  }

  private static class ActionExecutor implements ActionListener {

    @Override
    public void actionCalled(String actionKey) throws ActionException {
      try {
        switch (actionKey) {
          case PRINT_SERVER_MODEL_KEY:
            System.out.println("** Printing model.");

            System.out.println(serverModel);

            break;
          case WRITE_VALUE_KEY:
            System.out.println("Enter reference to write (e.g. myld/MYLN0.do.da.bda): ");
            String reference = actionProcessor.getReader().readLine();
            System.out.println("Enter functional constraint of referenced node: ");
            String fcString = actionProcessor.getReader().readLine();

            Fc fc = Fc.fromString(fcString);
            if (fc == null) {
              System.out.println("Unknown functional constraint.");
              return;
            }

            ModelNode modelNode = serverModel.findModelNode(reference, Fc.fromString(fcString));
            if (modelNode == null) {
              System.out.println(
                  "A model node with the given reference and functional constraint could not be found.");
              return;
            }

            if (!(modelNode instanceof BasicDataAttribute)) {
              System.out.println("The given model node is not a basic data attribute.");
              return;
            }

            BasicDataAttribute bda =
                (BasicDataAttribute) serverModel.findModelNode(reference, Fc.fromString(fcString));

            System.out.println("Enter value to write: ");
            String valueString = actionProcessor.getReader().readLine();

            try {
              setBdaValue(bda, valueString);
            } catch (Exception e) {
              System.out.println(
                  "The console server does not support writing this type of basic data attribute.");
              return;
            }

            List<BasicDataAttribute> bdas = new ArrayList<>();
            bdas.add(bda);
            serverSap.setValues(bdas);

            System.out.println("Successfully wrote data.");
            System.out.println(bda);

            break;

          default:
            break;
        }
      } catch (Exception e) {
        throw new ActionException(e);
      }
    }

    private void setBdaValue(BasicDataAttribute bda, String valueString) {
      if (bda instanceof BdaFloat32) {
        float value = Float.parseFloat(valueString);
        ((BdaFloat32) bda).setFloat(value);
      } else if (bda instanceof BdaFloat64) {
        double value = Float.parseFloat(valueString);
        ((BdaFloat64) bda).setDouble(value);
      } else if (bda instanceof BdaInt8) {
        byte value = Byte.parseByte(valueString);
        ((BdaInt8) bda).setValue(value);
      } else if (bda instanceof BdaInt8U) {
        short value = Short.parseShort(valueString);
        ((BdaInt8U) bda).setValue(value);
      } else if (bda instanceof BdaInt16) {
        short value = Short.parseShort(valueString);
        ((BdaInt16) bda).setValue(value);
      } else if (bda instanceof BdaInt16U) {
        int value = Integer.parseInt(valueString);
        ((BdaInt16U) bda).setValue(value);
      } else if (bda instanceof BdaInt32) {
        int value = Integer.parseInt(valueString);
        ((BdaInt32) bda).setValue(value);
      } else if (bda instanceof BdaInt32U) {
        long value = Long.parseLong(valueString);
        ((BdaInt32U) bda).setValue(value);
      } else if (bda instanceof BdaInt64) {
        long value = Long.parseLong(valueString);
        ((BdaInt64) bda).setValue(value);
      } else if (bda instanceof BdaBoolean) {
        boolean value = Boolean.parseBoolean(valueString);
        ((BdaBoolean) bda).setValue(value);
      } else {
        throw new IllegalArgumentException();
      }
    }

    @Override
    public void quit() {
      System.out.println("** Stopping server.");
      serverSap.stop();
      return;
    }
  }
}

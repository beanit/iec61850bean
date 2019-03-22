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

import com.beanit.openiec61850.BdaTriggerConditions;
import com.beanit.openiec61850.Brcb;
import com.beanit.openiec61850.ClientAssociation;
import com.beanit.openiec61850.ClientEventListener;
import com.beanit.openiec61850.ClientSap;
import com.beanit.openiec61850.DataSet;
import com.beanit.openiec61850.Fc;
import com.beanit.openiec61850.FcModelNode;
import com.beanit.openiec61850.ModelNode;
import com.beanit.openiec61850.Report;
import com.beanit.openiec61850.SclParseException;
import com.beanit.openiec61850.SclParser;
import com.beanit.openiec61850.ServerModel;
import com.beanit.openiec61850.ServiceError;
import com.beanit.openiec61850.Urcb;
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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ConsoleClient {

  private static final String PRINT_MODEL_KEY = "m";
  private static final String PRINT_MODEL_KEY_DESCRIPTION = "print model";
  private static final String GET_DATA_VALUES_KEY = "g";
  private static final String GET_DATA_VALUES_KEY_DESCRIPTION = "send GetDataValues request";
  private static final String READ_ALL_DATA_KEY = "ga";
  private static final String READ_ALL_DATA_KEY_DESCRIPTION = "update all data in the model";
  private static final String CREATE_DATA_SET_KEY = "cds";
  private static final String CREATE_DATA_SET_KEY_DESCRIPTION = "create data set";
  private static final String DELETE_DATA_SET_KEY = "dds";
  private static final String DELETE_DATA_SET_KEY_DESCRIPTION = "delete data set";
  private static final String REPORTING_KEY = "r";
  private static final String REPORTING_KEY_DESCRIPTION = "configure reporting";

  private static final StringCliParameter hostParam =
      new CliParameterBuilder("-h")
          .setDescription("The IP/domain address of the server you want to access.")
          .setMandatory()
          .buildStringParameter("host");
  private static final IntCliParameter portParam =
      new CliParameterBuilder("-p")
          .setDescription("The port to connect to.")
          .buildIntParameter("port", 102);
  private static final StringCliParameter modelFileParam =
      new CliParameterBuilder("-m")
          .setDescription(
              "The file name of the SCL file to read the model from. If this parameter is omitted the model will be read from the server device after connection.")
          .buildStringParameter("model-file");
  private static final ActionProcessor actionProcessor = new ActionProcessor(new ActionExecutor());
  private static volatile ClientAssociation association;
  private static ServerModel serverModel;

  public static void main(String[] args) {

    List<CliParameter> cliParameters = new ArrayList<>();
    cliParameters.add(hostParam);
    cliParameters.add(portParam);
    cliParameters.add(modelFileParam);

    CliParser cliParser =
        new CliParser(
            "openiec61850-console-client", "A client application to access IEC 61850 MMS servers.");
    cliParser.addParameters(cliParameters);

    try {
      cliParser.parseArguments(args);
    } catch (CliParseException e1) {
      System.err.println("Error parsing command line parameters: " + e1.getMessage());
      System.out.println(cliParser.getUsageString());
      System.exit(1);
    }

    InetAddress address;
    try {
      address = InetAddress.getByName(hostParam.getValue());
    } catch (UnknownHostException e) {
      System.out.println("Unknown host: " + hostParam.getValue());
      return;
    }

    ClientSap clientSap = new ClientSap();

    try {
      association = clientSap.associate(address, portParam.getValue(), null, new EventListener());
    } catch (IOException e) {
      System.out.println("Unable to connect to remote host.");
      return;
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                association.close();
              }
            });

    System.out.println("successfully connected");

    if (modelFileParam.isSelected()) {
      System.out.println("reading model from file...");

      try {
        serverModel = SclParser.parse(modelFileParam.getValue()).get(0);
      } catch (SclParseException e1) {
        System.out.println("Error parsing SCL file: " + e1.getMessage());
        return;
      }

      association.setServerModel(serverModel);

      System.out.println("successfully read model");

    } else {
      System.out.println("retrieving model...");

      try {
        serverModel = association.retrieveModel();
      } catch (ServiceError e) {
        System.out.println("Service error: " + e.getMessage());
        return;
      } catch (IOException e) {
        System.out.println("Fatal error: " + e.getMessage());
        return;
      }

      System.out.println("successfully read model");
    }

    actionProcessor.addAction(new Action(PRINT_MODEL_KEY, PRINT_MODEL_KEY_DESCRIPTION));
    actionProcessor.addAction(new Action(GET_DATA_VALUES_KEY, GET_DATA_VALUES_KEY_DESCRIPTION));
    actionProcessor.addAction(new Action(READ_ALL_DATA_KEY, READ_ALL_DATA_KEY_DESCRIPTION));
    actionProcessor.addAction(new Action(CREATE_DATA_SET_KEY, CREATE_DATA_SET_KEY_DESCRIPTION));
    actionProcessor.addAction(new Action(DELETE_DATA_SET_KEY, DELETE_DATA_SET_KEY_DESCRIPTION));
    actionProcessor.addAction(new Action(REPORTING_KEY, REPORTING_KEY_DESCRIPTION));

    actionProcessor.start();
  }

  private static class EventListener implements ClientEventListener {

    @Override
    public void newReport(Report report) {
      System.out.println("\n----------------");
      System.out.println("Received report: ");
      System.err.println(report);
      System.out.println("------------------");
    }

    @Override
    public void associationClosed(IOException e) {
      System.out.print("Received connection closed signal. Reason: ");
      if (!e.getMessage().isEmpty()) {
        System.out.println(e.getMessage());
      } else {
        System.out.println("unknown");
      }
      actionProcessor.close();
    }
  }

  private static class ActionExecutor implements ActionListener {

    @Override
    public void actionCalled(String actionKey) throws ActionException {
      try {
        switch (actionKey) {
          case PRINT_MODEL_KEY:
            System.out.println(serverModel);
            break;
          case READ_ALL_DATA_KEY:
            System.out.print("Reading all data...");
            try {
              association.getAllDataValues();
            } catch (ServiceError e) {
              System.err.println("Service error: " + e.getMessage());
            }
            System.out.println("done");
            break;
          case GET_DATA_VALUES_KEY:
            {
              if (serverModel == null) {
                System.out.println("You have to retrieve the model before reading data.");
                return;
              }

              FcModelNode fcModelNode = askForFcModelNode();

              System.out.println("Sending GetDataValues request...");

              try {
                association.getDataValues(fcModelNode);
              } catch (ServiceError e) {
                System.out.println("Service error: " + e.getMessage());
                return;
              } catch (IOException e) {
                System.out.println("Fatal error: " + e.getMessage());
                return;
              }

              System.out.println("Successfully read data.");
              System.out.println(fcModelNode);

              break;
            }
          case CREATE_DATA_SET_KEY:
            {
              System.out.println(
                  "Enter the reference of the data set to create (e.g. myld/MYLN0.dataset1): ");
              String reference = actionProcessor.getReader().readLine();

              System.out.println("How many entries shall the data set have: ");
              String numberOfEntriesString = actionProcessor.getReader().readLine();
              int numDataSetEntries = Integer.parseInt(numberOfEntriesString);

              List<FcModelNode> dataSetMembers = new ArrayList<>();
              for (int i = 0; i < numDataSetEntries; i++) {
                dataSetMembers.add(askForFcModelNode());
              }

              DataSet dataSet = new DataSet(reference, dataSetMembers);
              System.out.print("Creating data set..");
              association.createDataSet(dataSet);
              System.out.println("done");

              break;
            }
          case DELETE_DATA_SET_KEY:
            {
              System.out.println(
                  "Enter the reference of the data set to delete (e.g. myld/MYLN0.dataset1): ");
              String reference = actionProcessor.getReader().readLine();

              DataSet dataSet = serverModel.getDataSet(reference);
              if (dataSet == null) {
                throw new ActionException("Unable to find data set with the given reference.");
              }
              System.out.print("Deleting data set..");
              association.deleteDataSet(dataSet);
              System.out.println("done");

              break;
            }
          case REPORTING_KEY:
            {
              System.out.println("Enter the URCB reference: ");
              String reference = actionProcessor.getReader().readLine();
              Urcb urcb = serverModel.getUrcb(reference);
              if (urcb == null) {
                Brcb brcb = serverModel.getBrcb(reference);
                if (brcb != null) {
                  throw new ActionException(
                      "Though buffered reporting is supported by the library it is not yet supported by the console application.");
                }
                throw new ActionException("Unable to find RCB with the given reference.");
              }

              while (true) {
                association.getRcbValues(urcb);
                System.out.println();
                System.out.println(urcb);
                System.out.println();
                System.out.println("What do you want to configure?");
                System.out.println("1 - reserve");
                System.out.println("2 - cancel reservation");
                System.out.println("3 - enable");
                System.out.println("4 - disable");
                System.out.println("5 - set data set");
                System.out.println("6 - set trigger options");
                System.out.println("7 - set integrity period");
                System.out.println("8 - send general interrogation");
                System.out.println("0 - quit");
                try {
                  int rcbAction = Integer.parseInt(actionProcessor.getReader().readLine());
                  switch (rcbAction) {
                    case 0:
                      return;
                    case 1:
                      System.out.print("Reserving RCB..");
                      association.reserveUrcb(urcb);
                      System.out.println("done");
                      break;
                    case 2:
                      System.out.print("Canceling RCB reservation..");
                      association.cancelUrcbReservation(urcb);
                      System.out.println("done");
                      break;
                    case 3:
                      System.out.print("Enabling reporting..");
                      association.enableReporting(urcb);
                      System.out.println("done");
                      break;
                    case 4:
                      System.out.print("Disabling reporting..");
                      association.disableReporting(urcb);
                      System.out.println("done");
                      break;
                    case 5:
                      {
                        System.out.print("Set data set reference:");
                        String dataSetReference = actionProcessor.getReader().readLine();
                        urcb.getDatSet().setValue(dataSetReference);
                        List<ServiceError> serviceErrors =
                            association.setRcbValues(
                                urcb, false, true, false, false, false, false, false, false);
                        if (serviceErrors.get(0) != null) {
                          throw serviceErrors.get(0);
                        }
                        System.out.println("done");
                        break;
                      }
                    case 6:
                      {
                        System.out.print(
                            "Set the trigger options (data change, data update, quality change, interity, GI):");
                        String triggerOptionsString = actionProcessor.getReader().readLine();
                        String[] triggerOptionsStrings = triggerOptionsString.split(",");
                        BdaTriggerConditions triggerOptions = urcb.getTrgOps();
                        triggerOptions.setDataChange(
                            Boolean.parseBoolean(triggerOptionsStrings[0]));
                        triggerOptions.setDataUpdate(
                            Boolean.parseBoolean(triggerOptionsStrings[1]));
                        triggerOptions.setQualityChange(
                            Boolean.parseBoolean(triggerOptionsStrings[2]));
                        triggerOptions.setIntegrity(Boolean.parseBoolean(triggerOptionsStrings[3]));
                        triggerOptions.setGeneralInterrogation(
                            Boolean.parseBoolean(triggerOptionsStrings[4]));
                        List<ServiceError> serviceErrors =
                            association.setRcbValues(
                                urcb, false, false, false, false, true, false, false, false);
                        if (serviceErrors.get(0) != null) {
                          throw serviceErrors.get(0);
                        }
                        System.out.println("done");
                        break;
                      }
                    case 7:
                      {
                        System.out.print("Specify integrity period in ms:");
                        String integrityPeriodString = actionProcessor.getReader().readLine();
                        urcb.getIntgPd().setValue(Long.parseLong(integrityPeriodString));
                        List<ServiceError> serviceErrors =
                            association.setRcbValues(
                                urcb, false, false, false, false, false, true, false, false);
                        if (serviceErrors.get(0) != null) {
                          throw serviceErrors.get(0);
                        }
                        System.out.println("done");
                        break;
                      }
                    case 8:
                      System.out.print("Sending GI..");
                      association.startGi(urcb);
                      System.out.println("done");
                      break;
                    default:
                      System.err.println("Unknown option.");
                      break;
                  }
                } catch (ServiceError e) {
                  System.err.println("Service error: " + e.getMessage());
                } catch (NumberFormatException e) {
                  System.err.println("Cannot parse number: " + e.getMessage());
                }
              }
            }
          default:
            break;
        }
      } catch (Exception e) {
        throw new ActionException(e);
      }
    }

    private FcModelNode askForFcModelNode() throws IOException, ActionException {
      System.out.println("Enter reference (e.g. myld/MYLN0.do.da.bda): ");
      String reference = actionProcessor.getReader().readLine();
      System.out.println("Enter functional constraint of referenced node: ");
      String fcString = actionProcessor.getReader().readLine();

      Fc fc = Fc.fromString(fcString);
      if (fc == null) {
        throw new ActionException("Unknown functional constraint.");
      }

      ModelNode modelNode = serverModel.findModelNode(reference, Fc.fromString(fcString));
      if (modelNode == null) {
        throw new ActionException(
            "A model node with the given reference and functional constraint could not be found.");
      }

      if (!(modelNode instanceof FcModelNode)) {
        throw new ActionException(
            "The given model node is not a functionally constraint model node.");
      }

      FcModelNode fcModelNode = (FcModelNode) modelNode;
      return fcModelNode;
    }

    @Override
    public void quit() {
      System.out.println("** Closing connection.");
      association.close();
      return;
    }
  }
}

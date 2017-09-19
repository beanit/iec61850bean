package org.openmuc.openiec61850.app;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ClientEventListener;
import org.openmuc.openiec61850.ClientSap;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.ModelNode;
import org.openmuc.openiec61850.Report;
import org.openmuc.openiec61850.SclParseException;
import org.openmuc.openiec61850.ServerModel;
import org.openmuc.openiec61850.ServiceError;
import org.openmuc.openiec61850.internal.cli.Action;
import org.openmuc.openiec61850.internal.cli.ActionException;
import org.openmuc.openiec61850.internal.cli.ActionListener;
import org.openmuc.openiec61850.internal.cli.ActionProcessor;
import org.openmuc.openiec61850.internal.cli.CliParameter;
import org.openmuc.openiec61850.internal.cli.CliParameterBuilder;
import org.openmuc.openiec61850.internal.cli.CliParseException;
import org.openmuc.openiec61850.internal.cli.CliParser;
import org.openmuc.openiec61850.internal.cli.IntCliParameter;
import org.openmuc.openiec61850.internal.cli.StringCliParameter;

/**
 *
 * @author Stefan Feuerhahn
 *
 */
public class ConsoleClient {

    private static final String PRINT_MODEL_KEY = "m";
    private static final String PRINT_MODEL_KEY_DESCRIPTION = "print model";
    private static final String GET_DATA_VALUES_KEY = "r";
    private static final String GET_DATA_VALUES_KEY_DESCRIPTION = "send GetDataValues request";
    private static final String READ_ALL_DATA_KEY = "ra";
    private static final String READ_ALL_DATA_KEY_DESCRIPTION = "update all data in the model";

    private static final StringCliParameter hostParam = new CliParameterBuilder("-h")
            .setDescription("The IP/domain address of the server you want to access.")
            .setMandatory()
            .buildStringParameter("host");
    private static final IntCliParameter portParam = new CliParameterBuilder("-p")
            .setDescription("The port to connect to.")
            .buildIntParameter("port", 102);
    private static final StringCliParameter modelFileParam = new CliParameterBuilder("-m").setDescription(
            "The file name of the SCL file to read the model from. If this parameter is omitted the model will be read from the server device after connection.")
            .buildStringParameter("model-file");

    private static volatile ClientAssociation association;
    private static ServerModel serverModel;
    private static final ActionProcessor actionProcessor = new ActionProcessor(new ActionExecutor());

    private static class EventListener implements ClientEventListener {

        @Override
        public void newReport(Report report) {
            System.out.println("Received report: " + report);
        }

        @Override
        public void associationClosed(IOException e) {
            System.out.print("Received connection closed signal. Reason: ");
            if (!e.getMessage().isEmpty()) {
                System.out.println(e.getMessage());
            }
            else {
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
                    association.getAllDataValues();
                    System.out.println("done");
                    break;
                case GET_DATA_VALUES_KEY:

                    System.out.println("** Sending GetDataValues request.");

                    if (serverModel == null) {
                        System.out.println("You have to retrieve the model before reading data.");
                        return;
                    }

                    System.out.println("Enter reference to read (e.g. myld/MYLN0.do.da.bda): ");
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

                    if (!(modelNode instanceof FcModelNode)) {
                        System.out.println("The given model node is not a functionally constraint model node.");
                        return;
                    }

                    FcModelNode fcModelNode = (FcModelNode) serverModel.findModelNode(reference,
                            Fc.fromString(fcString));

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
                default:
                    break;
                }
            } catch (Exception e) {
                throw new ActionException(e);
            }
        }

        @Override
        public void quit() {
            System.out.println("** Closing connection.");
            association.close();
            return;
        }
    }

    public static void main(String[] args) {

        List<CliParameter> cliParameters = new ArrayList<>();
        cliParameters.add(hostParam);
        cliParameters.add(portParam);
        cliParameters.add(modelFileParam);

        CliParser cliParser = new CliParser("openiec61850-console-client",
                "A client application to access IEC 61850 MMS servers.");
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
        // alternatively you could use ClientSap(SocketFactory factory) to e.g. connect using SSL

        // optionally you can set some association parameters (but usually the default should work):
        // clientSap.setTSelRemote(new byte[] { 0, 1 });
        // clientSap.setTSelLocal(new byte[] { 0, 0 });

        try {
            association = clientSap.associate(address, portParam.getValue(), null, new EventListener());
        } catch (IOException e) {
            System.out.println("Unable to connect to remote host.");
            return;
        }

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                association.close();
            }
        });

        System.out.println("successfully connected");

        if (modelFileParam.isSelected()) {
            System.out.println("reading model from file...");

            try {
                serverModel = association.getModelFromSclFile(modelFileParam.getValue());
            } catch (SclParseException e1) {
                System.out.println("Error parsing SCL file: " + e1.getMessage());
                return;
            }

            System.out.println("successfully read model");

        }
        else {
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

        actionProcessor.start();

        //
        // // example for writing a variable:
        // FcModelNode modCtlModel = (FcModelNode) serverModel.findModelNode("ied1lDevice1/CSWI1.Mod.ctlModel", Fc.CF);
        // association.setDataValues(modCtlModel);
        //
        // // example for enabling reporting
        // Urcb urcb = serverModel.getUrcb("ied1lDevice1/LLN0.urcb1");
        // if (urcb == null) {
        // System.out.println("ReportControlBlock not found");
        // }
        // else {
        // association.getRcbValues(urcb);
        // System.out.println("urcb name: " + urcb.getName());
        // System.out.println("RptId: " + urcb.getRptId());
        // System.out.println("RptEna: " + urcb.getRptEna().getValue());
        // association.reserveUrcb(urcb);
        // association.enableReporting(urcb);
        // association.startGi(urcb);
        // association.disableReporting(urcb);
        // association.cancelUrcbReservation(urcb);
        // }
        //
        // // example for reading a variable:
        // FcModelNode totW = (FcModelNode) serverModel.findModelNode("ied1lDevice1/MMXU1.TotW", Fc.MX);
        // BdaFloat32 totWmag = (BdaFloat32) totW.getChild("mag").getChild("f");
        // BdaTimestamp totWt = (BdaTimestamp) totW.getChild("t");
        // BdaQuality totWq = (BdaQuality) totW.getChild("q");
        //
        // while (true) {
        // association.getDataValues(totW);
        // System.out.println("got totW: mag " + totWmag.getFloat() + ", time " + totWt.getDate() + ", quality "
        // + totWq.getValidity());
        //
        // try {
        // Thread.sleep(5000);
        // } catch (InterruptedException e) {
        // }
        //
        // }

    }

}

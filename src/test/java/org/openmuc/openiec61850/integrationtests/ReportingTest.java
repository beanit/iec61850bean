package org.openmuc.openiec61850.integrationtests;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmuc.openiec61850.BasicDataAttribute;
import org.openmuc.openiec61850.BdaFloat32;
import org.openmuc.openiec61850.ClientAssociation;
import org.openmuc.openiec61850.ClientEventListener;
import org.openmuc.openiec61850.ClientSap;
import org.openmuc.openiec61850.DataSet;
import org.openmuc.openiec61850.Fc;
import org.openmuc.openiec61850.FcModelNode;
import org.openmuc.openiec61850.Report;
import org.openmuc.openiec61850.SclParseException;
import org.openmuc.openiec61850.SclParser;
import org.openmuc.openiec61850.ServerEventListener;
import org.openmuc.openiec61850.ServerModel;
import org.openmuc.openiec61850.ServerSap;
import org.openmuc.openiec61850.ServiceError;
import org.openmuc.openiec61850.Urcb;

public class ReportingTest implements ClientEventListener {

    private static final String PREEXISTING_DATASET_REFERENCE = "ied1lDevice1/LLN0$dataset1";
    private static final String CREATED_DATASET_REFERENCE = "ied1lDevice1/LLN0$datasetnew";
    private static final String CHANGING_SERVER_DA_REFERENCE_1 = "ied1lDevice1/MMXU1.W.phsA.cVal.mag.f";
    private static final String CHANGING_SERVER_DA_REFERENCE_2 = "ied1lDevice1/DSCH1.SchdAbsTm.sptestval1";
    private static final int PORT = 54321;
    private static final String ICD_FILE = "src/test/resources/openiec61850sample01.icd";
    private static final String URCB1_REFERENCE = "ied1lDevice1/LLN0.urcb101";

    private ServerSap serverSap;
    private ServerModel serverModel;
    private ServerModel clientModel;
    ClientAssociation clientAssociation;
    private int reportCounter = 0;

    @Before
    public void startServerAndClient() throws SclParseException, UnknownHostException, IOException, ServiceError {
        startServer();
        startClient();
    }

    private void startClient() throws IOException, UnknownHostException, ServiceError {
        ClientSap clientSap = new ClientSap();
        this.clientAssociation = clientSap.associate(InetAddress.getByName("localhost"), PORT, "", this);
        this.clientModel = this.clientAssociation.retrieveModel();
    }

    private void startServer() throws SclParseException, IOException {

        serverSap = new ServerSap(PORT, 0, null, SclParser.parse(ICD_FILE).get(0), null);

        this.serverSap.startListening(new ServerEventListener() {

            @Override
            public List<ServiceError> write(List<BasicDataAttribute> arg0) {
                return null;
            }

            @Override
            public void serverStoppedListening(ServerSap arg0) {
            }
        });

        this.serverModel = this.serverSap.getModelCopy();
    }

    @Test
    public void reportingTest() throws ServiceError, IOException, InterruptedException {
        Urcb urcb = this.clientModel.getUrcb(URCB1_REFERENCE);
        assertNotNull(urcb);

        this.clientAssociation.getRcbValues(urcb);
        this.clientAssociation.reserveUrcb(urcb);
        this.clientAssociation.enableReporting(urcb);

        Thread.sleep(500);

        BdaFloat32 mag = (BdaFloat32) this.serverModel.findModelNode(CHANGING_SERVER_DA_REFERENCE_1, Fc.MX);
        Assert.assertNotNull(mag);
        Assert.assertEquals(0, this.reportCounter);

        mag.setFloat(new Float(3.0));
        List<BasicDataAttribute> bdas = new ArrayList<>();
        bdas.add(mag);
        this.serverSap.setValues(bdas);

        Thread.sleep(500);

        Assert.assertEquals(1, this.reportCounter);

    }

    @Test
    public void reportingWithCreatedDataSetTest() throws ServiceError, IOException, InterruptedException {
        // BdaFloat32 clientMag = (BdaFloat32)this.clientModel.findModelNode(CHANGING_SERVER_DA_REFERENCE, Fc.MX);

        FcModelNode clientMag = (FcModelNode) this.clientModel.findModelNode(CHANGING_SERVER_DA_REFERENCE_1, Fc.MX);
        assertNotNull(clientMag);
        List<FcModelNode> dataSetMembers = new ArrayList<>();
        dataSetMembers.add(clientMag);

        DataSet dataSet = new DataSet(CREATED_DATASET_REFERENCE, dataSetMembers);
        this.clientAssociation.createDataSet(dataSet);

        Urcb urcb = this.clientModel.getUrcb(URCB1_REFERENCE);
        assertNotNull(urcb);

        this.clientAssociation.getRcbValues(urcb);

        Assert.assertEquals(PREEXISTING_DATASET_REFERENCE, urcb.getDatSet().getStringValue());

        System.out.println("dataset: " + urcb.getDatSet().getStringValue());

        this.clientAssociation.reserveUrcb(urcb);

        urcb.getDatSet().setValue(CREATED_DATASET_REFERENCE);
        List<ServiceError> serviceErrors = this.clientAssociation.setRcbValues(urcb, false, true, false, false, false,
                false, false, false);

        Assert.assertNull(serviceErrors.get(0));

        this.clientAssociation.getRcbValues(urcb);

        Assert.assertEquals(CREATED_DATASET_REFERENCE, urcb.getDatSet().getStringValue());

        this.clientAssociation.enableReporting(urcb);

        Thread.sleep(500);

        BdaFloat32 mag = (BdaFloat32) this.serverModel.findModelNode(CHANGING_SERVER_DA_REFERENCE_1, Fc.MX);
        Assert.assertNotNull(mag);
        Assert.assertEquals(0, this.reportCounter);

        mag.setFloat(new Float(3.0));
        List<BasicDataAttribute> bdas = new ArrayList<>();
        bdas.add(mag);
        this.serverSap.setValues(bdas);

        Thread.sleep(1_000);

        Assert.assertEquals(1, this.reportCounter);

    }

    @After
    public void disconnectAndStopServer() throws Exception {
        if (this.serverSap != null) {
            this.serverSap.stop();
        }

    }

    @Override
    public void associationClosed(IOException arg0) {
    }

    @Override
    public void newReport(Report arg0) {
        System.out.println("got a report.");
        synchronized (this) {
            this.reportCounter++;
        }
    }

}

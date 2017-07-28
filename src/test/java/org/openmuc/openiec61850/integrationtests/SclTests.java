package org.openmuc.openiec61850.integrationtests;

import org.junit.Test;
import org.openmuc.openiec61850.SclParseException;
import org.openmuc.openiec61850.ServerSap;

public class SclTests {

    private final static String SCL_FILE_PATH_1 = "src/test/resources/testModel.icd";
    private final static String SCL_FILE_PATH_2 = "src/test/resources/testModel2.icd";

    @Test
    public void testClientServerCom() throws SclParseException {

        ServerSap.getSapsFromSclFile(SCL_FILE_PATH_1);
        ServerSap.getSapsFromSclFile(SCL_FILE_PATH_2);
    }

}

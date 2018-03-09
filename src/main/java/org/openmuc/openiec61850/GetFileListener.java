package org.openmuc.openiec61850;

/**
 * Callback handler for GetFile service
 */
public interface GetFileListener {
    /**
     * Is called when a new block of file data is received
     * 
     * @param fileData
     *            block of file data received
     * @param moreFollows
     *            true if more data blocks will follow, false otherwise
     * 
     * @return true to continue the GetFile service, false to cancel
     */
    boolean dataReceived(byte[] fileData, boolean moreFollows);
}

package org.openmuc.openiec61850;

import java.util.Calendar;

/**
 * @brief Contains file information received by the GetFileDirectory service
 */
public class FileInformation 
{

    private String filename;
    
    private long fileSize;
    
    private Calendar lastModified;

    public String getFilename() {
        return filename;
    }

    public long getFileSize() {
        return fileSize;
    }

    public Calendar getLastModified() {
        return lastModified;
    }

    public FileInformation(String filename, long fileSize,
            Calendar lastModified) 
    {
        super();
        this.filename = filename;
        this.fileSize = fileSize;
        this.lastModified = lastModified;
    }
    
    
}

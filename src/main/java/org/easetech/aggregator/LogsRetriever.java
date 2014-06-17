

package org.easetech.aggregator;

import org.oclc.wms.LogAggregator;
import org.oclc.wms.LogUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.commons.io.FileUtils;
import org.oclc.log.internal.xml.Logging;

/**
 * A simple class that is responsible for retrieving the logs from a remote URL.
 * The URL from where the logs are to be fetched is constructed from the logProperties.xml file
 * entries. Mainly the host and port for each profile is used to fetch the logs.
 * Once the logs are retrieved, they are saved in to an appropriate directory that can then be searched by {@link LogAggregator}.
 * 
 */
public class LogsRetriever {

    /**
     * A map containing the destination where the logs are to be downloaded and the source from where they need to be downloaded.
     */
    private Map<String, String> destinationToSourceMap = new HashMap<String, String>();

    /**
     * A file that contains the result of retrieving the logs from remote URL.
     */
    private final String OUTPUT_OF_LOG_DOWNLOADER_FILE = "/logDownloadResult.txt";

    /**
     * The BASE Directory where the logs will be downloaded
     */
    private String BASE_DIR;
    
    /**
     * The time in minutes for the scheduler to schedule download of logs at regular interval
     */
    private final long timeInMin;

    /**
     * 
     * Construct a new LogsRetriever
     * @param propertyFileLocation the property file containing profile entries 
     */
    public LogsRetriever(String propertyFileLocation) {
        super();
        Logging logProperties = LogUtil.getProperties(LogUtil.getFileInputStream(propertyFileLocation));
        timeInMin = logProperties.getDownloadLogsIntervalInMin();
        destinationToSourceMap = LogUtil.getDestinationToSourceMap(logProperties);
        BASE_DIR = logProperties.getBaseDirForLogFiles();
        
    }

    /**
     * 
     * Construct a new LogsRetriever from a file input stream
     * @param fileInputStream the input stream for a property file
     */
    public LogsRetriever(InputStream fileInputStream) {
        super();
        Logging logProperties = LogUtil.getProperties(fileInputStream);
        timeInMin = logProperties.getDownloadLogsIntervalInMin();
        destinationToSourceMap = LogUtil.getDestinationToSourceMap(logProperties);
        BASE_DIR = logProperties.getBaseDirForLogFiles();
        
    }
    
    /**
     * Schedule a timer task to download the logs at regular interval
     */
    public void schedule() {
        Timer timer = new Timer();
        timer.schedule(new LogRetrieverTask(this), 0L, new Long(timeInMin*1000).longValue());
    }
    
    /**
     * 
     * A {@link TimerTask} to download the logs at regular interval
     *
     */
    class LogRetrieverTask extends TimerTask {

        /**
         * Instance of {@link LogsRetriever}
         */
        private LogsRetriever retriever;
        /**
         * 
         * Construct a new LogDownloaderTask
         * @param retriever instance of {@link LogsRetriever}
         */
        public LogRetrieverTask(LogsRetriever retriever) {
            this.retriever = retriever;
        }
        @Override
        public void run() {
            retriever.retrieve();
            
        }
        
    }
    /**
     * Get the map containing the destination where the logs are 
     * to be downloaded and the source from where they need to be downloaded.
     * @return the destinationToSourceMap
     */
    public Map<String, String> getDestinationToSourceMap() {
        return destinationToSourceMap;
    }

    /**
     * Actual business method to retrieve the logs from a remote URL and save them to a local file
     */
    public void retrieve() {

        /**
         * Instantiate a file that will capture the results of this function into a file.
         */
        File resultCapturingFile = new File(BASE_DIR.concat("/").concat(OUTPUT_OF_LOG_DOWNLOADER_FILE));
        if (resultCapturingFile.exists()) {
            resultCapturingFile.delete();

        }

        try {
            resultCapturingFile.createNewFile();
        } catch (IOException e1) {
            //Do nothing
        }
        Map<String, String> map = getDestinationToSourceMap();
        Writer writer = null;
        try {
            if(resultCapturingFile.exists()) {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultCapturingFile), "utf-8"));
            }
            for (String destinationLocation : map.keySet()) {
                File file = new File(destinationLocation);
                try {
                    FileUtils.copyURLToFile(new URL(map.get(destinationLocation)), file);
                } catch (Exception e) {
                    if(writer != null) {
                        writer.write("Could not fetch file from : " + map.get(destinationLocation)
                            + " and write to location : " + destinationLocation);
                        writer.write(System.getProperty( "line.separator" ));
                        writer.write("Exception : " + e);
                        writer.write(System.getProperty( "line.separator" ));
                    }
                    continue;
                } 
                if(writer != null) {
                    writer.write("Successfully written the data FROM :" + map.get(destinationLocation) + " TO FILE :" + destinationLocation);
                    writer.write(System.getProperty( "line.separator" ));
                }
                
            }
        } catch (IOException ex) {
            // Do nothing for now
        } finally {
            try {
                if(writer != null)
                    writer.close();
            } catch (Exception ex) {
               
                //do Nothing
            }
        }

        

    }
}

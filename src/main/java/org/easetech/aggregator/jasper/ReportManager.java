
package org.easetech.aggregator.jasper;

import java.io.OutputStream;
import java.util.Map;
import net.sf.jasperreports.engine.JRDataSource;

/**
 * Provides methods to handle reports
 */
public interface ReportManager {

    /**
     * Runs a report
     * 
     * @param reportName Report name
     * @param format Format
     * @param reportParameters Parameters
     * @param jrDataSource jrDataSource
     * @param outputStream Output Stream
     * @return The content of the report
     */
    Object runReport(String reportName, String format, Map<String, String> reportParameters, JRDataSource jrDataSource, OutputStream outputStream);
}

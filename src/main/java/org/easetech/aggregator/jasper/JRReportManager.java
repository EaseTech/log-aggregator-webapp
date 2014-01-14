
package org.easetech.aggregator.jasper;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/**
 * Implementation of {@link ReportManager} to handle Jasper Reports
 */
public class JRReportManager implements ReportManager {
    
    public static final String ENCODING = "ISO-8859-1"; // ISO-8859-1 UTF-8
    
    /** Jasper report compiler */
    private JRReportCompiler jrReportCompiler;
    
    /** Jasper report runner */
    private JRReportRunner jrReportRunner;
    
    public JRReportManager() {
        jrReportCompiler = new JRReportCompiler();
        jrReportRunner = new JRReportRunner();
    }

    @Override
    public Object runReport(String reportName, String format, Map<String, String> reportParameters, JRDataSource jrDataSource, OutputStream outputStream) {
        JasperReport jasperReport = jrReportCompiler.compileReport(reportName);
        JasperPrint jasperPrint = null;
        
        Map<JRExporterParameter, Object> exporterParameters = new HashMap<JRExporterParameter, Object>();

        Map<String, Object> parameters = new HashMap<String, Object>();
        
        for (Entry<String, String> entry : reportParameters.entrySet()) {
            parameters.put(entry.getKey(), entry.getValue());
        }
        
        if (jasperReport != null) {
            try {
                if (format.equals("html")) {
                    jasperPrint = jrReportRunner.fillReport(jasperReport, parameters, jrDataSource);
                    StringBuffer htmlContent = new StringBuffer();
                    
                    exporterParameters.clear();
                    exporterParameters.put(JRExporterParameter.OUTPUT_STRING_BUFFER, htmlContent);
                    jrReportRunner.setReportParameters(exporterParameters);
                    
                    jrReportRunner.runReport(jasperPrint, OutputFormatType.HTML);
                    return htmlContent.toString();
                } else if (format.equals("pdf")) {
                    jasperPrint = jrReportRunner.fillReport(jasperReport, parameters, jrDataSource);
                    
                    exporterParameters.clear();
                    exporterParameters.put(JRExporterParameter.OUTPUT_STREAM, outputStream);
                    jrReportRunner.setReportParameters(exporterParameters);
                    
                    jrReportRunner.runReport(jasperPrint, OutputFormatType.PDF);
                    return null;
                }
            } catch (Exception e) {
                System.out.println("Error filling report " + reportName + " for format " + format + ". Reason: "
                    + e.getMessage());
                return null;
            }
        }

        return null;
    }

    /**
     * Setter for the {@link JRReportCompiler}
     * 
     * @param jrReportCompiler JRReportCompiler to set
     */
    public void setJrReportCompiler(JRReportCompiler jrReportCompiler) {
        this.jrReportCompiler = jrReportCompiler;
    }

    /**
     * Setter for the {@link JRReportRunner}
     * 
     * @param jrReportRunner JRReportRunner to set
     */
    public void setJrReportRunner(JRReportRunner jrReportRunner) {
        this.jrReportRunner = jrReportRunner;
    }

}


package org.easetech.aggregator.jasper;

import java.util.HashMap;
import java.util.Map;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRHtmlExporter;
import net.sf.jasperreports.engine.export.JRHtmlExporterParameter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
import net.sf.jasperreports.engine.export.JRRtfExporter;
import net.sf.jasperreports.engine.export.JRTextExporter;
import net.sf.jasperreports.engine.export.JRTextExporterParameter;
import net.sf.jasperreports.engine.export.ooxml.JRDocxExporter;

/**
 * Class will run the compiled report using the supplied datasources and convert it to the required format.
 * 
 */
public class JRReportRunner {

    /** Page Height */
    private static final Integer PAGE_HEIGHT = new Integer(798);
    /** Page width */
    private static final Integer PAGE_WIDTH = new Integer(581);
    /** Character width */
    private static final Float CHARACTER_WIDTH = new Float(7);
    /** Character Height */
    private static final Float CHARACTER_HEIGHT = new Float(14);
    /** Zoom Ratio */
    private static final float ZOOM_RATIO = 1.5f;
    /** Image UI */
    private static final String IMAGES_URI = "cid:";

    /**
     * Map of report parameters
     */
    private Map<JRExporterParameter, Object> reportParameters = new HashMap<JRExporterParameter, Object>();

    /**
     * Generates the report to a PDF format.
     * 
     * @throws JRException In case of an error
     */
    private void runPdfReport() throws JRException {
        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setParameters(this.reportParameters);
        exporter.exportReport();
    }

    /**
     * Generates the report to an RTF format.
     * 
     * @throws JRException In case of an error
     */
    private void runRtfReport() throws JRException {
        JRRtfExporter exporter = new JRRtfExporter();
        exporter.setParameters(this.reportParameters);
        exporter.exportReport();
    }

    /**
     * Generates the report to a .docx format (Windows Word)
     * 
     * @throws JRException In case of an error
     */
    private void runDocxReport() throws JRException {
        JRDocxExporter exporter = new JRDocxExporter();
        exporter.setParameters(this.reportParameters);
        exporter.exportReport();
    }

    /**
     * Generates the report to an HTML format.
     * 
     * @throws JRException In case of an error
     */
    private void runHtmlReport() throws JRException {
        this.reportParameters.put(JRHtmlExporterParameter.IS_USING_IMAGES_TO_ALIGN, false);
        this.reportParameters.put(JRHtmlExporterParameter.CHARACTER_ENCODING, JRReportManager.ENCODING);
        JRHtmlExporter exporter = new JRHtmlExporter();
        exporter.setParameters(this.reportParameters);
        exporter.exportReport();
    }

    /**
     * Generates the report to printer.
     * 
     * @throws JRException In case of an error
     */
    private void runPrinterReport() throws JRException {
        JRPrintServiceExporter exporter = new JRPrintServiceExporter();
        exporter.setParameters(this.reportParameters);
        exporter.exportReport();
    }

    /**
     * Generates the report to mail format. The {@link org.springframework.mail.javamail.JavaMailSenderImpl} is used for
     * this and a MIME message is generated and send.
     * 
     * @throws JRException In case of an error
     */
    private void runHtmlMailReport() throws JRException {
        this.reportParameters.put(JRHtmlExporterParameter.IMAGES_URI, IMAGES_URI);
        this.reportParameters.put(JRHtmlExporterParameter.ZOOM_RATIO, ZOOM_RATIO);
        this.reportParameters.put(JRExporterParameter.IGNORE_PAGE_MARGINS, true);
        this.runHtmlReport();
    }

    /**
     * Generates a text report
     * 
     * @throws JRException In case of an error
     */
    private void runTextMailReport() throws JRException {
        runTextReport();
    }

    /**
     * Generates a text report
     * 
     * @throws JRException In case of an error
     */
    private void runTextReport() throws JRException {
        JRTextExporter exporter = new JRTextExporter();
        // standard A4
        this.reportParameters.put(JRTextExporterParameter.BETWEEN_PAGES_TEXT, "\f");
        this.reportParameters.put(JRTextExporterParameter.PAGE_HEIGHT, PAGE_HEIGHT);
        this.reportParameters.put(JRTextExporterParameter.PAGE_WIDTH, PAGE_WIDTH);
        this.reportParameters.put(JRTextExporterParameter.CHARACTER_WIDTH, CHARACTER_WIDTH);
        this.reportParameters.put(JRTextExporterParameter.CHARACTER_HEIGHT, CHARACTER_HEIGHT);
        exporter.setParameters(reportParameters);
        exporter.exportReport();
    }

    /**
     * Fills the report with the data and returns a JasperPrint object.
     * 
     * @param report The report
     * @param parameters A map with parameters
     * @param jrDataSource The connection to be used
     * @return The JasperPrint object
     */
    public JasperPrint fillReport(JasperReport report, Map<String, Object> parameters, JRDataSource jrDataSource) {
        JasperPrint jasperPrint = null;

        try {
            System.out.println("Filling report " + report.getName() + " with data.");
            jasperPrint = JasperFillManager.fillReport(report, parameters, jrDataSource);
        } catch (JRException e) {
            System.out.println("Error filling layout " + report.getName() + ": " + e.getMessage());
        }

        return jasperPrint;

    }

    /**
     * Fills the report with the data and returns a JasperPrint object.
     * 
     * @param report The report
     * @param data The data
     * @return The JasperPrint object
     */
    public JasperPrint fillReport(JasperReport report, JRDataSource data) {
        JasperPrint jasperPrint = null;

        try {
            System.out.println("Filling report " + report.getName() + " with data.");
            jasperPrint = JasperFillManager.fillReport(report, null, data);
        } catch (JRException e) {
            System.out.println("Error filling layout " + report.getName() + ": " + e.getMessage());
        }

        return jasperPrint;
    }

    /**
     * Runs the report to the specified output type.
     * 
     * @param jasperPrint The JasperPrints list to be run.
     * @param outputType The type of report to be generated ({@link OutputFormatType})
     */
    public void runReport(JasperPrint jasperPrint, int outputType) {
        String reportName = jasperPrint.getName();
        try {
            System.out.println("Setting parameters for report");
            this.reportParameters.put(JRExporterParameter.JASPER_PRINT, jasperPrint);

            switch (outputType) {
            case OutputFormatType.HTML:
                System.out.println("Running HTML report " + reportName);
                this.runHtmlReport();
                break;

            case OutputFormatType.RTF:
                System.out.println("Running RTF report " + reportName);
                this.runRtfReport();
                break;

            case OutputFormatType.DOCX:
                System.out.println("Running DOCX report " + reportName);
                this.runDocxReport();
                break;

            case OutputFormatType.PDF:
                System.out.println("Running PDF report " + reportName);
                this.runPdfReport();
                break;

            case OutputFormatType.PRINTER:
                PrintServiceAttributeSet printServiceAttributeSet = new HashPrintServiceAttributeSet();
                // this.reportParameters.put(JRPrintServiceExporterParameter.PRINT_SERVICE,
                // this.reportParameters.get(MessageWriterParameter.PRINT_SERVICE));
                this.reportParameters.put(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET,
                    printServiceAttributeSet);

                this.reportParameters.put(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
                this.reportParameters.put(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
                System.out.println("Printing report " + reportName);
                this.runPrinterReport();
                break;

            case OutputFormatType.HTML_MAIL:
                System.out.println("Mailing HTML report " + reportName);
                this.runHtmlMailReport();
                break;

            case OutputFormatType.TEXT_MAIL:
                System.out.println("Mailing text report " + reportName);
                this.runTextMailReport();
                break;

            case OutputFormatType.TEXT:
                System.out.println("Running text report " + reportName);
                this.runTextReport();
                break;

            default:
                break;
            }
        } catch (JRException e) {
            System.out.println("Error running layout " + reportName + ": " + e.getMessage());
        } catch (ClassCastException e) {
            System.out.println("Error casting parameter value: " + e.getMessage());
        } finally {
            // Clear the report parameters, so they won't get in the way for
            // the next report.
            this.reportParameters.clear();
        }
    }

    /**
     * Sets a report parameter for customization of the export process.
     * 
     * @param parameter The parameter from the parameters defined
     * @param value The parameter value
     */
    public void setReportParameter(JRExporterParameter parameter, Object value) {
        this.reportParameters.put(parameter, value);
    }

    /**
     * Sets parameters from a specified map.
     * 
     * @param reportParameters The map containing the parameters
     * @see #setReportParameter(JRExporterParameter, Object)
     */
    public void setReportParameters(Map<JRExporterParameter, Object> reportParameters) {
        this.reportParameters = reportParameters;
    }

}

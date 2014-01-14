/**
 * 
 */

package org.easetech.aggregator.jasper;

import java.util.HashMap;

import java.util.Collection;
import java.util.Map;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRValidationFault;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * Jasper Report compiler
 */
public class JRReportCompiler {
    
    private Map<String, JasperReport> reportCache;
        
    public JRReportCompiler() {
        reportCache = new HashMap<String, JasperReport>();
    }

    /**
     * Compiles a report
     * 
     * @param reportFileName file name of the report
     * @return an instance of {@link JasperReport} with the compiled report
     */
    public JasperReport compileReport(String reportFileName) {
        JasperDesign jasperDesign;
        JasperReport jasperReport = null;
        try {
            // get report from cache
            JasperReport jasperReportFromCache = reportCache.get(reportFileName);
            if (jasperReportFromCache != null) {
                return jasperReportFromCache;
            }
            
            // compile report
            jasperDesign = JRXmlLoader.load(getClass().getResourceAsStream(reportFileName));
            jasperReport = this.compileReport(jasperDesign);
        } catch (JRException e) {
            jasperReport = null;
            System.out.println("Error loading report " + reportFileName + ". Reason: " + e.getMessage());
        }
        reportCache.put(reportFileName, jasperReport);
        
        return jasperReport;
    }

    /**
     * Compiles a report based on the jasper design
     * 
     * @param jasperDesign jasper design
     * @return an instance of {@link JasperReport} with the compiled report
     */
    private JasperReport compileReport(JasperDesign jasperDesign) {
        JasperReport jasperReport = null;
        try {
            Collection<JRValidationFault> errors = JasperCompileManager.verifyDesign(jasperDesign);
            if (!errors.isEmpty()) {
                System.out.println("Found errors validating design " + jasperDesign.getName());
                for (JRValidationFault jrValidationFault : errors) {
                    System.out.println(jrValidationFault.getMessage());
                }
            }
            jasperReport = JasperCompileManager.compileReport(jasperDesign);
        } catch (Throwable e) {
            System.out.println("Error compiling report " + jasperDesign.getName() + ". Reason: " + e.getMessage());
            jasperReport = null;
        }

        return jasperReport;
    }
}

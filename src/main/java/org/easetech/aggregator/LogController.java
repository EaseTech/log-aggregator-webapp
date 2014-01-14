
package org.easetech.aggregator;

import org.easetech.aggregator.jasper.JRReportManager;
import org.easetech.aggregator.jasper.ReportManager;

import org.oclc.wms.ConstantExpression;
import org.oclc.wms.ExceptionCountBean;
import org.oclc.wms.LogAggregator;
import org.oclc.wms.Result;
import org.oclc.wms.UniqueExceptionsBean;

import com.sun.jersey.multipart.FormDataParam;
import com.sun.jersey.spi.container.servlet.PerSession;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.IOUtils;
import org.grep4j.core.result.GrepResults;

/**
 * 
 * Controller for Log Aggregation Functionality.
 * This is a per session controller, which means that it will be instantiated per session.
 * Thus we can maintain the user data in a local cache which makes the search quicker.
 *
 */
@Path("/")
@PerSession
public class LogController {

    /**
     * The list of Custom Reports supported by the Log Aggregator framework
     */
    private final List<ReportName> reportNames;

    /**
     * Default location and name for the property file.
     */
    private final String PROPERTY_FILE_LOCATION = "logProperties.xml";

    /**
     * Service responsible for aggregating the logs
     */
    private LogAggregator aggregator;

    /**
     * Local per session cache of searched data
     */
    private Map<String, Object> cacheOfSearchedData = new HashMap<String, Object>();

    /**
     * Date time format supported
     */
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    /**
     * Date format supported
     */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    /**
     * Start time in case the user didnt specy one during stack trace searching
     */
    private static final String START_TIME = " 00:00:00.000";

    /** Jasper report manager */
    private ReportManager reportManager;
    
    private enum REPORT_TYPES {
        UENR("Unique Exception Names Report", "/UniqueExceptions.jrxml"),
        ECR("Exception and Count Report", "/ExceptionCount.jrxml"),
        TER("Truncated Exception Report", "/UniqueExceptions.jrxml");
        
        private String name;
        private String reportJrxml;
        
        private REPORT_TYPES(String name, String reportJrxml) {
            this.name = name;
            this.reportJrxml = reportJrxml;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @return the reportJrxml
         */
        public String getReportJrxml() {
            return reportJrxml;
        }
    }

    /**
     * 
     * Construct a new LogController.
     * This will instantiate the list of supported reports
     */
    public LogController() {
        this.reportNames = new ArrayList<ReportName>();
        ReportName name1 = new ReportName("ExceptionName", "Unique Exception Names Report", false);
        ReportName name2 = new ReportName("ExceptionCount", "Exception and Count Report", false);
        ReportName name3 = new ReportName("TruncatedException", "Truncated Exception Report", false);
        reportNames.add(name1);
        reportNames.add(name2);
        reportNames.add(name3);
        
        reportManager = new JRReportManager();
    }
    
    // E.G. http://localhost:8080/Logging-Rest-Server/log/generateReport?envName=Leiden%20Spoke&appName=Circulation&compName=Circ%20BL,&filepath=Enter%20file%20path&reports=ExceptionName&showCachedData=true&reportType=UENR&reportFormat=pdf
    @GET
    @Path("/generateReport")
    public Response getJasperReport(@QueryParam("appName") String appName, 
        @QueryParam("envName") String envName, 
        @QueryParam("compName") String compName, 
        @QueryParam("reports") String reports, 
        @QueryParam("showCachedData") boolean showCachedData,
        @QueryParam("reportType") String reportType,
        @QueryParam("reportFormat") String reportFormat) {
        
        if (aggregator == null) {
            this.initialize();
        }
        
        Boolean validateReportParameters = validateReportParameters(reportType, reportFormat);
        if (!validateReportParameters) {
            return Response.ok().build();
        }
        
        REPORT_TYPES type = REPORT_TYPES.valueOf(reportType);
        
        List<?> reportBeans = null;
        
        if (type.equals(REPORT_TYPES.UENR) || type.equals(REPORT_TYPES.TER)) {
            reportBeans = fetchLogs(appName, envName, compName, reports, showCachedData);
        } else if (type.equals(REPORT_TYPES.ECR)) {
            reportBeans = fetchExceptionCountLogs(appName, envName, compName, reports, showCachedData);
        }
        
        try {
            return generateJasperReportsResponse(reportBeans, type, reportFormat);
        } catch (IOException e) {
            System.out.println("Error generating report for type " + type +" and format " + reportFormat + "; Message: " + e.getMessage());
            e.printStackTrace();
        }

        return Response.ok().build();
    }
    
    public Response generateJasperReportsResponse(List<?> reportBeans, REPORT_TYPES reportType, String reportFormat) throws IOException {
        final String reportFileName = reportType.getReportJrxml();
        
        final Map<String, String> parameters = new HashMap<String, String>();
        
        final JRDataSource jrDataSource = getReportBeansBeansJrDataSource(reportBeans);
        
        return exportReportToResponse(jrDataSource, reportFormat, reportFileName, parameters);
    }

    private Response exportReportToResponse(final JRDataSource jrDataSource, String reportFormat, final String reportFileName,
        final Map<String, String> parameters) throws IOException {
        StreamingOutput streamingOutput = null;

        if (reportFormat.equalsIgnoreCase("pdf")) {
            streamingOutput = exportToPDF(reportFileName, parameters, jrDataSource);
            return Response.ok(streamingOutput).type(MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment; filename=" + reportFileName + ".pdf").build();
        } else if (reportFormat.equalsIgnoreCase("html")) {
            streamingOutput = exportToHTML(reportFileName, parameters, jrDataSource);
            return Response.ok(streamingOutput).type(MediaType.TEXT_HTML).build();
        }
        return Response.ok().build();
    }
    
    private Boolean validateReportParameters(String reportType, String reportFormat) {
        try {
            REPORT_TYPES.valueOf(reportType);
        } catch (Exception e) {
            System.out.println("No report type " + reportType + " found [UENR, ECR, TER]");
            return false;
        }
        
        if (reportFormat == null || !(reportFormat.equalsIgnoreCase("pdf") || reportFormat.equalsIgnoreCase("html"))) {
            System.out.println("No report format " + reportFormat + " found [pdf, html]");
            return false;
        }
        
        return true;
    }    
    
    /**
     * Method called during the loading of application.
     * This method  returns the list of Environments as well as supported reports for initialization of the UI
     * @return the list of Environments as well as supported reports for initialization of the UI
     */
    @GET
    @Path("/init")
    public LogProperties initialize() {
        aggregator = new LogAggregator(PROPERTY_FILE_LOCATION);

        LogProperties logProperties = new LogProperties();

        logProperties.setEnvNames(aggregator.getEnvironmentNames());
        logProperties.setReports(reportNames);
        
        return logProperties;
    }
    
    /**
     * Fetch the names of the supported components for the given app name and env name
     * @param appName the application Name
     * @param envName the environment Name
     * @return a collection of component names
     */
    @GET
    @Path("/fetchComponentNames")
    public List<String> getComponentNames(@QueryParam("appName")
    String appName, @QueryParam("envName")
    String envName) {
        
        Set<String> compNames = aggregator.getComponentNames(envName, appName);
        List<String> compNameList = new ArrayList<String>(compNames);
        Collections.sort(compNameList);
        return compNameList;
    }
    
    /**
     * Fetch the names of the supported applications for the given env name
     * @param envName the name of the environment
     * @return collection of Application names
     */
    @GET
    @Path("/fetchApplicationNames")
    public List<String> getApplicationNames(@QueryParam("envName")
    String envName) {

        List<String> appNameList = new ArrayList<String>(aggregator.getApplicationNames(envName));
        Collections.sort(appNameList);
        return appNameList;
    }

    /**
     * Method responsible for handling the upload of XML file and reading it.
     * @param uploadedInputStream the file input stream for the uploaded file.
     * @return the updated log properties
     * @throws IOException 
     */
    @POST
    @Path("/uploadFile")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public LogProperties getEnvData(@FormDataParam("file")
    InputStream uploadedInputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(uploadedInputStream, baos);
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream logDownloaderInputStream = new ByteArrayInputStream(bytes);
        
        LogsRetriever downloader = new LogsRetriever(logDownloaderInputStream);
        downloader.retrieve();
        ByteArrayInputStream logAggregatorInputStream = new ByteArrayInputStream(bytes);
        aggregator.loadProperties(logAggregatorInputStream);

        LogProperties logProperties = new LogProperties();

        logProperties.setEnvNames(aggregator.getEnvironmentNames());
        logProperties.setReports(reportNames);
        
        return logProperties;
    }

    /**
     * Method used to generate reports. Currently not used as Jasper is throwing some exceptions.
     * @param appName
     * @param envName
     * @param compName
     * @param reports
     * @param showCachedData
     * @return
     */
    @GET
    @Path("/generateReports")
    public LogProperties generateReports(@QueryParam("appName")
    String appName, @QueryParam("envName")
    String envName, @QueryParam("compName")
    String compName, @QueryParam("reports")
    String reports, @QueryParam("showCachedData")
    boolean showCachedData) {
        String key = null;
        LogProperties logProperties = new LogProperties();
        if (reports != null) {
            String[] reportNames = reports.split(",");
            for (String report : reportNames) {
                if ("ExceptionName".equals(report)) {
                    List<String> profiles = new ArrayList<String>();
                    String[] profileNames = compName.split(",");
                    for (String profileName : profileNames) {
                        if (profileName != null || !"".equals(profileName)) {
                            profiles.add(profileName);
                        }
                    }
                    Result result;
                    try {
                        key = envName.concat(":").concat(appName).concat(":").concat(profiles.toString())
                            .concat("ExceptionName");
                        if (showCachedData) {

                            logProperties = (LogProperties) cacheOfSearchedData.get(key);
                            if (logProperties != null) {
                                return logProperties;
                            }
                        }
                        result = aggregator.generateUniqueExceptionNamesReport(envName,
                            appName, profiles);
                        System.out.println(result);
                    } catch (Exception e) {
                        System.out.println(e);
                    }

                }
            }
        }

        logProperties.setEnvNames(aggregator.getEnvironmentNames());


        if (showCachedData) {
            cacheOfSearchedData.put(key, logProperties);
        }
        return logProperties;
    }

    /**
     * Fetch the Log entries for Exception Name OR Truncated Exceptions Report
     * @param appName the name of the application
     * @param envName the name of the environment
     * @param compName the comma separated list of components
     * @param reports the report to be generated
     * @param showCachedData whether data shoould be shown from cache
     * @return list of {@link UniqueExceptionsBean}
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/fetchLogs")
    public List<UniqueExceptionsBean> fetchLogs(@QueryParam("appName") String appName, 
        @QueryParam("envName") String envName, 
        @QueryParam("compName") String compName, 
        @QueryParam("reports") String reports, 
        @QueryParam("showCachedData") boolean showCachedData) {
        String key = null;
        List<UniqueExceptionsBean> result = new ArrayList<UniqueExceptionsBean>();
        if (reports != null) {
            String[] reportNames = reports.split(",");
            for (String report : reportNames) {
                if ("ExceptionName".equals(report) || "TruncatedException".equals(report)) {
                    List<String> profiles = new ArrayList<String>();
                    String[] profileNames = compName.split(",");
                    for (String profileName : profileNames) {
                        if (profileName != null || !"".equals(profileName)) {
                            profiles.add(profileName);
                        }
                    }
                    if ("ExceptionName".equals(report)) {
                        key = envName.concat(":").concat(appName).concat(":").concat(profiles.toString())
                            .concat("ExceptionName");
                        if (showCachedData) {
                            result = (List<UniqueExceptionsBean>) cacheOfSearchedData.get(key);
                            if (result != null) {
                                return result;
                            } else {
                                result = new ArrayList<UniqueExceptionsBean>();
                            }
                        } else {
                            cacheOfSearchedData.remove(key);
                        }
                        result.addAll(aggregator.fetchUniqueExceptionNames(envName,
                            appName, profiles));
                    } else if ("TruncatedException".equals(report)) {
                        key = envName.concat(":").concat(appName).concat(":").concat(profiles.toString())
                            .concat("TruncatedException");
                        if (showCachedData) {
                            result = (List<UniqueExceptionsBean>) cacheOfSearchedData.get(key);
                            if (result != null) {
                                return result;
                            } else {
                                result = new ArrayList<UniqueExceptionsBean>();
                            }
                        } else {
                            cacheOfSearchedData.remove(key);
                        }
                        result.addAll(aggregator.fetchUniqueExceptionsWithTruncatedDetail(envName,
                            appName, profiles));
                    }

                }
            }
        }
        if (showCachedData) {
            cacheOfSearchedData.put(key, result);
        }
        return result;
    }

    /**
     * Fetch the list of Exceptions and their count for the given env, application and components
     * @param appName the name of the application
     * @param envName the name of the environment
     * @param compName the comma separated list of components
     * @param reports the report to be generated
     * @param showCachedData whether data should be shown from cache
     * @return list of {@link ExceptionCountBean}
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/fetchExceptionCountLogs")
    public List<ExceptionCountBean> fetchExceptionCountLogs(@QueryParam("appName") String appName, 
        @QueryParam("envName") String envName, 
        @QueryParam("compName") String compName, 
        @QueryParam("reports") String reports, 
        @QueryParam("showCachedData") boolean showCachedData) {
        String key = null;
        List<ExceptionCountBean> result = new ArrayList<ExceptionCountBean>();
        if (reports != null) {
            String[] reportNames = reports.split(",");
            for (String report : reportNames) {
                if ("ExceptionCount".equals(report)) {

                    List<String> profiles = new ArrayList<String>();
                    String[] profileNames = compName.split(",");
                    for (String profileName : profileNames) {
                        if (profileName != null || !"".equals(profileName)) {
                            profiles.add(profileName);
                        }
                    }
                    key = envName.concat(":").concat(appName).concat(":").concat(profiles.toString())
                        .concat("ExceptionCount");
                    if (showCachedData) {
                        result = (List<ExceptionCountBean>) cacheOfSearchedData.get(key);
                        if (result != null) {
                            return result;
                        } else {
                            result = new ArrayList<ExceptionCountBean>();
                        }
                    } else {
                        cacheOfSearchedData.remove(key);
                    }
                    result.addAll(aggregator.fetchExceptionAndCount(envName, appName,
                        profiles));

                }
            }
        }
        if (showCachedData) {
            cacheOfSearchedData.put(key, result);
        }
        return result;
    }

    /**
     * Fetch the logs using Constant Expression
     * @param appName the name of the application
     * @param envName the name of the environment
     * @param compName the comma separated list of components
     * @param constantExpr the expr to search
     * @param showCachedData whether data should be shown from cache
     * @return list of Constant Expression logs
     */
    @SuppressWarnings("unchecked")
    @GET
    @Path("/fetchUsingConstantExpression")
    public List<ConstantExpression> fetchUsingConstantExpression(@QueryParam("appName") String appName, 
        @QueryParam("envName") String envName, 
        @QueryParam("compName") String compName, 
        @QueryParam("constantExpr") String constantExpr, 
        @QueryParam("showCachedData") boolean showCachedData) {
        String key = null;
        List<ConstantExpression> result = new ArrayList<ConstantExpression>();
        List<String> profiles = new ArrayList<String>();
        String[] profileNames = compName.split(",");
        for (String profileName : profileNames) {
            if (profileName != null || !"".equals(profileName)) {
                profiles.add(profileName);
            }
        }
        key = envName.concat(":")
                .concat(appName)
                .concat(":")
                .concat(profiles.toString())
                .concat(constantExpr)
                .concat("fetchUsingConstantExpression");
        
        if (showCachedData) {
            result = (List<ConstantExpression>) cacheOfSearchedData.get(key);
            if (result != null) {
                return result;
            } else {
                result = new ArrayList<ConstantExpression>();
            }
        } else {
            cacheOfSearchedData.remove(key);
        }
        List<String> logs = aggregator.fetchUsingConstantExpression(envName, appName,
            profiles, constantExpr);
        for (String log : logs) {
            ConstantExpression expr = new ConstantExpression();
            expr.setExpression(log);
            result.add(expr);
        }
        if (showCachedData) {
            cacheOfSearchedData.put(key, result);
        }
        return result;
    }

    /**
     * Fetch the logs using Constant Expression
     * @param appName the name of the application
     * @param envName the name of the environment
     * @param compName the comma separated list of components
     * @param regExpr the expr to search
     * @param showCachedData whether data should be shown from cache
     * @return list of Constant Expression logs
     */
    @SuppressWarnings("unchecked")
    @GET    
    @Path("/fetchUsingRegularExpression")
    public List<ConstantExpression> fetchUsingRegularExpression(@QueryParam("appName") String appName, 
        @QueryParam("envName") String envName, 
        @QueryParam("compName") String compName, 
        @QueryParam("regExpr")String regExpr,
        @QueryParam("showCachedData") boolean showCachedData) {
        List<ConstantExpression> result = new ArrayList<ConstantExpression>();
        String key = null;
        List<String> profiles = new ArrayList<String>();
        String[] profileNames = compName.split(",");
        for (String profileName : profileNames) {
            if (profileName != null || !"".equals(profileName)) {
                profiles.add(profileName);
            }
        }
        key = envName.concat(":").concat(appName).concat(":").concat(profiles.toString()).concat(regExpr).concat("fetchUsingRegularExpression");
        if (showCachedData) {
            result = (List<ConstantExpression>) cacheOfSearchedData.get(key);
            if (result != null) {
                return result;
            } else {
                result = new ArrayList<ConstantExpression>();
            }
        } else {
            cacheOfSearchedData.remove(key);
        }
        List<String> logs = aggregator.fetchUsingRegularExpression(envName, appName,
            profiles, regExpr);
        for (String log : logs) {
            ConstantExpression expr = new ConstantExpression();
            expr.setExpression(log);
            result.add(expr);
        }
        if (showCachedData) {
            cacheOfSearchedData.put(key, result);
        }
        return result;
    }

    /**
     * Fetch the exception stack trace for te given Env, application and components with the given search criteria
     * @param appName the name of the application
     * @param envName the name of the environment
     * @param compName the comma separated list of components
     * @param showCachedData whether data shoould be shown from cache
     * @param expr the expression to search
     * @param startDate the optional start date. Default to Today's date
     * @param endDate the optional end date. Defaults to Now.
     * @param linesBefore the lines to fetch before the exception
     * @param linesAfter lines to fetch after the exception line
     * @return searched results
     */
    @GET
    @Path("/fetchExceptionTrace")
    public GrepResults fetchExceptionStackTrace(@QueryParam("appName") String appName, 
        @QueryParam("envName") String envName, 
        @QueryParam("compName") String compName, 
        @QueryParam("expr") String expr, 
        @QueryParam("startDate") String startDate, 
        @QueryParam("endDate") String endDate, 
        @QueryParam("linesBefore") Integer linesBefore, 
        @QueryParam("linesAfter") Integer linesAfter, 
        @QueryParam("showCachedData") boolean showCachedData) {
        String key = null;
        GrepResults result = new GrepResults();
        List<String> profiles = new ArrayList<String>();
        String[] profileNames = compName.split(",");
        for (String profileName : profileNames) {
            if (profileName != null || !"".equals(profileName)) {
                profiles.add(profileName);
            }
        }

        if (startDate == null || startDate.isEmpty()) {
            startDate = DATE_FORMAT.format(new Date());
            startDate.concat(START_TIME);
        }
        if (endDate == null || endDate.isEmpty() || endDate.equals("undefined")) {
            endDate = DATE_TIME_FORMAT.format(new Date());
        }
        key = envName.concat(":")
                     .concat(appName)
                     .concat(":")
                     .concat(profiles.toString())
                     .concat(":")
                     .concat(expr)
                     .concat(":")
                     .concat(startDate)
                     .concat(":")
                     .concat(endDate)
                     .concat(":")
                     .concat(linesBefore.toString())
                     .concat(":")
                     .concat(linesAfter.toString());
        if (showCachedData) {
            result = (GrepResults) cacheOfSearchedData.get(key);
            if (result != null) {
                return result;
            } else {
                result = new GrepResults();
            }
        } else {
            cacheOfSearchedData.remove(key);
        }
        result = aggregator.fetchExceptionStackTrace(envName, appName, profiles,
            expr, startDate, endDate, linesBefore, linesAfter);

        if (showCachedData) {
            cacheOfSearchedData.put(key, result);
        }
        return result;
    }
    
    //==================================
    
    private StreamingOutput exportToHTML(final String reportFileName, final Map<String, String> parameters, final JRDataSource jrDataSource) throws IOException {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    String html = (String) reportManager.runReport(reportFileName, "html", parameters, jrDataSource, output);
                    output.write(html.getBytes());
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                } finally {
                    output.close();
                }
            }
        };
        
        return streamingOutput;
    }

    private StreamingOutput exportToPDF(final String reportFileName, final Map<String, String> parameters, final JRDataSource jrDataSource)
        throws IOException {

        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                try {
                    reportManager.runReport(reportFileName, "pdf", parameters, jrDataSource, output);
                } catch (Exception e) {
                    throw new WebApplicationException(e);
                } finally {
                    output.close();
                }
            }
        };
        
        return streamingOutput;
    }
    
    private JRDataSource getReportBeansBeansJrDataSource(List<?> reportBeans) {
        return new JRBeanCollectionDataSource(reportBeans);
    }

}

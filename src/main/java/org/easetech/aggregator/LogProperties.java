

package org.easetech.aggregator;

import java.util.List;
import java.util.Set;

/**
 * A DTO capturing the names of the environments supported and the supported reports 
 *
 */
public class LogProperties {
    
    /**
     * Collection of Environment names
     */
    private Set<String> envNames;

    /**
     * Collection of Reports
     */
    private List<ReportName> reports;

    /**
     * @return the reports
     */
    public List<ReportName> getReports() {
        return reports;
    }

    /**
     * @param reports the reports to set
     */
    public void setReports(List<ReportName> reports) {
        this.reports = reports;
    }

    /**
     * @return the envNames
     */
    public Set<String> getEnvNames() {
        return envNames;
    }

    /**
     * @param envNames the envNames to set
     */
    public void setEnvNames(Set<String> envNames) {
        this.envNames = envNames;
    }

    /**
     * @return toString representation
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("LogProperties [envNames=");
        builder.append(envNames);
        builder.append(", reports=");
        builder.append(reports);
        builder.append("]");
        return builder.toString();
    }

}

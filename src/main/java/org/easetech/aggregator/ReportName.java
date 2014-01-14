/****************************************************************************************************************
*
*  Copyright (c) 2014 OCLC, Inc. All Rights Reserved.
*
*  OCLC proprietary information: the enclosed materials contain
*  proprietary information of OCLC, Inc. and shall not be disclosed in whole or in 
*  any part to any third party or used by any person for any purpose, without written
*  consent of OCLC, Inc.  Duplication of any portion of these materials shall include this notice.
*
******************************************************************************************************************/

package org.easetech.aggregator;

/**
 * DTO that holds the report specific information
 *
 */
public class ReportName {
    /**
     * Name of the report
     */
    private String name;
    
    /**
     * Description of the report
     */
    private String description;
    
    /**
     * Whether the report is selected or not
     */
    private boolean selected;
    

    /**
     * 
     * Construct a new ReportName
     */
    public ReportName() {
        super();
    }

    /**
     * 
     * Construct a new ReportName
     * @param name name of report
     * @param description desc of report
     * @param selected whether report is selected or not
     */
    public ReportName(String name, String description, boolean selected) {
        super();
        this.name = name;
        this.description = description;
        this.selected = selected;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the selected
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * @return
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ReportName [name=");
        builder.append(name);
        builder.append(", description=");
        builder.append(description);
        builder.append(", selected=");
        builder.append(selected);
        builder.append("]");
        return builder.toString();
    }
    
    

}

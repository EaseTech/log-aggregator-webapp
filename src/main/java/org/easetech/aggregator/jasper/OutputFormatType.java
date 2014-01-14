
package org.easetech.aggregator.jasper;

/**
 * Contains the possible report formats used for generating the reports.
 * 
 */
public final class OutputFormatType {

    /**
     * Construct a new OutputFormatType
     */
    private OutputFormatType() {
        // empty private constructor
    }

    /**
     * Export output to PDF
     */
    public static final int PDF = 1;
    /**
     * Export output to HTML
     */
    public static final int HTML = 2;
    /**
     * Export output to RTF
     */
    public static final int RTF = 3;
    /**
     * Export output to printer
     */
    public static final int PRINTER = 4;
    /**
     * Export output to mail with HTML content
     */
    public static final int HTML_MAIL = 5;
    /**
     * Export output to the Open Document Format (OASIS Open Document Format for Office Applications)
     */
    public static final int ODF = 6;
    /**
     * Export output to Microsoft Word (.docx) format
     */
    public static final int DOCX = 7;
    /**
     * Export output to Open Document Format for Office Applications specifications for spreadsheets
     */
    public static final int ODS = 8;
    /**
     * Export output to Microsoft Excel (.xslx) format
     */
    public static final int XLSX = 9;
    /**
     * Export output to CSV format
     */
    public static final int CSV = 10;
    /**
     * Export output to XML
     */
    public static final int XML = 11;
    /**
     * Export output to TEXT
     */
    public static final int TEXT = 12;
    /**
     * Export output to mail with plain text content.
     */
    public static final int TEXT_MAIL = 13;
}

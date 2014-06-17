

package org.easetech.aggregator;

import javax.servlet.http.HttpServlet;

/**
 * A simple Load on start up servlet that is responsible for scheduling a {@link LogsRetriever} task
 * as soon as the application is deployed.
 *
 */
public class LogDownloaderServlet extends HttpServlet {
    
    /** Serial Version ID */
    private static final long serialVersionUID = 1L;

    /**
     * Initialize the servlet task.
     * TODO: The name of the log file is hard coded. Make it an env property to fetch from remote location as well.
     */
    @Override
    public void init() {
        LogsRetriever retriever = new LogsRetriever("logProperties.xml");
        retriever.schedule();
    }

}

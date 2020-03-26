// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.app;

import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.RequestLog;
import org.slf4j.Logger;

/**
 Request log factory for configuring Jetty server request logs to log4j
 */
class AppRequestLogFactory {

    private Logger logger;

    AppRequestLogFactory(Logger logger) {
        this.logger = logger;
    }

    CustomRequestLog create() {
        RequestLog.Writer writer = requestEntry -> logger.info(requestEntry);
        return new CustomRequestLog(writer, CustomRequestLog.EXTENDED_NCSA_FORMAT);
    }
}



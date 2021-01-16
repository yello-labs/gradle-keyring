package org.yellolab

import org.gradle.api.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * TODO: Add examples here for using in both settings and project scope.
 *
 */

class KeyringPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(KeyringPlugin.class);

    @Override
    void apply(Object o) {

    }

    public static String getSecret(String host, String userName) {
        logger.info("Retrieving secret")


        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")
    }

    public static boolean setSecret(String host, String userName, String secret) {
        logger.info("Setting secret")

        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")
    }
}

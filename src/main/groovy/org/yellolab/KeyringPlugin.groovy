package org.yellolab

import org.gradle.api.Plugin
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yellolab.Keyring;

/**
 * TODO: Add examples here for using in both settings and project scope.
 *
 */

class KeyringPlugin implements Plugin {
    private static final Logger logger = LoggerFactory.getLogger(KeyringPlugin.class);

    @Override
    void apply(Object o) {
        //Use this one with the keyring::method in the build scripts
        def keyring = new KeyringPlugin();
    }

    //or use it with the imported class by using Class.method with these
    public static String getSecret(String host, String userName) {
        logger.info("Retrieving secret")

        Keyring.getSecret(host, userName)

        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")
    }

    public static boolean setSecret(String host, String userName, String secret) {
        logger.info("Setting secret")

        Keyring.setSecret(host, userName, secret)

        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")
    }

    public static boolean deleteSecret(String host, String userName) {
        logger.info("Setting secret")

        Keyring.deleteSecret(host, userName)

        logger.trace("If you need to debug your output, do it in trace.  Gradle does NOT log trace")
    }
}

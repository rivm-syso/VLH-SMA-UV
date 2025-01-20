package nl.rivm.util;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility for logging the properties contained in the jar manifest file META-INF/MANIFEST.MF of the jar that is running.
 * Extra entries to be included in the manifest file META-INF/MANIFEST.MF can be specified in the pom-element
 * configuration/archive/manifestEntries of the maven-assembly-plugin or the maven-jar-plugin.
 * Maven will insert these entries in the manifest file during the corresponding Maven phases.
 */
@Slf4j
public final class JarManifestProperties extends PropertiesLogger {

    private JarManifestProperties() {
    }

    private static final String MANIFEST_FILE_PATH = "META-INF/MANIFEST.MF";

    public static void logProperties() {
        logProperties(null);
    }

    public static void logProperties(String... keys) {
        log(MANIFEST_FILE_PATH, keys);
    }
}

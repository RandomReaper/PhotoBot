package org.pignat.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.pignat.photobot.Bot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Version {
    private static final Logger log = LogManager.getLogger();
    private static String version = "ERROR:NO_VERSION";

    static {
        init();
    }

    /**
     * Get the version of the library
     *
     * @return A String containing the version
     */
    static public String string() {
        return version;
    }

    /**
     * Initialize the version.
     */
    private static void init() {
        final String f = "/res/generated/version.txt";
        String v = null;
        try {
            InputStream in = Bot.class.getResourceAsStream(f);
            if (in == null) {
                log.error(String.format("resource '%s' not found, not using the .jar? version will be wrong", f));
                return;
            }
            BufferedReader b = new BufferedReader(new InputStreamReader(in));
            if (b.ready()) {
                v = b.readLine();
            }
            if (v.contains("dirty") || v.contains("-")) {
                log.warn(String.format("WARNING: using non-release version '%s'", v));
            }
            version = v;
        } catch (IOException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(String.format("Failed to parse : %s'", v), e);
        }
    }
}

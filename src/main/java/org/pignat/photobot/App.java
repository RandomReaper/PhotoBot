package org.pignat.photobot;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.pignat.utils.Version;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

public class App implements IFileCreatedListener {
    protected static final Logger log = LogManager.getLogger();
    protected Bot bot;
    protected WatchFileChange watch;

    public App() {
        Set<Long> ids = new HashSet<Long>();
        Set<String> dirs = new HashSet<String>();
        String token = null;
        try (InputStream input = new FileInputStream("photobot.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            token = prop.getProperty("token");
            String sids = prop.getProperty("ids");
            if (sids != null) {
                ids.addAll(Arrays.stream(sids.split(",")).mapToLong(Long::parseLong).boxed().collect(Collectors.toSet()));
            }
            String sdirs = prop.getProperty("dirs");
            if (sdirs != null) {
                dirs.addAll(Arrays.asList(sdirs.split(",")));
            }
            log.debug("token:" + token);
            if (token == null || token.isBlank()) {
                log.error("no token");
            }
            log.info("ids:" + ids);
            if (ids.isEmpty()) {
                log.warn("ids is empty");
            }
            log.info("dirs:" + dirs);
            if (dirs.isEmpty()) {
                log.warn("dirs is empty");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        bot = new Bot(token, ids);
        watch = new WatchFileChange(dirs, this);
    }

    public static String version() {
        return Version.string();
    }

    @Override
    public void created(Path file) {
        bot.sendMsg(file.toString());
        bot.sendImg(file);
    }

    public static void main(String args[]) {
        Configurator.setRootLevel(Level.DEBUG);
        log.info("Starting photobot - " + version());
        new App();
    }

}

package org.pignat.photobot;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Set;

public class WatchFileChange {
    protected static final Logger log = LogManager.getLogger();
    protected IFileCreatedListener listener;

    public WatchFileChange(Set<String> dirs, IFileCreatedListener l) {
        listener = l;
        WatchService watcher = null;
        try {
            watcher = FileSystems.getDefault().newWatchService();
            for (String dir : dirs) {
                Path p = Paths.get(dir);
                p.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);
            }
        } catch (IOException e) {
            log.error(e);
        }

        WatchService finalWatcher = watcher;
        new Thread() {
            public void run() {
                while (true) {
                    WatchKey k;
                    try {
                        k = watcher.take();
                    } catch (InterruptedException e) {
                        continue;
                    }
                    for (WatchEvent<?> event : k.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();

                        if (kind == StandardWatchEventKinds.OVERFLOW) {
                            continue;
                        } else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path dir = (Path) k.watchable();
                            WatchEvent<Path> ev = (WatchEvent<Path>) event;
                            Path fileName = dir.resolve(ev.context());
                            log.debug("file created : " + fileName);
                            listener.created(fileName);
                        }
                    }
                    boolean valid = k.reset();
                    if (!valid) {
                        break;
                    }
                }
            }
        }.start();
    }
}

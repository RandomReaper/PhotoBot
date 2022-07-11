package org.pignat.photobot;

import java.nio.file.Path;

public interface IFileCreatedListener {
    public void created(Path file);
}

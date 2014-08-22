package com.noths.ratel;

import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

class ProjectRoot {

    private static final Logger LOG = Logger.getLogger(ProjectRoot.class.getName());

    static ProjectRoot projectRoot() {
        String rootDirectory;
        try {
            rootDirectory = new File(ProjectRoot.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            LOG.log(Level.WARNING, "Unable to work out root directory", e);
            rootDirectory = "Unable to work out root directory " + e.getMessage();
        }

        return new ProjectRoot(rootDirectory);
    }

    private final String path;

    private ProjectRoot(final String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}

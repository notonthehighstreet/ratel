package com.noths.ratel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    static Server toServer(final String environment) {
        String hostName;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            LOG.log(Level.WARNING, "Unable to work out host name for localhost!", e);
            hostName = "Unable to work out host name " + e.getMessage();
        }
        return new Server(environment, hostName, ProjectRoot.projectRoot());
    }

    private Server(final String environmentName, final String hostname, final ProjectRoot projectRoot) {
        this.environmentName = environmentName;
        this.hostname = hostname;
        this.projectRoot = projectRoot;
    }

    @JsonProperty("environment_name")
    private final String environmentName;
    private final String hostname;
    @JsonProperty("project_root")
    private final ProjectRoot projectRoot;
    private final Stats stats = new Stats(new Mem());

    public String getEnvironmentName() {
        return environmentName;
    }

    public String getHostname() {
        return hostname;
    }

    public ProjectRoot getProjectRoot() {
        return projectRoot;
    }

    public Stats getStats() {
        return stats;
    }
}

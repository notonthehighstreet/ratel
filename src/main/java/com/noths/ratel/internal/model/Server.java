package com.noths.ratel.internal.model;

/*
 * #%L
 * Ratel Library
 * %%
 * Copyright (C) 2014 notonthehighstreet.com
 * %%
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * #L%
 */

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Honeybadger API class. Contains information about the server the code is running on.
 */
public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    public static Server toServer(final String environment) {
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

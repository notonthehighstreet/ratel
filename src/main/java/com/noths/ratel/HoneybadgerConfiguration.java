package com.noths.ratel;

import javax.annotation.CheckForNull;
import java.net.URL;
import java.util.Collection;

/**
 * Configuration that will be used when communicating with Honeybadger.
 */
public interface HoneybadgerConfiguration {

    /**
     * Unique key assigned to a project.
     * @return Unique key assigned to a project.
     */
    String getKey();

    /**
     * URL that should be used when communicating with Honeybadger.
     * @return URL that will be used to send error notifications.
     */
    URL getUrl();

    /**
     * Name of the application.
     * @return Name of the application.
     */
    String getName();

    /**
     * Version number of the application; may be null.
     * @return Version number of the application.
     */
    @CheckForNull
    String getVersion();

    /**
     * Environment that the application is running in, e.g. 'prod'.
     * @return Environment that the application is currently running in.
     */
    String getEnvironment();

    /**
     * Any exception classes that should NOT be sent to Honeybadger. This must be the exact class name and not super classes.
     * @return Exception classes that shouldn't be sent to Honeybadger.
     */
    Collection<String> getExclude();
}

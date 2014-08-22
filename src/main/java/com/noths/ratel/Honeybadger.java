package com.noths.ratel;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

/**
 * This class provides a method of communicating exceptions to <a href="https://honeybadger.io">Honeybadger</a>.
 */
public class Honeybadger {

    private static final Logger LOG = Logger.getLogger(Honeybadger.class.getName());
    private static final String API_VERSION = "1.3.0";

    private final HoneybadgerConfiguration configuration;
    private final Executor executor;
    private final Notifier notifier;
    private final HttpRequest request;

    /**
     * Construct a new instance with the given parameters.
     * @param configuration The configuration that will be used when communicating to Honeybadger.
     * @param executor Executor where the communication with Honeybadger will take place. This is used to avoid the scenario where responding back to the user is delayed while waiting
     *                 for the request to Honeybadger to timeout.
     * @param mapper Jackson mapper that will be used to turn objects into JSON.
     */
    public Honeybadger(final HoneybadgerConfiguration configuration, final Executor executor, final ObjectMapper mapper) {
        this(configuration, executor, new HttpRequest(mapper));
    }

    Honeybadger(final HoneybadgerConfiguration configuration, final Executor executor, final HttpRequest request) {
        this.configuration = configuration;
        this.executor = executor;
        this.request = request;
        this.notifier = new Notifier(configuration.getKey(), configuration.getName(), API_VERSION);
    }

    /**
     * Notify Honeybadger that an exception occurred.
     * @param url URL that was being called when the error occurred.
     * @param controller Controller that was called by the user (optional).
     * @param action Action that was called on the controller by the user (optional).
     * @param method HTTP method that was being used when the exception occurred  (optional).
     * @param userAgent User agent of the user that made the request  (optional).
     * @param remoteAddress Address of the user that made the request  (optional).
     * @param parameters Parameters that had been sent with the HTTP request.
     * @param t Exception that occurred.
     */
    public void notify(final String url, @Nullable final String controller, @Nullable final String action, @Nullable final String method, @Nullable final String userAgent,
                       @Nullable final String remoteAddress, final Map<String, String[]> parameters, final Throwable t) {

        if (ignoreException(t)) {
            return;
        }

        final Map<String, String> cgi = new HashMap<String, String>();

        if (method != null) {
            cgi.put("REQUEST_METHOD", method);
        }
        if (userAgent != null) {
            cgi.put("HTTP_USER_AGENT", userAgent);
        }
        if (remoteAddress != null) {
            cgi.put("REMOTE_ADDR", remoteAddress);
        }
        final String version = configuration.getVersion();
        if (version != null) {
            cgi.put("SERVER_SOFTWARE", configuration.getName() + "/" + version);
        }

        notifyHoneybadger(constructNotice(t, constructRequest(url, controller, action, parameters, cgi)));
    }

    private void notifyHoneybadger(final Map<String, ?> notice) {
        // Notify about an error off of the main thread to avoid delaying the response in case of timing out to external service
        executor.execute(new Runnable() {
            @Override
            public void run() {
                restCall(notice);
            }
        });
    }

    private void restCall(final Map<String, ?> notice) {
        try {

            final Map<String, String> headers = new HashMap<String, String>();
            headers.put("X-API-Key", configuration.getKey());
            headers.put("Content-Type", "application/json");

            final int response = request.call((HttpURLConnection) configuration.getUrl().openConnection(), "POST", headers, notice);

            if (response < 200 || response > 299) {
                LOG.log(Level.SEVERE, "Call to Honeybadger failed with code " + response);
            }
        } catch (final IOException e) {
            LOG.log(Level.SEVERE, "Failure occurred while trying to talk to Honeybadger", e);
        }
    }

    private boolean ignoreException(final Throwable t) {
        return configuration.getExclude().contains(t.getClass().getName());
    }

    private Map<String, Object> constructNotice(final Throwable e, final Request request) {
        final Map<String, Object> notice = new HashMap<String, Object>();
        notice.put("notifier", notifier);
        notice.put("error", Error.fromException(e));
        notice.put("server", Server.toServer(configuration.getEnvironment()));
        notice.put("request", request);
        return notice;
    }

    private Request constructRequest(final String url, @Nullable final String controller, @Nullable final String action, final Map<String, String[]> parameters,
                                     final Map<String, String> cgi) {

        final Request request = new Request();
        request.setUrl(url);
        request.setCgiData(cgi);
        if (controller != null) {
            request.setComponent(controller);
        }
        if (action != null) {
            request.setAction(action);
        }

        request.setParams(join(parameters));

        return request;
    }

    private <K, V> Map<K, String> join(final Map<K, V[]> map) {
        if (map == null) {
            return Collections.emptyMap();
        }

        final Map<K, String> joinedMap = new HashMap<K, String>();

        for (final Map.Entry<K, V[]> e : map.entrySet()) {
            final StringBuilder value = new StringBuilder(e.getValue().length * 16);

            Iterator<V> iterator = asList(e.getValue()).iterator();
            while (iterator.hasNext()) {
                value.append(iterator.next());
                if (iterator.hasNext()) {
                    value.append(",");
                }
            }

            joinedMap.put(e.getKey(), value.toString());
        }

        return joinedMap;
    }

}

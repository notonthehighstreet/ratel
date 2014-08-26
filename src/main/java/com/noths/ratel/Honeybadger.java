package com.noths.ratel;

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
     * Construct a new instance with the given parameters. Will use Java as the programming language.
     * @param configuration The configuration that will be used when communicating to Honeybadger.
     * @param executor Executor where the communication with Honeybadger will take place. This is used to avoid the scenario where responding back to the user is delayed while waiting
     *                 for the request to Honeybadger to timeout.
     * @param mapper Jackson mapper that will be used to turn objects into JSON.
     */
    public Honeybadger(final HoneybadgerConfiguration configuration, final Executor executor, final ObjectMapper mapper) {
        this(configuration, executor, new HttpRequest(mapper), "java");
    }

    /**
     * Construct a new instance with the given parameters.
     * @param configuration The configuration that will be used when communicating to Honeybadger.
     * @param executor Executor where the communication with Honeybadger will take place. This is used to avoid the scenario where responding back to the user is delayed while waiting
     *                 for the request to Honeybadger to timeout.
     * @param mapper Jackson mapper that will be used to turn objects into JSON.
     * @param language Programming language that the application is written in (java, scala, groovy, etc).
     */
    public Honeybadger(final HoneybadgerConfiguration configuration, final Executor executor, final ObjectMapper mapper, final String language) {
        this(configuration, executor, new HttpRequest(mapper), language);
    }

    Honeybadger(final HoneybadgerConfiguration configuration, final Executor executor, final HttpRequest request, final String language) {
        this.configuration = configuration;
        this.executor = executor;
        this.request = request;
        this.notifier = new Notifier(configuration.getKey(), configuration.getName(), API_VERSION, language);
    }

    /**
     * Notify Honeybadger that an exception occurred.
     * @param identifier Identifier for this exception such as the URL for a web request or correlation ID for a message.
     * @param t Exception that occurred.
     */
    public void notify(final String identifier, final Throwable t) {
        notify(identifier, null, null, null, null, null, Collections.<String, String[]>emptyMap(), t);
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
        notify(url, controller, action, method, userAgent, remoteAddress, parameters, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap(),
                Collections.<String, String>emptyMap(), t);
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
     * @param sessionDetails Session details associated with the HTTP request.
     * @param cookies Cookies associated with the HTTP request.
     * @param context Context of the exception.
     * @param t Exception that occurred.
     */
    public void notify(final String url, @Nullable final String controller, @Nullable final String action, @Nullable final String method, @Nullable final String userAgent,
                       @Nullable final String remoteAddress, final Map<String, String[]> parameters, final Map<String, String> sessionDetails, final Map<String, String> cookies,
                       final Map<String, String> context, final Throwable t) {

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
        if (!cookies.isEmpty()) {
            cgi.put("HTTP_COOKIE", toString(cookies));
        }

        notifyHoneybadger(constructNotice(t, constructRequest(url, controller, action, parameters, sessionDetails, context, cgi)));
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
        return configuration.getExcludeExceptions().contains(t.getClass().getName());
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
                                     final Map<String, String> sessionDetails, final Map<String, String> context, final Map<String, String> cgi) {

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
        request.setSession(sessionDetails);
        request.setContext(context);

        return request;
    }

    private String toString(final Map<String, String> map) {
        final StringBuilder sb = new StringBuilder(map.size() * 16);

        final Iterator<Map.Entry<String, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry<String, String> e = iterator.next();
            sb.append(e.getKey()).append("=").append(e.getValue());
            if (iterator.hasNext()) {
                sb.append("; ");
            }
        }

        return sb.toString();
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

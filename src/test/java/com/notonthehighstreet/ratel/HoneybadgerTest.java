package com.notonthehighstreet.ratel;

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

import com.notonthehighstreet.ratel.internal.model.Error;
import com.notonthehighstreet.ratel.internal.model.Notifier;
import com.notonthehighstreet.ratel.internal.model.Request;
import com.notonthehighstreet.ratel.internal.model.Server;
import com.notonthehighstreet.ratel.internal.utility.HttpRequest;
import org.hamcrest.CustomTypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class HoneybadgerTest {

    private Honeybadger subject;
    private Executor executor;
    private String systemEnvironment;
    private String name;
    private URL url;
    private String key;
    private HttpRequest httpRequest;
    private String language;

    @Before
    public void setup() throws MalformedURLException {
        executor = mock(Executor.class);

        final DummyConfiguration configuration = new DummyConfiguration();

        systemEnvironment = "expected environment";
        name = "expected name";
        url = new URL("http", "localhost", 80, "index.html");
        key = "expected key";
        language = "language";

        configuration.setEnvironment(systemEnvironment);
        configuration.setName(name);
        configuration.setUrl(url);
        configuration.setKey(key);
        configuration.setExclude(Collections.singletonList(UnsupportedOperationException.class.getName()));

        httpRequest = mock(HttpRequest.class);
        subject = new Honeybadger(configuration, executor, httpRequest, language);
    }

    @Test
    public void shouldDoNothingIfExceptionExcluded() {
        subject.notify("", new UnsupportedOperationException());

        verifyZeroInteractions(executor, httpRequest);
    }

    @Test
    public void shouldSendRequestToHoneyBadgerIfExceptionHasOccurred() throws Exception {

        final String message = "exception message";

        final String controller = "name of the controller";
        final String action = "name of the action";
        final String userAgent = "user agent";
        final String remoteAddress = "address of the browser";
        final String method = "method of HTTP request";
        final String requestUri = "URL that was requested";
        final Map<String, String> parameters = Collections.singletonMap("parameter", "value");

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(final InvocationOnMock invocation) throws Throwable {
                ((Runnable) invocation.getArguments()[0]).run();
                return null;
            }
        }).when(executor).execute(any(Runnable.class));

        subject.notify(requestUri,
                controller,
                action,
                method,
                userAgent,
                remoteAddress,
                toArray(parameters),
                new IllegalArgumentException(message));

        final String description = String.format("HttpEntity with headers (contentType=application/json, X-API-Key=%s)" +
                        " and body (controller=%s, action=%s, userAgent=%s, remoteAddress=%s, requestUri=%s," +
                        " parameters=%s, message=%s, method=%s, environment=%s, name=%s)",
                key, controller, action, userAgent, remoteAddress, requestUri, parameters, message, method, systemEnvironment, name);

        CustomTypeSafeMatcher<HttpURLConnection> matchesHttpUrlConnection =
                new CustomTypeSafeMatcher<HttpURLConnection>("HttpURLConnection with a URL of " + url) {
            @Override
            protected boolean matchesSafely(final HttpURLConnection item) {
                return item.getURL() == url;
            }
        };

        CustomTypeSafeMatcher<Object> matchesBodyMap = new CustomTypeSafeMatcher<Object>(description) {
            @Override
            protected boolean matchesSafely(final Object item) {
                if (!(item instanceof Map)) {
                    return false;
                }
                final Map<?, ?> body = (Map<?, ?>) item;
                return bodyMatches(body, requestUri, parameters, method, controller, action, userAgent, remoteAddress);
            }
        };

        verify(httpRequest).call(argThat(matchesHttpUrlConnection), eq("POST"), eq(expectedHeaders(key)), argThat(matchesBodyMap));
    }

    private Map<String, String[]> toArray(final Map<String, String> map) {
        final Map<String, String[]> ret = new HashMap<String, String[]>();

        for (final Map.Entry<String, String> e : map.entrySet()) {
            ret.put(e.getKey(), new String[]{e.getValue()});
        }

        return ret;
    }

    private boolean bodyMatches(final Object body, final String requestUri, final Map<String, String> parameters, final String method, final String controller,
                                final String action, final String userAgent, final String remoteAddress) {
        if (!(body instanceof Map)) {
            return false;
        }

        final Map notice = (Map) body;

        final Server server = (Server) notice.get("server");
        final Notifier notifier = (Notifier) notice.get("notifier");
        final Error error = (Error) notice.get("error");
        final Request request = (Request) notice.get("request");

        if (server == null || notifier == null || error == null || request == null) {
            return false;
        }

        if (anyNull(server.getHostname(), server.getProjectRoot().getPath()) || !equals(server.getEnvironmentName(), systemEnvironment)) {
            return false;
        }

        if (anyNull(notifier.getName(), notifier.getVersion()) || !equals(notifier.getName(), name) || !equals(notifier.getLanguage(), language)) {
            return false;
        }

        if (!request.getUrl().endsWith(requestUri) || !parameters.equals(request.getParams())) {
            return false;
        }

        if (anyNull(error.getMessage(), error.getClazz()) || error.getBacktrace().isEmpty()) {
            return false;
        }

        if (!request.getCgiData().get("REQUEST_METHOD").equals(method) || !request.getCgiData().get("HTTP_USER_AGENT").equals(userAgent)
                || !request.getCgiData().get("REMOTE_ADDR").equals(remoteAddress)) {
            return false;
        }

        if (!request.getAction().equals(action) || !request.getComponent().equals(controller)) {
            return false;
        }

        return true;
    }

    private Map<String, String> expectedHeaders(final String key) {
        final HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("X-API-Key", key);
        headers.put("Content-Type", "application/json");
        return headers;
    }

    private boolean anyNull(final String... os) {
        for (final String o : os) {
            if (o == null || o.trim().equals("")) {
                return true;
            }
        }
        return false;
    }

    private boolean equals(final Object one, final Object two) {
        if (one == two) {
            return true;
        }
        if (one == null || two == null) {
            return false;
        }
        return one.equals(two);
    }

    private static final class DummyConfiguration implements HoneybadgerConfiguration {

        private String key;
        private URL url;
        private String name;
        private String environment;
        private List<String> exclude;

        @Override
        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }

        @Override
        public URL getUrl() {
            return url;
        }

        public void setUrl(final URL url) {
            this.url = url;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getVersion() {
            return null;
        }

        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public String getEnvironment() {
            return environment;
        }

        public void setEnvironment(final String environment) {
            this.environment = environment;
        }

        @Override
        public Collection<String> getExcludeExceptions() {
            return exclude;
        }

        public void setExclude(final List<String> exclude) {
            this.exclude = exclude;
        }

    }

}
package com.notonthehighstreet.ratel.internal.utility;

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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utility class used to split out the calling of Honeybadger from creating the body. This class is required as {@linkplain java.net.URL URL} is final and so cannot be mocked.
 */
public class HttpRequest {

    private static final int TEN_SECONDS = (int) TimeUnit.SECONDS.toMillis(10);
    private final ObjectMapper mapper;

    public HttpRequest(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public int call(final HttpURLConnection connection, final String method, final Map<String, String> headers, final Object body) throws IOException {

        connection.setConnectTimeout(TEN_SECONDS);
        connection.setReadTimeout(TEN_SECONDS);

        connection.setRequestMethod(method);

        for (final Map.Entry<String, String> header : headers.entrySet()) {
            connection.setRequestProperty(header.getKey(), header.getValue());
        }

        connection.setDoOutput(true);

        mapper.writeValue(connection.getOutputStream(), body);

        return connection.getResponseCode();

    }
}

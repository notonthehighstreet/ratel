package com.noths.ratel.internal.utility;

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
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.net.HttpURLConnection;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpRequestTest {

    private HttpRequest subject;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        mapper = mock(ObjectMapper.class);
        subject = new HttpRequest(mapper);
    }

    @Test
    public void callShouldMakeHTTPRequest() throws Exception {
        final String method = "GET";
        final String parameterName = "name";
        final String parameterValue = "value";
        final Object body = new Object();

        final int expected = 123123;

        final HttpURLConnection connection = mock(HttpURLConnection.class);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(connection.getOutputStream()).thenReturn(outputStream);
        when(connection.getResponseCode()).thenReturn(expected);

        final int actual = subject.call(connection, method, Collections.singletonMap(parameterName, parameterValue), body);

        assertEquals(expected, actual);

        verify(mapper).writeValue(same(outputStream), same(body));
        verify(connection).setRequestMethod(eq(method));
        verify(connection).setReadTimeout(anyInt());
        verify(connection).setConnectTimeout(anyInt());
        verify(connection).setDoOutput(eq(true));
        verify(connection).setRequestProperty(eq(parameterName), eq(parameterValue));
    }
}

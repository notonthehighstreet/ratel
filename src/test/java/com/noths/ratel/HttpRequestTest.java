package com.noths.ratel;

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
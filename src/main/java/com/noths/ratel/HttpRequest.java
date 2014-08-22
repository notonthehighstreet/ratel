package com.noths.ratel;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class HttpRequest {

    private static final int TEN_SECONDS = (int) TimeUnit.SECONDS.toMillis(10);
    private final ObjectMapper mapper;

    HttpRequest(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    int call(final HttpURLConnection connection, final String method, final Map<String, String> headers, final Object body) throws IOException {

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

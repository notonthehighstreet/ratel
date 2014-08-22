package com.noths.ratel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

class Error {

    static Error fromException(final Throwable e) {
        return new Error(e.getClass().getName(), e.getMessage(), unwrapStackTrace(e));
    }

    private static List<Backtrace> unwrapStackTrace(final Throwable e) {
        // As Honeybadger only supports a single error, we need to massage the wrapped exceptions into a single stack trace
        final List<Backtrace> backtraces = new ArrayList<Backtrace>();

        Throwable cause = e;

        while (cause != null) {
            if (!backtraces.isEmpty()) {
                backtraces.add(Backtrace.markerBacktrace(cause));
            }

            for (final StackTraceElement element : e.getStackTrace()) {
                backtraces.add(Backtrace.fromStackTrace(element));
            }

            cause = cause.getCause();
        }

        return backtraces;
    }

    @JsonProperty("class")
    private final String clazz;
    private final String message;
    private final List<Backtrace> backtrace;

    private Error(final String clazz, final String message, final List<Backtrace> backtrace) {
        this.clazz = clazz;
        this.message = message.length() > 1024 ? message.substring(0, 1024) : message;
        this.backtrace = backtrace;
    }

    public String getClazz() {
        return clazz;
    }

    public List<Backtrace> getBacktrace() {
        return backtrace;
    }

    public String getMessage() {
        return message;
    }
}

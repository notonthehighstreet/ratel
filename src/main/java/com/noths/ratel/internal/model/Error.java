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

import java.util.ArrayList;
import java.util.List;

public class Error {

    public static Error fromException(final Throwable e) {
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
        this.message = message != null && message.length() > 1024 ? message.substring(0, 1024) : message;
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

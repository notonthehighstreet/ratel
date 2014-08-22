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

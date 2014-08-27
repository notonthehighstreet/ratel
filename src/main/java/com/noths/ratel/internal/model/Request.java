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

import java.util.Map;

/**
 * Honeybadger API class. Contains information about a single HTTP request.
 */
public class Request {
    private String url;
    private Map<String, String> params;
    @JsonProperty("cgi_data")
    private Map<String, String> cgiData;
    private Map<String, String> session;
    private Map<String, String> context;
    private String component;
    private String action;

    public String getUrl() {
        return url;
    }

    public void setUrl(final String url) {
        this.url = url;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(final Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getCgiData() {
        return cgiData;
    }

    public void setCgiData(final Map<String, String> cgiData) {
        this.cgiData = cgiData;
    }

    public Map<String, String> getSession() {
        return session;
    }

    public void setSession(final Map<String, String> session) {
        this.session = session;
    }

    public Map<String, String> getContext() {
        return context;
    }

    public void setContext(final Map<String, String> context) {
        this.context = context;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(final String component) {
        this.component = component;
    }

    public String getAction() {
        return action;
    }

    public void setAction(final String action) {
        this.action = action;
    }
}

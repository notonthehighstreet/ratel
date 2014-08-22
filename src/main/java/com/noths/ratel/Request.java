package com.noths.ratel;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

class Request {
    private String url;
    private Map<String, String> params;
    @JsonProperty("cgi_data")
    private Map<String, String> cgiData;
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

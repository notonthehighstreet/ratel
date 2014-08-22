package com.noths.ratel;

import com.fasterxml.jackson.annotation.JsonProperty;

class Notifier {

    @JsonProperty("api_key")
    private final String apiKey;
    private final String name;
    private final String version;
    private final String language;

    Notifier(final String apiKey, final String name, final String version) {
        this.apiKey = apiKey;
        this.name = name;
        this.version = version;
        this.language = "java";
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getLanguage() {
        return language;
    }
}

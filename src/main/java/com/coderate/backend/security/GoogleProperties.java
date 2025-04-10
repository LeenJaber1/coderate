package com.coderate.backend.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
public class GoogleProperties {
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    // Getters and setters
    public String getId() {
        return clientId;
    }

    public void setId(String id) {
        this.clientId = id;
    }

    public String getSecret() {
        return clientSecret;
    }

    public void setSecret(String secret) {
        this.clientSecret = secret;
    }
}

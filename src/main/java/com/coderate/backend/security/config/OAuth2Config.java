package com.coderate.backend.security.config;

import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.stereotype.Component;

@Component
public class OAuth2Config {
    private final OAuth2ClientProperties oAuth2ClientProperties;

    public OAuth2Config(OAuth2ClientProperties oAuth2ClientProperties) {
        this.oAuth2ClientProperties = oAuth2ClientProperties;
    }

    public ClientRegistration getClientRegistration(String registrationId) {
        OAuth2ClientProperties.Registration registration =
                oAuth2ClientProperties.getRegistration().get(registrationId);

        OAuth2ClientProperties.Provider provider =
                oAuth2ClientProperties.getProvider().get(registrationId);

        if (registration == null || provider == null) {
            throw new IllegalArgumentException("Unknown client registration: " + registrationId);
        }
        return ClientRegistration.withRegistrationId(registrationId)
                .clientId(registration.getClientId())
                .clientSecret(registration.getClientSecret())
                .authorizationGrantType(new AuthorizationGrantType(registration.getAuthorizationGrantType()))
                .redirectUri(registration.getRedirectUri())
                .scope(registration.getScope())
                .authorizationUri(provider.getAuthorizationUri())
                .tokenUri(provider.getTokenUri())
                .userInfoUri(provider.getUserInfoUri())
                .userNameAttributeName(provider.getUserNameAttribute())
                .clientName(registration.getClientName())
                .build();
    }
}

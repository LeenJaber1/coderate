package com.coderate.backend.security.custom;

import com.coderate.backend.model.OAuth2Refresh;
import com.coderate.backend.repository.OAuth2RefreshTokenRepository;
import com.coderate.backend.security.config.OAuth2Config;
import com.coderate.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class CustomOAuth2ClientService implements OAuth2AuthorizedClientService {
    @Autowired
    private OAuth2RefreshTokenRepository tokenRepository;
    @Autowired
    private OAuth2Config config;
    @Autowired
    private UserService userService;

    @Override
    public <T extends OAuth2AuthorizedClient> T loadAuthorizedClient(String clientRegistrationId, String principalName) {
        OAuth2Refresh token = tokenRepository.findByUserIdAndRegistrationId(principalName, clientRegistrationId).orElse(null);
        if (token == null) {
            return null;
        }
        OAuth2AuthorizedClient authorizedClient = toOAuth2AuthorizedClient(token);
        return (T) authorizedClient;

    }


    @Override
    public void saveAuthorizedClient(OAuth2AuthorizedClient authorizedClient, Authentication principal) {
        if (tokenRepository.findByUserIdAndRegistrationId(principal.getName(), authorizedClient.getClientRegistration().getRegistrationId()).isPresent()) {
            return;
        }
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            OAuth2User oauthUser = oauthToken.getPrincipal();

            // Get user info from OAuth2User
            String email = oauthUser.getAttribute("email");
            String name = oauthUser.getAttribute("name");


            // Check if token already exists
            OAuth2Refresh token = tokenRepository.findByUserIdAndRegistrationId(principal.getName(), authorizedClient.getClientRegistration().getRegistrationId()).orElse(null);
            if (token != null) {
                token.setAccessToken(authorizedClient.getAccessToken().getTokenValue());
                if (authorizedClient.getRefreshToken() != null) {
                    token.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
                }
                token.setAccessTokenExpiresAt(authorizedClient.getAccessToken().getExpiresAt());
                tokenRepository.save(token);
            } else {
                token = new OAuth2Refresh();
                token.setUserId(principal.getName());
                token.setRegistrationId(authorizedClient.getClientRegistration().getRegistrationId());
                token.setAccessToken(authorizedClient.getAccessToken().getTokenValue());

                if (authorizedClient.getRefreshToken() != null) {
                    token.setRefreshToken(authorizedClient.getRefreshToken().getTokenValue());
                    token.setAccessTokenExpiresAt(authorizedClient.getAccessToken().getExpiresAt());
                    token.setCreatedAt(Instant.now());
                    token.setEmail(email);
                    token.setName(name);
                    tokenRepository.save(token);
                    userService.createUser(name, email, name.replaceAll("\\s", ""));
                }
            }
        }
    }

    @Override
    public void removeAuthorizedClient(String clientRegistrationId, String principalName) {
        tokenRepository.deleteByUserIdAndRegistrationId(principalName, clientRegistrationId);
    }

    private OAuth2AuthorizedClient toOAuth2AuthorizedClient(OAuth2Refresh token) {
        // Get the ClientRegistration from properties
        ClientRegistration clientRegistration =
                config.getClientRegistration(token.getRegistrationId());

        OAuth2AccessToken accessToken = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                token.getAccessToken(),
                token.getCreatedAt(),
                token.getAccessTokenExpiresAt());

        OAuth2RefreshToken refreshToken = token.getRefreshToken() != null ?
                new OAuth2RefreshToken(token.getRefreshToken(), token.getCreatedAt()) : null;

        return new OAuth2AuthorizedClient(
                clientRegistration,
                token.getUserId(),
                accessToken,
                refreshToken);
    }
}

package com.coderate.backend.util;

import com.coderate.backend.model.OAuth2Refresh;
import com.coderate.backend.repository.OAuth2RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class GoogleTokenService {
    private OAuth2RefreshTokenRepository auth2RefreshTokenRepository;

    public GoogleTokenService(OAuth2RefreshTokenRepository auth2RefreshTokenRepository) {
        this.auth2RefreshTokenRepository = auth2RefreshTokenRepository;
    }

    public String getRefreshToken(String email) throws Exception {
        return auth2RefreshTokenRepository.findByEmail(email).orElseThrow(() -> new Exception("no refresh token found, log in again")).getRefreshToken();
    }

    public void setAccessToken(String email, String accessToken) throws Exception {
        OAuth2Refresh oAuth2Refresh = auth2RefreshTokenRepository.findByEmail(email).orElse(null);
        if(oAuth2Refresh != null){
            Instant now = Instant.now();
            Instant expiryTime = now.plus(1, ChronoUnit.HOURS);
            oAuth2Refresh.setAccessToken(accessToken);
            oAuth2Refresh.setAccessTokenExpiresAt(expiryTime);
            oAuth2Refresh.setCreatedAt(now);
            auth2RefreshTokenRepository.save(oAuth2Refresh);
        }
        else{
            throw new Exception("No Token found, Login again");
        }
    }
}

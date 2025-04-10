package com.coderate.backend.controller;

import com.auth0.jwt.JWT;
import com.coderate.backend.enums.AuthType;
import com.coderate.backend.security.GoogleProperties;
import com.coderate.backend.util.GoogleTokenService;
import com.coderate.backend.util.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private JWTService jwtService;

    private GoogleProperties googleProperties;

    private GoogleTokenService googleTokenService;

    public AuthController(JWTService jwtService, GoogleProperties googleProperties, GoogleTokenService googleTokenService) {
        this.jwtService = jwtService;
        this.googleProperties = googleProperties;
        this.googleTokenService = googleTokenService;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken = getRefreshTokenValue(request, "refreshToken");

        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            AuthType authType = jwtService.authType(refreshToken);
            if(authType == AuthType.NORMAL){
                String username = JWT.decode(refreshToken).getSubject();
                String newAccessToken = jwtService.getNewAccessToken(request, username);
                jwtService.deleteRefreshToken(refreshToken);
                Cookie accessTokenCookie = createCookie("accessToken", newAccessToken, 7 * 24 * 60 * 60);
                response.addCookie(accessTokenCookie);
            }
            else if(authType == AuthType.OAUTH){
                try {
                    String email = JWT.decode(refreshToken).getSubject();
                    String googleRefreshToken = googleTokenService.getRefreshToken(email);
                    String googleAccessToken = getNewGoogleAccessToken(googleRefreshToken);
                    googleTokenService.setAccessToken(email, googleAccessToken);
                }
                catch (Exception e){
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Expired Refresh Token");
                }

            }
            return ResponseEntity.ok("ok");
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Expired Refresh Token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        response.addCookie(createDeletedCookie("accessToken"));
        response.addCookie(createDeletedCookie("refreshToken"));

        String refreshToken = getRefreshTokenValue(request, "refreshToken");
        if (refreshToken != null) {
            jwtService.deleteRefreshToken(refreshToken);
        }
        return ResponseEntity.ok("Logged out successfully");
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }

    private Cookie createDeletedCookie(String name) {
        return createCookie(name, null, 0);
    }

    private String getRefreshTokenValue(HttpServletRequest request, @NonNull String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public String getNewGoogleAccessToken(String refreshToken) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        System.out.println("Using client_id: " + googleProperties.getId());
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", googleProperties.getId());
        body.add("client_secret", googleProperties.getSecret());
        body.add("refresh_token", refreshToken);
        body.add("grant_type", "refresh_token");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            return (String) response.getBody().get("access_token");
        }

        throw new RuntimeException("Failed to get access token: " + response.getStatusCode());
    }
}

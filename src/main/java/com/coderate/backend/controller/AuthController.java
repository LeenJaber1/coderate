package com.coderate.backend.controller;

import com.auth0.jwt.JWT;
import com.coderate.backend.enums.AuthType;
import com.coderate.backend.model.User;
import com.coderate.backend.service.UserService;
import com.coderate.backend.util.JWTService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private JWTService jwtService;


    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    private UserService userService;


    public AuthController(JWTService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @PostMapping("/google")
    public ResponseEntity<?> exchangeCode(
            @RequestBody Map<String, String> body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            String code = body.get("code");
            if (code == null || code.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Authorization code missing"));
            }

            // Exchange code for tokens
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("code", code);
            params.add("client_id", clientId);
            params.add("client_secret", clientSecret);
            params.add("redirect_uri", redirectUri);
            params.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(params, headers);
            ResponseEntity<Map> tokenResponse = restTemplate.postForEntity("https://oauth2.googleapis.com/token", tokenRequest, Map.class);

            if (!tokenResponse.getStatusCode().is2xxSuccessful()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Failed to get tokens from Google"));
            }

            String idToken = (String) tokenResponse.getBody().get("id_token");
            GoogleIdToken.Payload payload = verifyIdToken(idToken);

            String email = payload.getEmail();
            String name = (String) payload.get("name");

            // Create or fetch user
            this.userService.createUser(name , email , email);

            // Generate your own app tokens
            String accessToken = jwtService.getNewAccessToken(request, email);
            String refreshToken = jwtService.getNewRefreshToken(request, email, AuthType.OAUTH);

            // Set tokens in secure HttpOnly cookies
            response.addCookie(createCookie("accessToken", accessToken, 7 * 24 * 60 * 60));
            response.addCookie(createCookie("refreshToken", refreshToken, 30 * 24 * 60 * 60));

            return ResponseEntity.ok(Map.of("message", "Login successful"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken = getRefreshTokenValue(request, "refreshToken");

        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            String username = JWT.decode(refreshToken).getSubject();
            String newAccessToken = jwtService.getNewAccessToken(request, username);
            jwtService.deleteRefreshToken(refreshToken);
            Cookie accessTokenCookie = createCookie("accessToken", newAccessToken, 7 * 24 * 60 * 60);
            response.addCookie(accessTokenCookie);
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

    private GoogleIdToken.Payload verifyIdToken(String idTokenString) throws Exception {
        NetHttpTransport transport = new NetHttpTransport();
        GsonFactory jsonFactory = new GsonFactory();

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(transport, jsonFactory)
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new Exception("Invalid ID token");
        }
    }

}

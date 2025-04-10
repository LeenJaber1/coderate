package com.coderate.backend.security.custom;

import com.coderate.backend.enums.AuthType;
import com.coderate.backend.model.User;
import com.coderate.backend.util.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private final JWTService jwtService;

    public CustomOAuth2SuccessHandler(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        Map<String, Object> attributes = oauthToken.getPrincipal().getAttributes();
        String email = (String) attributes.get("email");

        String accessToken = jwtService.getNewAccessToken(request, email);
        String refreshToken = jwtService.getNewRefreshToken(request, email, AuthType.OAUTH);
        response.addCookie(createCookie("accessToken", accessToken, 7 * 24 * 60 * 60));
        response.addCookie(createCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60));
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh-Token", refreshToken);
        response.setContentType("application/json");
        //remove after frontend
        response.sendRedirect("/user/");
    }


    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}

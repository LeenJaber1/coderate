package com.coderate.backend.security.custom;

import com.coderate.backend.model.User;
import com.coderate.backend.util.JWTService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class CustomOnSuccessHandler implements AuthenticationSuccessHandler {
    @Autowired
    private JWTService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType(APPLICATION_JSON_VALUE);
        String accessToken = jwtService.getNewAccessToken(request , ((User) authentication.getPrincipal()).getUsername());
        String refreshToken = jwtService.getNewRefreshToken(request , ((User) authentication.getPrincipal()).getUsername());
        response.addCookie(createCookie("accessToken" , accessToken ,  7 * 24 * 60 * 60));
        response.addCookie(createCookie("refreshToken" , refreshToken ,  7 * 24 * 60 * 60));
        response.setHeader("Authorization", "Bearer " + accessToken);
        response.setHeader("Refresh-Token", refreshToken);
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

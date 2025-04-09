package com.coderate.backend.controller;

import com.auth0.jwt.JWT;
import com.coderate.backend.util.JWTService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private JWTService jwtService;

    public AuthController(JWTService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refreshToken = getCookieValue(request, "refreshToken");

        if (refreshToken != null && jwtService.validateRefreshToken(refreshToken)) {
            String username = JWT.decode(refreshToken).getSubject();
            String newAccessToken = jwtService.getNewAccessToken(request, username);

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

        String refreshToken = getCookieValue(request, "refreshToken");
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

    private String getCookieValue(HttpServletRequest request, @NonNull String cookieName) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }


}

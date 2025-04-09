package com.coderate.backend.security.custom;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.coderate.backend.model.User;
import com.coderate.backend.service.UserService;
import com.coderate.backend.util.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class CustomJwtFilter extends GenericFilterBean {
    private JWTService jwtService;

    private UserService userService;

    public CustomJwtFilter(JWTService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        if (request.getServletPath().equals("/login") || request.getServletPath().startsWith("/register")) {
            filterChain.doFilter(request, response);
        } else {
            String accessToken = getAccessToken(request);
            if (accessToken != null) {
                DecodedJWT decodedJWT = jwtService.getDecodedJWT(accessToken);
                String username = decodedJWT.getSubject();
                try {
                    User user = (User) userService.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    return;
                }
            }
            filterChain.doFilter(request, response);
        }
    }

    private String getAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("accessToken".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}

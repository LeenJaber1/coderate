package com.coderate.backend.util;

import jakarta.servlet.http.Cookie;

public class CookieCreation {
    public static Cookie createCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(false);
        cookie.setMaxAge(60 * 60);
        return cookie;
    }
}

package com.coderate.backend.model;

import com.coderate.backend.enums.AuthType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "refresh_tokens")
public class RefreshToken {
    @Id
    private String id;
    private String username;
    private AuthType loginType;

    public RefreshToken() {
    }

    public RefreshToken(String username, AuthType authType) {
        this.username = username;
        this.loginType = authType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public AuthType getLoginType() {
        return loginType;
    }

    public void setLoginType(AuthType loginType) {
        this.loginType = loginType;
    }
}


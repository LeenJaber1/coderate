package com.coderate.backend.model;

import com.coderate.backend.enums.Authorities;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Document(collection = "users")
public class User implements UserDetails {
    @Id
    private String id;
    private String displayName;
    private String username;
    private String password;
    private String email;
    private Set<Authorities> authorities;
    private Set<String> projectsId;

    public User() {
    }

    public User(String displayName, String username, String password, String email) {
        this.displayName = displayName;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = new HashSet<>();
    }

    public User(String displayName, String username, String email) {
        this.displayName = displayName;
        this.username = username;
        this.email = email;
        this.authorities = new HashSet<>();
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities
                .stream()
                .map(authority -> new SimpleGrantedAuthority(authority.name()))
                .collect(Collectors.toSet());
    }

    public void setAuthorities(Set<Authorities> authorities) {
        this.authorities = authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getProjects() {
        return projectsId;
    }

    public void setProjects(Set<String> projects) {
        this.projectsId = projects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id) && Objects.equals(displayName, user.displayName) && Objects.equals(username, user.username) && Objects.equals(password, user.password) && Objects.equals(email, user.email) && Objects.equals(authorities, user.authorities) && Objects.equals(projectsId, user.projectsId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, displayName, username, password, email, authorities, projectsId);
    }
}

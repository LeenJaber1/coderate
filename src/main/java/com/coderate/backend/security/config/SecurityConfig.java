package com.coderate.backend.security.config;

import com.coderate.backend.security.custom.CustomAuthorizationRequestResolver;
import com.coderate.backend.security.custom.CustomJwtFilter;
import com.coderate.backend.security.custom.CustomOAuth2ClientService;
import com.coderate.backend.security.custom.CustomOnSuccessHandler;
import com.coderate.backend.service.UserService;
import com.coderate.backend.util.JWTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private CustomOAuth2ClientService customOAuth2ClientService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private UserService userService;

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public AuthenticationManager customAuthenticationManager() {
        return new ProviderManager(Collections.singletonList(authenticationProvider()));
    }

//    @Bean
//    public CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(Arrays.asList("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
//        configuration.addAllowedHeader("*");
//        configuration.setExposedHeaders(List.of("Content-Range"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        UsernamePasswordAuthenticationFilter customUsernamePasswordAuthenticationFilter = new UsernamePasswordAuthenticationFilter(customAuthenticationManager());
        customUsernamePasswordAuthenticationFilter.setAuthenticationSuccessHandler(new CustomOnSuccessHandler());
        CustomJwtFilter customJwtFilter = new CustomJwtFilter();
        OAuth2AuthorizationRequestResolver customResolver =
                new CustomAuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/register/**", "/login").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization ->
                                authorization.authorizationRequestResolver(customResolver))
                        .authorizedClientService(customOAuth2ClientService))
                .formLogin(Customizer.withDefaults())
                .addFilterBefore(customUsernamePasswordAuthenticationFilter, SecurityContextHolderAwareRequestFilter.class)
                .addFilterBefore(customJwtFilter, customUsernamePasswordAuthenticationFilter.getClass())
                .build();
    }

}


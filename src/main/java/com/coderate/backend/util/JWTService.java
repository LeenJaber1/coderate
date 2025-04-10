package com.coderate.backend.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.coderate.backend.enums.AuthType;
import com.coderate.backend.model.RefreshToken;
import com.coderate.backend.repository.RefreshTokenRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class JWTService {
    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.access.secret}")
    private String accessSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshSecret;

    @Value("${jwt.refresh.time}")
    private long refreshTime;

    @Value("${jwt.access.time}")
    private long accessTime;

    private Algorithm jwtAccessAlgorithm;
    private Algorithm jwtRefreshAlgorithm;

    public JWTService() {

    }

    @PostConstruct
    public void init() {
        jwtAccessAlgorithm = Algorithm.HMAC256(accessSecret);
        jwtRefreshAlgorithm = Algorithm.HMAC256(refreshSecret);
    }

    public String getNewAccessToken(HttpServletRequest request, String username) {
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + accessTime))
                .withIssuer(request.getRequestURI().toString())
                .sign(jwtAccessAlgorithm);
    }

    public String getNewRefreshToken(HttpServletRequest request, String username, AuthType authType) {
        RefreshToken refreshToken = new RefreshToken(username, authType);
        refreshTokenRepository.save(refreshToken);
        return JWT.create()
                .withSubject(username)
                .withExpiresAt(new Date(System.currentTimeMillis() + refreshTime))
                .withIssuer(request.getRequestURI().toString())
                .withClaim("id", refreshToken.getId())
                .sign(jwtRefreshAlgorithm);

    }

    public void deleteRefreshToken(String token) {
        try {
            JWT.require(jwtRefreshAlgorithm).build().verify(token);
            String id = JWT.decode(token).getClaim("id").asString();
            refreshTokenRepository.deleteById(id);
        } catch (Exception e) {
            e.getMessage();
        }

    }

    public boolean validateRefreshToken(String token) {
        try {
            JWT.require(jwtRefreshAlgorithm).build().verify(token);
            String id = JWT.decode(token).getClaim("id").asString();
            return refreshTokenRepository.findById(id).isPresent();
        } catch (Exception e) {
            return false;
        }
    }

    public DecodedJWT getDecodedJWT(String token) {
        JWTVerifier verifier = JWT.require(jwtAccessAlgorithm).build();
        return verifier.verify(token);
    }

    public AuthType authType(String refreshToken){
        try {
            JWT.require(jwtRefreshAlgorithm).build().verify(refreshToken);
            String id = JWT.decode(refreshToken).getClaim("id").asString();
            RefreshToken refreshToken1 = refreshTokenRepository.findById(id).orElse(null);
            if(refreshToken1 != null){
                return refreshToken1.getLoginType();
            }
            return null;
        }
        catch (Exception e){
            return null;
        }
    }


}

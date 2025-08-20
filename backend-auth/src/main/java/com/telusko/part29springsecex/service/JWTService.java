package com.telusko.part29springsecex.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JWTService {

    @Value("${jwt.secret}")
    private String secretkey = "";

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        Algorithm algorithm = Algorithm.HMAC256(secretkey);
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date(System.currentTimeMillis()))
                .withExpiresAt(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .withPayload(claims)
                .sign(algorithm);
    }

    public String extractUserName(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUserName(token);
            Algorithm algorithm = Algorithm.HMAC256(secretkey);
            JWT.require(algorithm).build().verify(token);
            return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        DecodedJWT decodedJWT = JWT.decode(token);
        return decodedJWT.getExpiresAt().before(new Date());
    }
}
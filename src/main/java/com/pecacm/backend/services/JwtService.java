package com.pecacm.backend.services;

import com.pecacm.backend.exception.AcmException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    public String generateToken(UserDetails userDetails) {
        HashMap<String, Object> claims = new HashMap<>();
        return Jwts.builder().setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + (1000L * 3600 * 24 * 30)))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
        } catch (Exception ex) {
            throw new AcmException("Token is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception ex) {
            throw new AcmException("Token is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    public Date extractExpiration(String token) {
        try {
            return extractClaim(token, Claims::getExpiration);
        } catch (Exception ex) {
            throw new AcmException("Token is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception ex) {
            throw new AcmException("Token is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claim) {
        try {
            Claims claims = extractAllClaims(token);
            return claim.apply(claims);
        } catch (Exception ex) {
            throw new AcmException("Token is invalid", HttpStatus.BAD_REQUEST);
        }
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
        } catch (Exception ex) {
            throw new AcmException("Token is invalid", HttpStatus.BAD_REQUEST);
        }
    }

}

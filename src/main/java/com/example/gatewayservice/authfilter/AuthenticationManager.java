package com.example.gatewayservice.authfilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;

    public AuthenticationManager(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }


    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = authentication.getCredentials().toString();

        try {
            Jws<Claims> claims = jwtUtil.getAllClaimsFromToken(token);
            if (claims == null) {
                return Mono.empty();
            }
            Date expirationDate = claims.getBody().getExpiration();
            if (expirationDate.before(new Date(System.currentTimeMillis())))
                return Mono.empty();

            String subject = claims.getBody().getSubject();
            ArrayList<String> permissions = (ArrayList<String>) claims.getBody().get("roles");
            List<SimpleGrantedAuthority> authorities = permissions.stream().map(SimpleGrantedAuthority::new).toList();

            return Mono.just(new UsernamePasswordAuthenticationToken(subject, null, authorities));

        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            return Mono.empty();
        }
    }
}

package com.example.gatewayservice.security;

import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.example.gatewayservice.security.SecurityConstants.HEADER_NAME;
import static com.example.gatewayservice.security.SecurityConstants.TOKEN_PREFIX;


public class AuthorizationFilter extends OncePerRequestFilter {


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws IOException, ServletException {

        String header = request.getHeader(HEADER_NAME);
        String username = "";
        String token = "";

        if (header == null) {
            logger.warn(HEADER_NAME + " header not found");
        } else if (!header.startsWith(TOKEN_PREFIX)) {
            logger.warn("Token does not start with 'Bearer'");
        } else {
            token = header.substring(HEADER_NAME.length() + 1);
            try {
                username = TokenUtil.getUsernameFromToken(token);
            } catch (IllegalArgumentException e) {
                logger.warn("Unable to get JWT token", e);
            } catch (ExpiredJwtException e) {
                logger.warn("This token has expired", e);
            }
        }

        if (tokenIsValidAndUnauthenticated(username, token)) {
            UsernamePasswordAuthenticationToken authenticationToken = TokenUtil.getAuthenticationToken(token);
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }
        chain.doFilter(request, response);
    }

    private boolean tokenIsValidAndUnauthenticated(String username, String token) {
        return !username.isEmpty() && !token.isEmpty() && SecurityContextHolder.getContext().getAuthentication() == null;
    }
}

package com.example.gatewayservice.authfilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final String [] urls = { "/sign-up", "/login" };

    public SecurityConfig(AuthenticationManager authenticationManager,
                          SecurityContextRepository securityContextRepository) {
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .httpBasic().disable()
                .formLogin().disable()
                .csrf().disable()
                .logout().disable()
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .authenticationManager(authenticationManager)
                .securityContextRepository(securityContextRepository)
                .exceptionHandling()
                .authenticationEntryPoint((exchange, exception) -> getResponse(exchange, HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((exchange, exception) -> getResponse(exchange, HttpStatus.FORBIDDEN))
                .and()
                .authorizeExchange()
                .pathMatchers(urls).permitAll()
                .anyExchange().authenticated()
                .and()
                .build();
    }

    private Mono<Void> getResponse(ServerWebExchange exchange, HttpStatus status) {
        return Mono.fromRunnable(() -> exchange.getResponse().setStatusCode(status));
    }
}

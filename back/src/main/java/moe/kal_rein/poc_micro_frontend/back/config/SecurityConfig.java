package moe.kal_rein.poc_micro_frontend.back.config;

import lombok.RequiredArgsConstructor;
import moe.kal_rein.poc_micro_frontend.back.security.*;
import moe.kal_rein.poc_micro_frontend.back.security.oauth2.HttpCookieOAuth2AuthorizationRequestRepository;
import moe.kal_rein.poc_micro_frontend.back.security.oauth2.OAuth2AuthenticationFailureHandler;
import moe.kal_rein.poc_micro_frontend.back.security.oauth2.OAuth2AuthenticationSuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = false)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AppProperties appProperties;
    private final CookieTokenResolver cookieTokenResolver;
    private final CookieLogoutHandler cookieLogoutHandler;
    private final OidcLogoutSuccessHandler oidcLogoutSuccessHandler;
    private final RefreshTokenAuthenticationFilter refreshTokenAuthenticationFilter;
    private final KeycloakJwtAuthenticationConverter keycloakJwtAuthenticationConverter;
    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;
    private final UnauthorizedAuthenticationEntryPoint unauthorizedAuthenticationEntryPoint;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource())
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedAuthenticationEntryPoint)
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/oauth2/authorization/*", "/login/oauth2/code/*", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(server -> server
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(keycloakJwtAuthenticationConverter)
                        )
                        .bearerTokenResolver(cookieTokenResolver)
                )
                .oauth2Login(login -> login
                        .authorizationEndpoint(endpoint -> endpoint
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )
                .logout(logout -> logout
                        .addLogoutHandler(cookieLogoutHandler)
                        .logoutSuccessHandler(oidcLogoutSuccessHandler)
                );

        http.addFilterBefore(refreshTokenAuthenticationFilter, BearerTokenAuthenticationFilter.class);
        return http.build();
    }

    private CorsConfigurationSource corsConfigurationSource() {
        var configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}

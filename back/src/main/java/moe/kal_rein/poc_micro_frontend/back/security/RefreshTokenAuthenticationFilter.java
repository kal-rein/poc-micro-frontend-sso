package moe.kal_rein.poc_micro_frontend.back.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.kal_rein.poc_micro_frontend.back.config.AppProperties;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.endpoint.DefaultRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenAuthenticationFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final AppProperties appProperties;
    private final CookieService cookieService;
    private final CookieTokenResolver cookieTokenResolver;
    private final CookieRefreshTokenResolver cookieRefreshTokenResolver;
    private final ClientRegistrationRepository clientRegistrationRepository;

    private final OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> accessTokenResponseClient = new DefaultRefreshTokenTokenResponseClient();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var accessToken = cookieTokenResolver.resolve(request);
        var refreshToken = cookieRefreshTokenResolver.resolve(request);
        if (accessToken == null || refreshToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var accessTokenJwt = getJwt(accessToken);
        if (accessTokenJwt != null) {
            filterChain.doFilter(request, response);
            return;
        }

        var oauth2AccessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, accessToken, null, null);
        var oauth2RefreshToken = new OAuth2RefreshToken(refreshToken, null);

        var authorizedClient = getAuthorizedClient(oauth2AccessToken, oauth2RefreshToken);
        if (authorizedClient == null) {
            filterChain.doFilter(request, response);
            return;
        }

        var grantRequest = new OAuth2RefreshTokenGrantRequest(authorizedClient.getClientRegistration(), oauth2AccessToken, oauth2RefreshToken);
        try {
            var tokenResponse = accessTokenResponseClient.getTokenResponse(grantRequest);
            var idToken = (String) tokenResponse.getAdditionalParameters().get("id_token");
            cookieService.addAuthCookies(request, response, tokenResponse.getAccessToken(), tokenResponse.getRefreshToken(), idToken);

            request.setAttribute(appProperties.getAuth().getAccessTokenCookieName(), tokenResponse.getAccessToken().getTokenValue());
        } catch (OAuth2AuthorizationException ex) {
            log.error("Error requesting new access token using refresh token grant", ex);
            cookieService.deleteAuthCookies(request, response);
        }

        filterChain.doFilter(request, response);
    }

    @Nullable
    private Jwt getJwt(String token) {
        try {
            return jwtDecoder.decode(token);
        } catch (BadJwtException failed) {
            return null;
        }
    }

    @Nullable
    private OAuth2AuthorizedClient getAuthorizedClient(OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        var clientRegistration = clientRegistrationRepository.findByRegistrationId(appProperties.getOauth2().getClientName());
        if (clientRegistration == null) {
            return null;
        }

        return new OAuth2AuthorizedClient(clientRegistration, "RefreshTokenAuthenticationFilter", accessToken, refreshToken);
    }
}

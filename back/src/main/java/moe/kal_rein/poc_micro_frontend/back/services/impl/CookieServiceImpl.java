package moe.kal_rein.poc_micro_frontend.back.services.impl;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import moe.kal_rein.poc_micro_frontend.back.config.AppProperties;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CookieServiceImpl implements CookieService {

    private final AppProperties appProperties;

    @Override
    public Optional<Cookie> get(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }

        return Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(name)).findFirst();
    }

    @Override
    public void add(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(request.isSecure());

        var domain = appProperties.getCookie().getDomain();
        if (StringUtils.hasText(domain)) {
            cookie.setDomain(domain);
        }

        response.addCookie(cookie);
    }

    @Override
    public void delete(HttpServletRequest request, HttpServletResponse response, String name) {
        get(request, name).ifPresent((cookie) -> {
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            cookie.setMaxAge(0);
            cookie.setSecure(request.isSecure());

            var domain = appProperties.getCookie().getDomain();
            if (StringUtils.hasText(domain)) {
                cookie.setDomain(domain);
            }

            response.addCookie(cookie);
        });
    }

    @Override
    public void addOauth2Cookies(HttpServletRequest request, HttpServletResponse response, String authorizationRequest, String state, String redirectUri) {
        add(request, response, appProperties.getOauth2().getAuthorizationRequestCookieName(), authorizationRequest, -1);
        add(request, response, appProperties.getOauth2().getStateCookieName(), state, -1);

        if (StringUtils.hasText(redirectUri)) {
            add(request, response, appProperties.getOauth2().getRedirectUriCookieName(), redirectUri, -1);
        }
    }

    @Override
    public void deleteOauth2Cookies(HttpServletRequest request, HttpServletResponse response) {
        delete(request, response, appProperties.getOauth2().getAuthorizationRequestCookieName());
        delete(request, response, appProperties.getOauth2().getRedirectUriCookieName());
        delete(request, response, appProperties.getOauth2().getStateCookieName());
    }

    @Override
    public void addAuthCookies(HttpServletRequest request, HttpServletResponse response, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken, String idToken) {
        var expirationSeconds = (int) Math.floorDiv(appProperties.getAuth().getTokenExpirationMs(), 1000);

        add(request, response, appProperties.getAuth().getAccessTokenCookieName(), accessToken.getTokenValue(), expirationSeconds);
        if (refreshToken != null) {
            add(request, response, appProperties.getAuth().getRefreshTokenCookieName(), refreshToken.getTokenValue(), expirationSeconds);
        }

        if (idToken != null) {
            add(request, response, appProperties.getAuth().getIdTokenCookieName(), idToken, expirationSeconds);
        }
    }

    @Override
    public void deleteAuthCookies(HttpServletRequest request, HttpServletResponse response) {
        delete(request, response, appProperties.getAuth().getAccessTokenCookieName());
        delete(request, response, appProperties.getAuth().getRefreshTokenCookieName());
    }
}

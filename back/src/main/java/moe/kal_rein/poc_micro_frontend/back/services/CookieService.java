package moe.kal_rein.poc_micro_frontend.back.services;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;

import java.util.Optional;

public interface CookieService {

    Optional<Cookie> get(HttpServletRequest request, String name);

    void add(HttpServletRequest request, HttpServletResponse response, String name, String value, int maxAge);

    void delete(HttpServletRequest request, HttpServletResponse response, String name);

    void addOauth2Cookies(HttpServletRequest request, HttpServletResponse response, String authorizationRequest, String state, @Nullable String redirectUri);

    void deleteOauth2Cookies(HttpServletRequest request, HttpServletResponse response);

    void addAuthCookies(HttpServletRequest request, HttpServletResponse response, OAuth2AccessToken accessToken, @Nullable OAuth2RefreshToken refreshToken, @Nullable String idToken);

    void deleteAuthCookies(HttpServletRequest request, HttpServletResponse response);
}

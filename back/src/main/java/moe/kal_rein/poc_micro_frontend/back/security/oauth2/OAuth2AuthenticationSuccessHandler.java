package moe.kal_rein.poc_micro_frontend.back.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.kal_rein.poc_micro_frontend.back.config.AppProperties;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.apache.coyote.BadRequestException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AppProperties appProperties;
    private final CookieService cookieService;
    private final OAuth2AuthorizedClientRepository authorizedClientRepository;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        var authorizedClient = authorizedClientRepository.loadAuthorizedClient(appProperties.getOauth2().getClientName(), authentication, request);
        var accessToken = authorizedClient.getAccessToken();
        if (ObjectUtils.isEmpty(accessToken.getTokenValue())) {
            oAuth2AuthenticationFailureHandler.onAuthenticationFailure(request, response, new AuthenticationServiceException("Access token not available"));
            return;
        }

        var refreshToken = authorizedClient.getRefreshToken();
        var oidcUser = (DefaultOidcUser) authentication.getPrincipal();
        cookieService.addAuthCookies(request, response, accessToken, refreshToken, oidcUser.getIdToken().getTokenValue());

        var targetUrl = getTargetUrl(request);

        clearAuthenticationAttributes(request, response);
        if (response.isCommitted()) {
            log.debug("Response has already been committed. Unable to redirect to {}", targetUrl);
            return;
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getTargetUrl(HttpServletRequest request) throws BadRequestException {
        var redirectUri = cookieService.get(request, appProperties.getOauth2().getRedirectUriCookieName()).map(Cookie::getValue);
        if (redirectUri.isPresent() && !appProperties.getOauth2().isAuthorizedRedirectUri(redirectUri.get())) {
            throw new BadRequestException(String.format("The redirect URI is not part of the authorized list %s", redirectUri.get()));
        }

        var targetUrl = redirectUri.orElse(getDefaultTargetUrl());
        return UriComponentsBuilder.fromUriString(targetUrl).build().toUriString();
    }

    private void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        cookieService.deleteOauth2Cookies(request, response);
    }
}

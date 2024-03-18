package moe.kal_rein.poc_micro_frontend.back.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import moe.kal_rein.poc_micro_frontend.back.config.AppProperties;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieTokenResolver implements BearerTokenResolver {

    private final AppProperties appProperties;
    private final CookieService cookieService;

    @Override
    public String resolve(HttpServletRequest request) {
        var accessTokenRequest = request.getAttribute(appProperties.getAuth().getAccessTokenCookieName());
        if (accessTokenRequest != null) {
            return (String) accessTokenRequest;
        }

        return cookieService.get(request, appProperties.getAuth().getAccessTokenCookieName())
                .map(Cookie::getValue)
                .orElse(null);
    }
}

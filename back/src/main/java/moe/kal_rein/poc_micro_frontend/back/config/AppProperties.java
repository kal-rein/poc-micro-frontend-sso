package moe.kal_rein.poc_micro_frontend.back.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Getter
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Auth auth = new Auth();
    private final OAuth2 oauth2 = new OAuth2();
    private final Cookie cookie = new Cookie();

    @Getter
    @Setter
    public static class Auth {
        private String accessTokenCookieName;
        private String refreshTokenCookieName;
        private String idTokenCookieName;
        private long tokenExpirationMs;
        private String logoutUrlRedirect;
    }

    @Getter
    @Setter
    public static class OAuth2 {
        private String clientName;
        private String authorizationRequestCookieName;
        private String redirectUriCookieName;
        private String redirectUriParamName;
        private String stateCookieName;
        private List<String> authorizedRedirectUris = new ArrayList<>();

        public boolean isAuthorizedRedirectUri(String redirectUri) {
            var targetRedirectUri = URI.create(redirectUri);
            return authorizedRedirectUris.stream().anyMatch((authorizedRedirectUri) -> {
                var authorizedUri = URI.create(authorizedRedirectUri);
                return targetRedirectUri.getHost().equalsIgnoreCase(authorizedUri.getHost())
                        && targetRedirectUri.getPort() == authorizedUri.getPort();
            });
        }
    }

    @Getter
    @Setter
    public static class Cookie {
        private String domain;
    }
}

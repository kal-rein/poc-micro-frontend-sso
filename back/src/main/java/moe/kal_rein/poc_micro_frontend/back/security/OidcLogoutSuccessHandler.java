package moe.kal_rein.poc_micro_frontend.back.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import moe.kal_rein.poc_micro_frontend.back.config.AppProperties;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.SimpleUrlLogoutSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OidcLogoutSuccessHandler extends SimpleUrlLogoutSuccessHandler {

    private final AppProperties appProperties;
    private final CookieService cookieService;
    private final ClientRegistrationRepository clientRegistrationRepository;

    @Override
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        var idToken = cookieService.get(request, appProperties.getAuth().getIdTokenCookieName());
        if (idToken.isEmpty()) {
            return super.determineTargetUrl(request, response);
        }

        var clientRegistration = clientRegistrationRepository.findByRegistrationId(appProperties.getOauth2().getClientName());
        if (clientRegistration == null) {
            return super.determineTargetUrl(request, response);
        }

        var endSessionEndpoint = endSessionEndpoint(clientRegistration);
        if (endSessionEndpoint == null) {
            return super.determineTargetUrl(request, response);
        }

        return endpointUri(endSessionEndpoint, idToken.get().getValue(), appProperties.getAuth().getLogoutUrlRedirect());
    }

    @Nullable
    private URI endSessionEndpoint(ClientRegistration clientRegistration) {
        if (clientRegistration != null) {
            var providerDetails = clientRegistration.getProviderDetails();
            var endSessionEndpoint = providerDetails.getConfigurationMetadata().get("end_session_endpoint");
            if (endSessionEndpoint != null) {
                return URI.create(endSessionEndpoint.toString());
            }
        }

        return null;
    }

    private String endpointUri(URI endSessionEndpoint, String idToken, @Nullable String postLogoutRedirectUri) {
        var builder = UriComponentsBuilder.fromUri(endSessionEndpoint);
        builder.queryParam("id_token_hint", idToken);
        if (postLogoutRedirectUri != null) {
            builder.queryParam("post_logout_redirect_uri", postLogoutRedirectUri);
        }

        return builder.encode(StandardCharsets.UTF_8)
                .build()
                .toUriString();
    }
}

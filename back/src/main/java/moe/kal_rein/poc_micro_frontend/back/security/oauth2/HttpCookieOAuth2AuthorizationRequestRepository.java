package moe.kal_rein.poc_micro_frontend.back.security.oauth2;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import moe.kal_rein.poc_micro_frontend.back.config.AppProperties;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final ConversionService conversionService = new DefaultConversionService();
    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;
    private final CookieService cookieService;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        var stateCookie = cookieService.get(request, appProperties.getOauth2().getStateCookieName()).orElse(null);
        if (stateCookie == null) {
            log.debug("State cookie is not available");
            return null;
        }

        return cookieService.get(request, appProperties.getOauth2().getAuthorizationRequestCookieName())
                .flatMap((cookie) -> getAuthorizationRequest(cookie, stateCookie.getValue()))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            cookieService.deleteOauth2Cookies(request, response);
            return;
        }

        if (ObjectUtils.isEmpty(authorizationRequest.getState())) {
            log.error("Authorization request state is empty");
            return;
        }

        cookieService.addOauth2Cookies(
                request,
                response,
                serializeAuthorizationRequest(authorizationRequest),
                authorizationRequest.getState(),
                request.getParameter(appProperties.getOauth2().getRedirectUriParamName())
        );
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        var authorizationRequest = loadAuthorizationRequest(request);

        cookieService.deleteOauth2Cookies(request, response);
        return authorizationRequest;
    }

    private Optional<OAuth2AuthorizationRequest> getAuthorizationRequest(Cookie cookie, String state) {
        return deserializeAuthorizationRequest(cookie.getValue()).flatMap((authorizationRequest) -> {
            if (!state.equals(authorizationRequest.getState())) {
                log.warn("Received state doesn't match the state stored previously");
                return Optional.empty();
            }

            return Optional.of(authorizationRequest);
        });
    }

    private String serializeAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest) {
        var node = objectMapper.createObjectNode();
        node.put("authorizationUri", authorizationRequest.getAuthorizationUri());
        node.put("authorizationRequestUri", authorizationRequest.getAuthorizationRequestUri());

        node.put("clientId", authorizationRequest.getClientId());
        node.put("redirectUri", authorizationRequest.getRedirectUri());

        var scopeArrayNode = node.putArray("scopes");
        authorizationRequest.getScopes().forEach(scopeArrayNode::add);
        node.put("state", authorizationRequest.getState());

        var attributesNode = node.putObject("attributes");
        authorizationRequest.getAttributes().forEach((key, value) -> attributesNode.put(key, conversionService.convert(value, String.class)));

        var additionalParamsNode = node.putObject("additionalParams");
        authorizationRequest.getAdditionalParameters().forEach((key, value) -> additionalParamsNode.put(key, conversionService.convert(value, String.class)));

        return Base64.getEncoder().encodeToString(node.toString().getBytes());
    }

    private Optional<OAuth2AuthorizationRequest> deserializeAuthorizationRequest(String serializedAuthorizationRequest) {
        JsonNode node;
        try {
            var json = new String(Base64.getDecoder().decode(serializedAuthorizationRequest));
            node = objectMapper.readTree(json);
        } catch (JsonProcessingException ex) {
            log.error("Cannot deserialize authorization request from JSON", ex);
            return Optional.empty();
        }

        var attributesMap = new HashMap<String, Object>();
        node.get("attributes").fields().forEachRemaining(entry -> attributesMap.put(entry.getKey(), entry.getValue().textValue()));

        var additionalParamsMap = new HashMap<String, Object>();
        node.get("additionalParams").fields().forEachRemaining(entry -> additionalParamsMap.put(entry.getKey(), entry.getValue().textValue()));

        var scopes = new HashSet<String>();
        node.withArray("scopes").forEach(scopeNode -> scopes.add(scopeNode.textValue()));

        var authorizationRequest = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri(node.get("authorizationUri").textValue())
                .authorizationRequestUri(node.get("authorizationRequestUri").textValue())
                .clientId(node.get("clientId").textValue())
                .redirectUri(node.get("redirectUri").textValue())
                .state(node.get("state").textValue())
                .scopes(scopes)
                .attributes(attributesMap)
                .additionalParameters(additionalParamsMap)
                .build();

        return Optional.of(authorizationRequest);
    }
}

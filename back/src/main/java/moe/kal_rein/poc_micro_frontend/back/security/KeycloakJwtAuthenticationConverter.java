package moe.kal_rein.poc_micro_frontend.back.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Component
public class KeycloakJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final static String SCOPE_AUTHORITY_PREFIX = "SCOPE_";
    private final static String REALM_AUTHORITY_PREFIX = "ROLE_";
    private final static String RESOURCE_AUTHORITY_PREFIX = "ROLE_";

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        return new JwtAuthenticationToken(jwt, getAuthorities(jwt), jwt.getClaimAsString(JwtClaimNames.SUB));
    }

    @SuppressWarnings("unchecked")
    private Collection<? extends GrantedAuthority> getAuthorities(Jwt jwt) {
        var authorities = new HashSet<GrantedAuthority>();

        var scope = jwt.getClaimAsString("scope");
        if (StringUtils.hasText(scope)) {
            Arrays.stream(scope.split(" "))
                    .map(s -> SCOPE_AUTHORITY_PREFIX + s)
                    .map(SimpleGrantedAuthority::new)
                    .forEach(authorities::add);
        }

        var realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            var roles = (List<String>) realmAccess.get("roles");
            if (roles != null) {
                roles.stream()
                        .map(role -> REALM_AUTHORITY_PREFIX + role)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }

        var resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            var roles = (List<String>) resourceAccess.get("roles");
            if (roles != null) {
                roles.stream()
                        .map(role -> RESOURCE_AUTHORITY_PREFIX + role)
                        .map(SimpleGrantedAuthority::new)
                        .forEach(authorities::add);
            }
        }

        return authorities;
    }
}

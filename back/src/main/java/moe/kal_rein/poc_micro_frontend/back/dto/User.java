package moe.kal_rein.poc_micro_frontend.back.dto;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Set;
import java.util.stream.Collectors;

public record User(
        String uuid,
        String name,
        Set<String> authorities
) {
    public static User from(JwtAuthenticationToken token) {
        return new User(
                token.getToken().getClaimAsString(JwtClaimNames.SUB),
                token.getToken().getClaimAsString("preferred_username"),
                token.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toSet())
        );
    }
}

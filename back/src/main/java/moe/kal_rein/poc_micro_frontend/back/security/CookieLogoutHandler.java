package moe.kal_rein.poc_micro_frontend.back.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import moe.kal_rein.poc_micro_frontend.back.services.CookieService;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CookieLogoutHandler implements LogoutHandler {

    private final CookieService cookieService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cookieService.deleteAuthCookies(request, response);
    }
}

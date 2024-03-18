package moe.kal_rein.poc_micro_frontend.back.controller;

import moe.kal_rein.poc_micro_frontend.back.dto.User;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/user")
public class UserController {

    @GetMapping("me")
    public User me(JwtAuthenticationToken token) {
        return User.from(token);
    }
}

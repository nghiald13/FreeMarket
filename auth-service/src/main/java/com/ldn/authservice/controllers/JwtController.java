package com.ldn.authservice.controllers;

import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.services.AuthService;
import com.ldn.common.annotations.RawResponse;
import com.ldn.common.annotations.Public;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Public
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/token")
public class JwtController {
    private final AuthService authService;
    private final RSAKey rsaJwk;

    @RawResponse
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaJwk.toPublicJWK()).toJSONObject();   // rsaJwk inject qua constructor
    }

    @PostMapping("/refresh")
    public AuthResponse refreshTokens(@RequestHeader("X-Refresh-Token") String rawRefreshToken) {
        return this.authService.refreshTokens(rawRefreshToken);
    }


}

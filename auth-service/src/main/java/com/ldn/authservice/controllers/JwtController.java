package com.ldn.authservice.controllers;

import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.security.RsaKeyProperties;
import com.ldn.authservice.services.AuthService;
import com.ldn.common.security.Public;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/token")
public class JwtController {
    private final AuthService authService;
    private final RsaKeyProperties rsaKeys;

    @Public
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        RSAKey publicJwk = new RSAKey.Builder(rsaKeys.publicKey()).build();
        return new JWKSet(publicJwk).toJSONObject();
    }

    @PostMapping("/refresh")
    public AuthResponse refreshTokens(@RequestHeader("X-Refresh-Token") String rawRefreshToken) {
        return this.authService.refreshTokens(rawRefreshToken);
    }


}

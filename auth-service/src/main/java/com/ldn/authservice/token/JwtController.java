package com.ldn.authservice.token;

import com.ldn.authservice.auth.AuthService;
import com.ldn.authservice.auth.dto.responses.AuthResponse;
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
    private final RSAKey rsaJwk;

    @RawResponse
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        return new JWKSet(rsaJwk.toPublicJWK()).toJSONObject();   // rsaJwk inject qua constructor
    }
}

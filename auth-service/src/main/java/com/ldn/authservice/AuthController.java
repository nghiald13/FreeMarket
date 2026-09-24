package com.ldn.authservice;

import com.ldn.authservice.dto.request.LoginRequest;
import com.ldn.authservice.dto.request.RegisterRequest;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.security.RsaKeyProperties;
import com.ldn.common.security.Public;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Public
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final RsaKeyProperties rsaKeys;

    @PostMapping("/register")
    public RegisterResponse createAccount(@RequestBody @Valid RegisterRequest registerRequest) {
        return this.authService.createAccount(registerRequest);
    }

    @PostMapping("/login")
    public String login(@RequestBody @Valid LoginRequest loginRequest) {
        return this.authService.login(loginRequest);
    }

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        RSAKey publicJwk = new RSAKey.Builder(rsaKeys.publicKey()).build();
        return new JWKSet(publicJwk).toJSONObject();
    }
}

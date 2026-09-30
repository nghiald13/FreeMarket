package com.ldn.authservice.controllers;

import com.ldn.authservice.dto.request.*;
import com.ldn.authservice.dto.response.AuthResponse;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.security.RsaKeyProperties;
import com.ldn.authservice.services.AuthService;
import com.ldn.common.security.Public;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final RsaKeyProperties rsaKeys;


    @Public
    @PostMapping("/register")
    public RegisterResponse createAccount(@RequestBody @Valid RegisterRequest registerRequest) {
        return this.authService.createAccount(registerRequest);
    }

    @Public
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        return this.authService.login(loginRequest);
    }

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

    @Public
    @PostMapping("/verify/process")
    public String verifyProcessRequest(@RequestBody() @Valid VerifyProcessRequest verifyProcessRequest) {
        return this.authService.verifyProcessRequest(verifyProcessRequest);
    }

    @Public
    @PostMapping("/verify/request")
    public void verifyEmailRequest(@RequestBody() @Valid VerifyEmailRequest verifyEmailRequest) {
        this.authService.verifyEmailRequest(verifyEmailRequest);
    }

    @Public
    @PostMapping("/verify/proceed")
    public AuthResponse verifyProceedRequest(@RequestBody() @Valid VerifyProceedRequest verifyProceedRequest) {
        return this.authService.verifyProceedRequest(verifyProceedRequest);
    }

}

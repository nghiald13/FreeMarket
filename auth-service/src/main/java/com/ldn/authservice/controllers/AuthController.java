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

@Public
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse createAccount(@RequestBody @Valid RegisterRequest registerRequest) {
        return this.authService.createAccount(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        return this.authService.login(loginRequest);
    }

    @PostMapping("/verify/process")
    public String verifyProcessRequest(@RequestBody() @Valid VerifyProcessRequest verifyProcessRequest) {
        return this.authService.verifyProcessRequest(verifyProcessRequest);
    }

    @PostMapping("/verify/request")
    public void verifyEmailRequest(@RequestBody() @Valid VerifyEmailRequest verifyEmailRequest) {
        this.authService.verifyEmailRequest(verifyEmailRequest);
    }

    @PostMapping("/verify/proceed")
    public AuthResponse verifyProceedRequest(@RequestBody() @Valid VerifyProceedRequest verifyProceedRequest) {
        return this.authService.verifyProceedRequest(verifyProceedRequest);
    }

}

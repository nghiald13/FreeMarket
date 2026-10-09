package com.ldn.authservice.auth;

import com.ldn.authservice.auth.dto.requests.*;
import com.ldn.authservice.auth.dto.responses.AuthResponse;
import com.ldn.authservice.auth.dto.responses.RegisterResponse;
import com.ldn.common.annotations.Public;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Public
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse createAccount(@RequestBody @Valid RegisterRequest registerRequest) {
        return this.authService.createAccount(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        return this.authService.login(loginRequest);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestHeader("X-Refresh-Token") String rawRefreshToken) {
        return this.authService.refreshTokens(rawRefreshToken);
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

    @PostMapping("/forgotPW")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void forgotPasswordRequest(@RequestBody() @Valid ForgotPasswordRequest forgotPasswordRequest) {
        this.authService.forgotPasswordRequest(forgotPasswordRequest);
    }

    @PostMapping("/resetPW")
    public AuthResponse resetPasswordRequest(@RequestBody() @Valid ResetPasswordRequest resetPasswordRequest) {
        return this.authService.resetPasswordRequest(resetPasswordRequest);
    }

}

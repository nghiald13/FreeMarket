package com.ldn.authservice;

import com.ldn.authservice.dto.request.RegisterRequest;
import com.ldn.authservice.dto.response.RegisterResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public RegisterResponse createAccount(@RequestBody RegisterRequest registerRequest) {
        return this.authService.createAccount(registerRequest);
    }
}

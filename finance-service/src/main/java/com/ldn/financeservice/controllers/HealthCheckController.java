package com.ldn.financeservice.controllers;

import com.ldn.common.annotations.Public;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finance")
public class HealthCheckController {

    @Public
    @GetMapping("/health-check")
    public String healthCheck() {
        return "healthy";
    }
}

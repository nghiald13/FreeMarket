package com.ldn.authservice.auth.dto.responses;

import com.ldn.authservice.auth.entities.Account;

public record RegisterResponse(Long id) {
    public static RegisterResponse fromEntity(Account account) {
        return new RegisterResponse(account.getId());
    }
}

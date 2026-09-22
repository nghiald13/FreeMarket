package com.ldn.authservice.dto.response;

import com.ldn.authservice.pojo.Account;

public record RegisterResponse(Long id) {
    public static RegisterResponse fromEntity(Account account) {
        return new RegisterResponse(account.getId());
    }
}

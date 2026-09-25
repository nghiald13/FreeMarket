package com.ldn.authservice.dto;

import java.util.Map;

public record RefreshTokenDto(
        Long accountId,
        String token,
        boolean isRevoked
) {
    public static RefreshTokenDto fromMap(Map<String, Object> map) {
        return new RefreshTokenDto(
                (Long) map.get("accountId"),
                (String) map.get("token"),
                (Boolean) map.get("isRevoked")
        );
    }
}

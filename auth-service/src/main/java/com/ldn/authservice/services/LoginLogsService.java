package com.ldn.authservice.services;

import com.ldn.authservice.entities.Account;
import com.ldn.authservice.entities.LoginLogs;
import com.ldn.authservice.repositories.LoginLogsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogsService {
    private final LoginLogsRepository loginLogsRepository;

    public LoginLogs log(Account account, String identifier, String status) {
        LoginLogs log = LoginLogs.builder()
                .accountId(account)
                .identifierUsed(identifier)
                .status(status)
                .build();
        return this.loginLogsRepository.save(log);
    }

    public int checkLatest5Attempts(Account account) {
        return this.loginLogsRepository.getRecentFailedAttempt(account);
    }
}

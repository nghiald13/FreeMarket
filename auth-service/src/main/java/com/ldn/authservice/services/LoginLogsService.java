package com.ldn.authservice.services;

import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.pojo.LoginLogs;
import com.ldn.authservice.repository.LoginLogsRepository;
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

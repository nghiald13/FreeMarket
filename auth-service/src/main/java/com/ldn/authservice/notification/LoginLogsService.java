package com.ldn.authservice.notification;

import com.ldn.authservice.auth.entities.Account;
import com.ldn.authservice.notification.entities.LoginLogs;
import com.ldn.authservice.notification.repositories.LoginLogsRepository;
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

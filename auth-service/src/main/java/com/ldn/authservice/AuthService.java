package com.ldn.authservice;

import com.ldn.authservice.dto.request.RegisterRequest;
import com.ldn.authservice.dto.response.RegisterResponse;
import com.ldn.authservice.exception.AccountExistedException;
import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.repository.AccountRepository;
import com.ldn.common.utils.Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AccountRepository accountRepository;

    public RegisterResponse createAccount (RegisterRequest userInfo) {
        // Check uniqueness of email and phone
        boolean existed = !this.accountRepository.findByEmailOrPhone((userInfo.email()), userInfo.phone()).isEmpty();
        if (existed) throw new AccountExistedException();

        // Hash password & generate mfa_secret
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password_hash = encoder.encode(userInfo.password());
        String mfaSecret = Utils.generateBase32Secret();

        // Business
        Account account = Account.builder()
                .name(userInfo.name())
                .email(userInfo.email())
                .password(password_hash)
                .phone(userInfo.phone())
                .mfaSecret(mfaSecret)
                .build();
        accountRepository.save(account);

        return RegisterResponse.fromEntity(account);
    }
}

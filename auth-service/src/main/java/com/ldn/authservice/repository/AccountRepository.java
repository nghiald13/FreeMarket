package com.ldn.authservice.repository;

import com.ldn.authservice.pojo.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmailOrPhone(String email, String phone);
    Optional<Account> findByEmail(String email);
}

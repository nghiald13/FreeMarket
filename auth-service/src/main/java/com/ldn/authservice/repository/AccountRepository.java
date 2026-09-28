package com.ldn.authservice.repository;

import com.ldn.authservice.pojo.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmailOrPhone(String email, String phone);

    @Query("SELECT account from Account account WHERE account.email = :emailOrPhone OR account.phone = :emailOrPhone")
    Optional<Account> findByEmailOrPhone(String emailOrPhone);
}

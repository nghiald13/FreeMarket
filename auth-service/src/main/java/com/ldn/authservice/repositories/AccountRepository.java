package com.ldn.authservice.repositories;

import com.ldn.authservice.entities.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByEmailOrPhone(String email, String phone);

    @Query("SELECT account from Account account WHERE account.email = :emailOrPhone OR account.phone = :emailOrPhone")
    Optional<Account> findByEmailOrPhone(String emailOrPhone);
}

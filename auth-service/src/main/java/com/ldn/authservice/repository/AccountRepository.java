package com.ldn.authservice.repository;

import com.ldn.authservice.pojo.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByEmailOrPhone(String email, String phone);
}

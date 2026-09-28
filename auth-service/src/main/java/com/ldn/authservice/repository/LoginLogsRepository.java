package com.ldn.authservice.repository;

import com.ldn.authservice.pojo.Account;
import com.ldn.authservice.pojo.LoginLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface LoginLogsRepository extends JpaRepository<LoginLogs, Long> {
    @Query("""
                SELECT COUNT(logs)
                FROM LoginLogs logs
                WHERE logs.status = 'failed'
                	AND logs.id IN (
                		SELECT subLogs.id
                		FROM LoginLogs subLogs
                        WHERE subLogs.accountId = :account
                		ORDER BY subLogs.createdAt DESC
                		LIMIT 5
                	)
            """)
    int getRecentFailedAttempt(Account account);
}

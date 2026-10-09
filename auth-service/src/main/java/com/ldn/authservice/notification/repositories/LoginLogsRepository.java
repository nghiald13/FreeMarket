package com.ldn.authservice.notification.repositories;

import com.ldn.authservice.auth.entities.Account;
import com.ldn.authservice.notification.entities.LoginLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

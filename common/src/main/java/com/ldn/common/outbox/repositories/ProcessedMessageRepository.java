package com.ldn.common.outbox.repositories;

import com.ldn.common.outbox.entities.ProcessedMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedMessageRepository extends JpaRepository<ProcessedMessage, String> {
}

package com.ldn.common.outbox.repositories;

import com.ldn.common.outbox.entities.OutboxEvent;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE) // FOR UPDATE
    @QueryHints({
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "-2")
    })
    @Query("""
            SELECT ev
            FROM OutboxEvent ev
            WHERE ev.publishedAt IS NULL
                        ORDER BY ev.id ASC
            """)
    List<OutboxEvent> findUnpublishedForUpdate(Pageable pageable);

    @Modifying
    @Query("delete from OutboxEvent ev where ev.publishedAt is not null and ev.publishedAt < :before")
    int deletePublishedBefore(@Param("before") LocalDateTime before);

    boolean existsByAggregateId(String aggregateId);
}
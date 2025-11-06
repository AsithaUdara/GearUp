package com.gearup.userauth.repository;

import com.gearup.userauth.model.UserAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserAuditLogRepository extends JpaRepository<UserAuditLog, Long> {

    List<UserAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId);

    Page<UserAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    List<UserAuditLog> findByUserIdAndActionOrderByCreatedAtDesc(Long userId, String action);

    List<UserAuditLog> findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
            Long userId, LocalDateTime startDate, LocalDateTime endDate);
}

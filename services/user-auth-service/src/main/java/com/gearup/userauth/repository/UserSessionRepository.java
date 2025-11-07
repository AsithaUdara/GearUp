package com.gearup.userauth.repository;

import com.gearup.userauth.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    Optional<UserSession> findBySessionToken(String sessionToken);

    Optional<UserSession> findByRefreshToken(String refreshToken);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.user.id = :userId AND s.isActive = true")
    void deactivateAllUserSessions(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE UserSession s SET s.isActive = false WHERE s.sessionToken = :sessionToken")
    void deactivateSession(@Param("sessionToken") String sessionToken);
}

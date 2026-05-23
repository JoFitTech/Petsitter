package com.softwareengineering.petsitter.user.repository;

import com.softwareengineering.petsitter.user.domain.PasswordResetCode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetCodeRepository extends JpaRepository<PasswordResetCode, UUID> {

    Optional<PasswordResetCode> findFirstByEmailAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            @Param("email") String email,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    List<PasswordResetCode> findByEmailAndUsedAtIsNull(@Param("email") String email);

    @Modifying
    @Query("DELETE FROM PasswordResetCode prc WHERE prc.expiresAt < :threshold OR (prc.createdAt < :oneHourAgo AND prc.usedAt IS NOT NULL)")
    void deleteExpiredCodes(@Param("threshold") LocalDateTime threshold, @Param("oneHourAgo") LocalDateTime oneHourAgo);
}

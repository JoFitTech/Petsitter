package com.softwareengineering.petsitter.user.repository;

import com.softwareengineering.petsitter.user.domain.LoginCode;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.UUID;
import java.util.List;

@Repository
public interface LoginCodeRepository extends JpaRepository<LoginCode, UUID> {

    /**
     * Findet den aktuellsten, noch gültigen Registrierungs-Code für eine Email.
     * Nutzt findFirstBy + OrderBy für JPQL-Kompatibilität (kein LIMIT 1).
     */
    Optional<LoginCode> findFirstByEmailAndUsedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
            @Param("email") String email,
            @Param("expiresAt") LocalDateTime expiresAt
    );

    /**
     * Findet alle ungenutzten Codes für eine Email (zum Invalidieren).
     */
    List<LoginCode> findByEmailAndUsedAtIsNull(@Param("email") String email);

    /**
     * Löscht abgelaufene und alte Codes (älter als 1 Stunde).
     * @Modifying ist DRINGEND erforderlich für DELETE-Queries!
     */
    @org.springframework.data.jpa.repository.Modifying
    @Query("DELETE FROM LoginCode lc WHERE lc.expiresAt < :threshold OR (lc.createdAt < :oneHourAgo AND lc.usedAt IS NOT NULL)")
    void deleteExpiredCodes(@Param("threshold") LocalDateTime threshold, @Param("oneHourAgo") LocalDateTime oneHourAgo);
}

package com.softwareengineering.petsitter.review.repository;

import com.softwareengineering.petsitter.review.domain.UserReview;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserReviewRepository extends JpaRepository<UserReview, UUID> {

    boolean existsByBooking_IdAndReviewer_Id(UUID bookingId, UUID reviewerId);

    Optional<UserReview> findByBooking_IdAndReviewer_Id(UUID bookingId, UUID reviewerId);

    long countByReviewee_Id(UUID revieweeId);

    @Query("select avg(r.rating) from UserReview r where r.reviewee.id = :revieweeId")
    Double findAverageRatingByRevieweeId(@Param("revieweeId") UUID revieweeId);
}


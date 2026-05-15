package com.softwareengineering.petsitter.favorite.domain;

import com.softwareengineering.petsitter.offer.domain.Offer;
import com.softwareengineering.petsitter.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "favorite",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_favorite_user_offer",
                columnNames = {"user_id", "offer_id"}))
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "favorite_id", nullable = false)
    private UUID favoriteId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "offer_id", nullable = false)
    private Offer offer;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Favorite() {
    }

    public Favorite(User user, Offer offer) {
        this.user = user;
        this.offer = offer;
    }

    public UUID getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(UUID favoriteId) {
        this.favoriteId = favoriteId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Offer getOffer() {
        return offer;
    }

    public void setOffer(Offer offer) {
        this.offer = offer;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}

package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.PasswordResetToken;
import com.hubert.apartmentbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findTopByUserOrderByCreatedAtDesc(User user);

    void deleteByUser(User user);
}
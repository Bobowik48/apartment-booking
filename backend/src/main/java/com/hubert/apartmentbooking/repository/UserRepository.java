package com.hubert.apartmentbooking.repository;

import com.hubert.apartmentbooking.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
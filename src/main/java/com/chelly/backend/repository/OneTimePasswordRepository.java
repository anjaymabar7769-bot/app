package com.chelly.backend.repository;

import com.chelly.backend.models.OneTimePassword;
import com.chelly.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OneTimePasswordRepository extends JpaRepository<OneTimePassword, Integer> {
    Optional<OneTimePassword> findByCode(String code);
    List<OneTimePassword> findAllByUserAndAvailableTrue(User user);
}

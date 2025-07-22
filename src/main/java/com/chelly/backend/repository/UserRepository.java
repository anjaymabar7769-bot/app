package com.chelly.backend.repository;

import com.chelly.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.name = 'USER' ORDER BY u.points DESC")
    List<User> findTopUsersForLeaderboard();


    @Query("SELECT COUNT(user) + 1 FROM User user WHERE user.points > (SELECT user2.points FROM User user2 WHERE user2.id = :userId)")
    Integer getUserRank(@Param("userId") Integer userId);
}

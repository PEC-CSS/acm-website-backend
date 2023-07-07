package com.pecacm.backend.repository;

import com.pecacm.backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByEmailOrSid(String email, Integer sid);

    Optional<User> findByEmail(String email);
}

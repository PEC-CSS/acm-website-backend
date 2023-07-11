package com.pecacm.backend.repository;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByEmailOrSid(String email, Integer sid);

    Optional<User> findByEmail(String email);

    @Query("SELECT u.designation FROM User u " +
           "WHERE u.email = :email")
    Optional<Role> findRoleByEmail(String email);

    @Modifying
    @Query("UPDATE User " +
           "SET designation = :newRole " +
           "WHERE email = :email")
    void updateRoleByEmail(String email, Role newRole);
}

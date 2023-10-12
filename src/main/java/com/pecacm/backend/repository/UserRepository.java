package com.pecacm.backend.repository;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByEmailOrSid(String email, Integer sid);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email in :emails")
    List<User> findAllByEmail(Collection<String> emails);

    Page<User> findAllByBatch(int batch, PageRequest pageRequest);

    @Query("SELECT u.designation FROM User u " +
           "WHERE u.email = :email")
    Optional<Role> findRoleByEmail(String email);

    @Modifying
    @Query("UPDATE User " +
           "SET designation = :newRole " +
           "WHERE email = :email")
    void updateRoleByEmail(String email, Role newRole);

    Long countByXpGreaterThan(Integer xp);

    Page<User> findAllByOrderByXpDesc(PageRequest pageRequest);
}

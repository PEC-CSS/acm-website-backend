package com.pecacm.backend.repository;

import com.pecacm.backend.entities.User;
import com.pecacm.backend.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    Boolean existsByEmailOrSid(String email, Integer sid);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.email in :emails")
    List<User> findAllByEmail(Collection<String> emails);

    Page<User> findAllByBatch(int batch, PageRequest pageRequest);

    @Query("SELECT u.designation FROM User u " +
           "WHERE u.email = :email")
    Optional<Role> findRoleByEmail(String email);

    @Query("SELECT u.verified FROM User u " +
            "WHERE u.email = :email")
    Optional<Boolean> checkVerifiedByEmail(String email);

    @Modifying
    @Query("UPDATE User " +
           "SET designation = :newRole " +
           "WHERE email = :email")
    void updateRoleByEmail(String email, Role newRole);

    @Modifying
    @Query("UPDATE User " +
            "SET password = :password " +
            "WHERE email = :email")
    void updatePasswordByEmail(String password, String email);

    Long countByXpGreaterThan(Integer xp);

    Page<User> findAllByOrderByXpDesc(PageRequest pageRequest);
    @Query("SELECT u FROM User u " +
            "WHERE SPLIT_PART(u.email, '.', 1) ILIKE %:query% " +
            "AND (:verifiedOnly = false OR u.verified = true) " +
            "ORDER BY u.id LIMIT 10")
    List<User> findAllBySearchQuery(@Param("query") String query, @Param("verifiedOnly") Boolean verifiedOnly);

    List<User> findAllByDesignation(Role role);

    List<User> findByEmailIn(List<String> emails);

}

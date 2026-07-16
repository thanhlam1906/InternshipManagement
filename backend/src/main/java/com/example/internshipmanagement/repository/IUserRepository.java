package com.example.internshipmanagement.repository;

import com.example.internshipmanagement.entity.User;
import com.example.internshipmanagement.entity.enums.AuthProvider;
import com.example.internshipmanagement.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IUserRepository extends JpaRepository<User, Integer> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.username = :username OR u.email = :email")
    boolean existsByUsernameOrEmail(@Param("username") String username, @Param("email") String email);

    @EntityGraph(attributePaths = {"student", "mentor"})
    Page<User> findByRole(Role role, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"student", "mentor"})
    Page<User> findAll(Pageable pageable);

    boolean existsByEmailAndUserIdNot(String email, Integer userId);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByProviderAndProviderId(AuthProvider provider, String providerId);
}

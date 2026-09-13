package com.maieveen.crm.security;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    @EntityGraph(attributePaths = "roles")
    Optional<AdminUser> findByUsername(String username);

    Optional<AdminUser> findByEmailIgnoreCase(String email);
}

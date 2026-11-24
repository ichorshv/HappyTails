package com.happytails.adoption.repository;

import com.happytails.adoption.entity.User;
import com.happytails.adoption.enums.Role;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  Page<User> findByRole(Role role, Pageable pageable);
}

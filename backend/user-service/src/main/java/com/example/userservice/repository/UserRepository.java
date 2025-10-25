package com.example.userservice.repository;

import com.example.userservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 사용자명으로 사용자 찾기
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 이메일로 사용자 찾기
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 사용자명 또는 이메일로 사용자 찾기
     */
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    /**
     * 사용자명 존재 여부 확인
     */
    boolean existsByUsername(String username);
    
    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
    
    /**
     * 활성화된 사용자 찾기
     */
    @Query("SELECT u FROM User u WHERE u.username = :username AND u.isEnabled = true")
    Optional<User> findActiveUserByUsername(@Param("username") String username);
    
    /**
     * 이메일 또는 이름으로 검색 (페이징)
     */
    Page<User> findByEmailContainingOrNameContaining(String email, String name, Pageable pageable);
    
    /**
     * 역할별 사용자 수 조회
     */
    long countByRole(User.Role role);
    
    /**
     * 활성화 상태별 사용자 수 조회
     */
    long countByIsEnabled(boolean isEnabled);
}
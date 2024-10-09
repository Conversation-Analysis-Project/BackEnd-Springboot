package com.sometimes.code.repository;


import com.sometimes.code.domain.article.Article;
import com.sometimes.code.domain.auth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByNameAndBirth(String name, LocalDate birth);

    boolean existsByEmail(String email);

    Optional<User> findByNickName(String nickName);

    Optional<User> findByUserId(Long userId);



}

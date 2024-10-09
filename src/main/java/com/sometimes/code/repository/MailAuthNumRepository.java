package com.sometimes.code.repository;


import com.sometimes.code.domain.auth.MailAuthNum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailAuthNumRepository extends JpaRepository<MailAuthNum, Long> {
    Optional<MailAuthNum> findByEmailAndAuthNum(String email, String authNum);
}

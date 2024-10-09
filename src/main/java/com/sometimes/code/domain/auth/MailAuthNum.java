package com.sometimes.code.domain.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MailAuthNum {

    @Id
    private String email;

    @Column(nullable = false)
    private String authNum;

    @Column(nullable = false)
    private LocalDateTime expiration;

}
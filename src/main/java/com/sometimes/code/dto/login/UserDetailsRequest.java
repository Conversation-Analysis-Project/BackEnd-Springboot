package com.sometimes.code.dto.login;

import com.sometimes.code.domain.auth.Authority;
import com.sometimes.code.domain.auth.User;
import lombok.Data;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;

@Data
public class UserDetailsRequest {
    private String email;
    private String password;
    private String name;
    private LocalDate birth;
    private User.Gender gender;
    private String nickName;
    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(email, password);
    }
}

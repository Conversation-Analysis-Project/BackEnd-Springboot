package com.sometimes.code.dto.myPage;

import com.sometimes.code.domain.auth.User;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UserInfoResponse {

    private Long userId;
    private String email;
    private List<String> profileImages;
    private String nickName;
    private String name;
    private LocalDate birth;
    private User.Gender gender;
}

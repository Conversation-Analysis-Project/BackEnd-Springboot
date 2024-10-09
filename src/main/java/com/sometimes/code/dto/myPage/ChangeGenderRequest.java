package com.sometimes.code.dto.myPage;

import com.sometimes.code.domain.auth.User;
import lombok.Data;

@Data
public class ChangeGenderRequest {
    private User.Gender newGender;
}

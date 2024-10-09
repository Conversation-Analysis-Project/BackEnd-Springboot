package com.sometimes.code.dto.myPage;

import lombok.Data;

import java.time.LocalDate;


@Data
public class ChangeBirthRequest {

    private LocalDate newBirth;
}

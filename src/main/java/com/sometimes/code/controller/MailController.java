package com.sometimes.code.controller;

import com.sometimes.code.dto.mail.EmailCheckDto;
import com.sometimes.code.dto.mail.MailVO;
import com.sometimes.code.service.AwsMailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MailController {

    private final AwsMailService awsMailService;

    @PostMapping("/mailSend")
    public ResponseEntity<String> sendMail(@RequestBody MailVO mailVO) {
        // 이메일 전송
        awsMailService.send(mailVO);

        return ResponseEntity.ok("Email sent successfully!");
    }

    @PostMapping("/mailAuth")
    public ResponseEntity<String> authenticateEmail(@RequestBody EmailCheckDto emailCheckDto) {
        boolean isValid = awsMailService.validateAuthNum(emailCheckDto);
        if (isValid) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(400).body("인증 실패");
        }
    }
}

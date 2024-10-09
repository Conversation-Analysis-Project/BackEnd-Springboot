package com.sometimes.code.service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import com.amazonaws.services.simpleemail.model.SendRawEmailResult;
import com.sometimes.code.config.MailUtil;
import com.sometimes.code.domain.auth.MailAuthNum;
import com.sometimes.code.dto.mail.EmailCheckDto;
import com.sometimes.code.dto.mail.MailVO;
import com.sometimes.code.repository.MailAuthNumRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class AwsMailService {

    private final AmazonSimpleEmailService amazonSimpleEmailService;
    private final MailAuthNumRepository mailAuthNumRepository;

    public String send(MailVO mailVO) {
        // 인증 번호 생성
        String authNum = generateAuthNum();

        // 이메일 본문 구성
        String bodyHtml = String.format("<h2>인증번호는 %s 입니다</h2>", authNum);

        try {
            // 이메일 전송
            SendRawEmailRequest sendRawEmailRequest = MailUtil.getSendRawEmailRequest(mailVO.getEmail(), bodyHtml);
            SendRawEmailResult result = amazonSimpleEmailService.sendRawEmail(sendRawEmailRequest);
            log.info("Email sent successfully, message ID: {}", result.getMessageId());

            // 인증 번호와 만료 시간 저장
            LocalDateTime expiration = LocalDateTime.now().plusMinutes(10); // 10분 유효
            saveAuthNum(mailVO.getEmail(), authNum, expiration);
            return "Email sent successfully!";
        } catch (Exception e) {
            log.error("이메일 전송 중 오류 발생", e);
            return "Failed to send email: " + e.getMessage();
        }
    }

    public void saveAuthNum(String email, String authNum, LocalDateTime expiration) {
        MailAuthNum mailAuthNum = new MailAuthNum(email, authNum, expiration);
        mailAuthNumRepository.save(mailAuthNum);
    }

    public boolean validateAuthNum(EmailCheckDto emailCheckDto) {
        // 인증 번호와 이메일로 DB에서 인증 번호 찾기
        return mailAuthNumRepository.findByEmailAndAuthNum(emailCheckDto.getEmail(), emailCheckDto.getAuthNum())
                .map(mailAuthNum -> mailAuthNum.getExpiration().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    private String generateAuthNum() {
        Random random = new Random();
        int authNum = 100000 + random.nextInt(900000); // 6자리 랜덤 숫자 생성
        return String.valueOf(authNum);
    }
}

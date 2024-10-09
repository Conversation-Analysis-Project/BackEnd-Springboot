package com.sometimes.code.config;

import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

@Slf4j
public class MailUtil {

    private static final String SUBJECT = "sometime 메일 인증 번호입니다.";

    public static SendRawEmailRequest getSendRawEmailRequest(String receiver, String bodyHtml) {
        try {
            // 메일 세션 생성
            Session session = Session.getDefaultInstance(new Properties());
            MimeMessage message = new MimeMessage(session);

            // 제목 설정
            message.setSubject(SUBJECT);

            // 발신자 설정
            message.setFrom(new InternetAddress("hsjj001102@gmail.com"));

            // 수신자 설정
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));

            // 메일 본문 설정
            MimeMultipart msgBody = new MimeMultipart("alternative");

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(bodyHtml, "text/html; charset=UTF-8");

            msgBody.addBodyPart(htmlPart);

            MimeBodyPart wrap = new MimeBodyPart();
            wrap.setContent(msgBody);

            MimeMultipart msg = new MimeMultipart("mixed");
            msg.addBodyPart(wrap);

            message.setContent(msg);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);

            RawMessage rawMessage = new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));
            return new SendRawEmailRequest(rawMessage);

        } catch (MessagingException | IOException e) {
            log.error("메일 생성 중 오류 발생", e);
            throw new RuntimeException("메일 생성 중 오류 발생", e);
        }
    }
}


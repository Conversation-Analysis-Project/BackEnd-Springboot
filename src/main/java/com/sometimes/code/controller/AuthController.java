package com.sometimes.code.controller;

import com.sometimes.code.dto.login.ResetPasswordRequest;
import com.sometimes.code.dto.login.TokenDto;
import com.sometimes.code.dto.login.TokenRequestDto;
import com.sometimes.code.dto.login.UserDetailsRequest;
import com.sometimes.code.repository.UserRepository;
import com.sometimes.code.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody UserDetailsRequest userDetailsRequest){
        try {
            authService.signup(userDetailsRequest);
            return ResponseEntity.ok("User registered successfully.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/emailCheck/{email}")
    public ResponseEntity<Boolean> checkEmail(@PathVariable String email) {
        boolean isEmailTaken = authService.isEmailTaken(email);
        // 사용 중이면 true, 사용 가능하면 false
        return ResponseEntity.ok(!isEmailTaken);
    }


    @GetMapping("/nickNameCheck/{nickName}")
    public ResponseEntity<Boolean> checkNickName(@PathVariable String nickName) {
        boolean isNickNameTaken = authService.isNicknameTaken(nickName);
        // 사용 중이면 true, 사용 가능하면 false
        return ResponseEntity.ok(!isNickNameTaken);
    }


    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDetailsRequest userDetailsRequest){
        try {
            TokenDto tokenDto = authService.login(userDetailsRequest);
            return ResponseEntity.ok(tokenDto);
        } catch (AuthenticationException ex) {
            // Spring Security의 AuthenticationException 사용
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + ex.getMessage());
        } catch (Exception ex) {
            // 기타 예외 처리
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred: " + ex.getMessage());
        }
    }

    @PostMapping("/reissue")
    public ResponseEntity<?> reissue(@RequestBody TokenRequestDto tokenRequestDto) {
        try {
            TokenDto tokenDto = authService.reissue(tokenRequestDto);
            return ResponseEntity.ok(tokenDto);
        } catch (Exception ex) {
            // 단순한 문자열 메시지 반환
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Token reissue failed: " + ex.getMessage());
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.getEmail(), request.getNewPassword());
            // 성공 시 단순 문자열 메시지 반환
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception ex) {
            // 예외 발생 시 단순 문자열 메시지 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while updating password: " + ex.getMessage());
        }
    }


}

package com.sometimes.code.service;

import com.sometimes.code.domain.auth.Authority;
import com.sometimes.code.domain.auth.RefreshToken;
import com.sometimes.code.domain.auth.User;
import com.sometimes.code.dto.login.TokenDto;
import com.sometimes.code.dto.login.TokenRequestDto;
import com.sometimes.code.dto.login.UserDetailsRequest;
import com.sometimes.code.jwt.TokenProvider;
import com.sometimes.code.repository.RefreshTokenRepository;
import com.sometimes.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;


    public boolean isEmailTaken(String email){
        return userRepository.findByEmail(email).isPresent();
    }

    public boolean isNicknameTaken(String nickName) {
        return userRepository.findByNickName(nickName).isPresent();
    }

    public void signup(UserDetailsRequest userDetailsRequest) {

        String encodedPassword = passwordEncoder.encode(userDetailsRequest.getPassword());


        User user = new User();
        user.setEmail(userDetailsRequest.getEmail());
        user.setPassword(encodedPassword);
        user.setName(userDetailsRequest.getName());
        user.setNickName(userDetailsRequest.getNickName());
        user.setBirth(userDetailsRequest.getBirth());
        user.setGender(userDetailsRequest.getGender());
        user.setAuthority(Authority.ROLE_USER);

        userRepository.save(user);
    }

    @Transactional
    public TokenDto login(UserDetailsRequest userDetailRequest) {
        // 1. Login ID/PW 를 기반으로 AuthenticationToken 생성
        UsernamePasswordAuthenticationToken authenticationToken = userDetailRequest.toAuthentication();

        // 2. 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
        //    authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername 메서드가 실행됨
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 4. RefreshToken 저장
        RefreshToken refreshToken = RefreshToken.builder()
                .key(authentication.getName())
                .value(tokenDto.getRefreshToken())
                .build();

        refreshTokenRepository.save(refreshToken);

        // 5. 토큰 발급
        return tokenDto;
    }

    @Transactional
    public TokenDto reissue(TokenRequestDto tokenRequestDto) {
        // 1. Refresh Token 검증
        if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
        }

        // 2. Access Token 에서 Member ID 가져오기
        Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

        // 3. 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
        RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
                .orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

        // 4. Refresh Token 일치하는지 검사
        if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
            throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
        }

        // 5. 새로운 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        // 6. 저장소 정보 업데이트
        RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
        refreshTokenRepository.save(newRefreshToken);

        // 토큰 발급
        return tokenDto;
    }

    @Transactional
    public void resetPassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email : " + email));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

}






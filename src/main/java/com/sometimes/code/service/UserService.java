package com.sometimes.code.service;

import com.sometimes.code.domain.auth.ProfileImage;
import com.sometimes.code.domain.auth.User;
import com.sometimes.code.dto.myPage.UserInfoResponse;
import com.sometimes.code.jwt.TokenProvider;
import com.sometimes.code.repository.ProfileImageRepository;
import com.sometimes.code.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final S3ImageService s3ImageService;
    private final ProfileImageRepository profileImageRepository;

    @Transactional(readOnly = true)
    public UserInfoResponse getUserInfo(String token) {
        // 토큰에서 사용자 ID 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 정보 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 해당 유저의 모든 프로필 이미지 URL 가져오기
        List<ProfileImage> profileImages = profileImageRepository.findByUser_UserId(user.getUserId());
        List<String> profileImageUrls = profileImages.stream()
                .map(ProfileImage::getUrl)  // ProfileImage 객체에서 URL을 추출
                .collect(Collectors.toList());

        // 사용자 정보 DTO로 변환
        UserInfoResponse userInfoResponse = new UserInfoResponse();
        userInfoResponse.setUserId(user.getUserId());
        userInfoResponse.setEmail(user.getEmail());
        userInfoResponse.setProfileImages(profileImageUrls);  // 이미지 URL 리스트 설정
        userInfoResponse.setNickName(user.getNickName());
        userInfoResponse.setName(user.getName());
        userInfoResponse.setBirth(user.getBirth());
        userInfoResponse.setGender(user.getGender());

        return userInfoResponse;
    }


    @Transactional
    public void changeNickName(String token, String newNickName) {
        // 토큰에서 사용자 이메일 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 닉네임 변경
        user.setNickName(newNickName);

        // 사용자 정보 저장 (변경 사항 반영)
        userRepository.save(user);
    }

    @Transactional
    public void changeName(String token, String newName) {
        // 토큰에서 사용자 이메일 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 이름 변경
        user.setName(newName);

        // 사용자 정보 저장 (변경 사항 반영)
        userRepository.save(user);
    }

    @Transactional
    public void changeGender(String token, User.Gender newGender) {
        // 토큰에서 사용자 이메일 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 이름 변경
        user.setGender(newGender);

        // 사용자 정보 저장 (변경 사항 반영)
        userRepository.save(user);
    }

    @Transactional
    public void changeBirth(String token, LocalDate newBirth) {
        // 토큰에서 사용자 이메일 추출
        Long userId = tokenProvider.getUserIdFromToken(token);

        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 이름 변경
        user.setBirth(newBirth);

        // 사용자 정보 저장 (변경 사항 반영)
        userRepository.save(user);
    }

    @Transactional
    public String uploadProfileImage(String token, MultipartFile image) {
        Long userId = tokenProvider.getUserIdFromToken(token);

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 기존 프로필 이미지 삭제
        List<ProfileImage> existingProfileImages = profileImageRepository.findByUser_UserId(userId);
        if (!existingProfileImages.isEmpty()) {
            for (ProfileImage profileImage : existingProfileImages) {
                // S3에서 기존 이미지 삭제
                s3ImageService.deleteImageFromS3(profileImage.getUrl());

                // ProfileImage 테이블에서 삭제
                profileImageRepository.delete(profileImage);
            }
        }

        // 새로운 프로필 이미지를 `profileImg` 폴더에 업로드
        String imageUrl = s3ImageService.uploadImage(image, "profileImg");

        // 업로드된 이미지 정보를 ProfileImage 테이블에 저장
        ProfileImage newProfileImage = ProfileImage.builder()
                .user(user)
                .url(imageUrl)
                .build();

        profileImageRepository.save(newProfileImage);

        return imageUrl;
    }



}

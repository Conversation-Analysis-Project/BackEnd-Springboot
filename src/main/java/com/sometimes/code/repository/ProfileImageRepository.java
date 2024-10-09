package com.sometimes.code.repository;

import com.sometimes.code.domain.auth.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

    // userId로 해당 유저의 모든 프로필 이미지를 가져오는 메서드
    List<ProfileImage> findByUser_UserId(Long userId);
}

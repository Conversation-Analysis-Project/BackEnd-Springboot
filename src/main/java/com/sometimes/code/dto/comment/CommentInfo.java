package com.sometimes.code.dto.comment;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CommentInfo {
    private Long commentId;
    private Long userId;
    private String nickName;
    private LocalDate createdAt;
    private List<String> profileImages;  // 여러 프로필 이미지를 위한 리스트로 변경
    private String content;
}

package com.sometimes.code.dto.article;

import com.sometimes.code.domain.article.Article;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ArticleDetailInfo {

    private Long articleId;
    private Long userId;
    private Article.Category category;
    private String title;
    private String nickName;
    private LocalDate createAt;
    private String content;
    private List<String> imageUrls;  // 다중 이미지를 위한 List로 변경
    private Integer likes;
    private Integer hits;
    private Integer commentNum;
    private boolean mine;
    private boolean pushLikes;
    private List<String> profileImgs;  // 여러 프로필 이미지를 지원할 경우 List로 변경
}

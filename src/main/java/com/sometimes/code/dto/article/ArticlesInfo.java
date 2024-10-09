package com.sometimes.code.dto.article;

import com.sometimes.code.domain.article.Article;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ArticlesInfo {
    private Long pageNum;
    private List<ArticleInfo> articles;

    @Data
    @Builder
    public static class ArticleInfo{
        private Long articleId;
        private Long userId;
        private Article.Category category;
        private String title;
        private String author;
        private LocalDate createdAt;
        private Integer likes;
        private Boolean existImg;
        private Integer commentCnt;




    }
}

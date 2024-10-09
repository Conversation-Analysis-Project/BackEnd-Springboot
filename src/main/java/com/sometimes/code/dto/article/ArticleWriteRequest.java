package com.sometimes.code.dto.article;

import com.sometimes.code.domain.article.Article;
import lombok.Data;

@Data
public class ArticleWriteRequest {
    private String title;
    private String content;
    private Article.Category category;
}

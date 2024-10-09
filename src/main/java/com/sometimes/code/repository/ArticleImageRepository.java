package com.sometimes.code.repository;

import com.sometimes.code.domain.article.Article;
import com.sometimes.code.domain.article.ArticleImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleImageRepository extends JpaRepository<ArticleImage, Long> {
    List<ArticleImage> findByArticle(Article article);
}

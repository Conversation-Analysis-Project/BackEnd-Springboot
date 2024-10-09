package com.sometimes.code.repository;

import com.sometimes.code.domain.article.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    int countByArticle_ArticleId(Long articleId);

    boolean existsByUser_UserIdAndArticle_ArticleId(Long userId, Long articleId);

    Optional<Likes> findByUser_UserIdAndArticle_ArticleId(Long userId, Long articleId);
}

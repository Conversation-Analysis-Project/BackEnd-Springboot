package com.sometimes.code.repository;


import com.sometimes.code.domain.article.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleArticleId(Long articleId);

    List<Comment> findByUserUserId(Long userId);

    long countByArticleArticleId(Long articleId);
}
